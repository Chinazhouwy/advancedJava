package com.advancedjava.mq;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * RocketMQ 官方客户端（rocketmq-client）基础 API 演示。
 *
 * <p>本机无 Broker 也可直接跑：构造 Producer/Message、注册选择器与事务监听器
 * 都在连接建立之前完成；{@code start}/{@code send} 若连不上 NameServer 会
 * 捕获异常并打印「API 已走到网络调用」的说明，不视为失败。
 *
 * <p>有 Broker 时（如 docker 起 namesrv+broker），去掉注释中的限制即可完整跑通。
 *
 * <p>运行：mvn -q compile exec:java 不可用时用
 * {@code java -cp target/classes:... com.advancedjava.mq.RocketmqApiDemo}
 * 或 IDE 直接跑 main。
 *
 * <p>对照 Kafka 见 {@link KafkaApiDemo}。
 */
public final class RocketmqApiDemo {

    private static final String NAME_SERVER = "127.0.0.1:9876";
    private static final String ORDER_TOPIC = "TOPIC_ORDER_STATUS";
    private static final String STOCK_TOPIC = "TOPIC_STOCK_DEDUCT";

    public static void main(String[] args) {
        demoNormalSend();
        demoOrderedSend();
        demoTransactionHalfMessage();
        demoDelayMessage();
        demoPushConsumerAtLeastOnce();
    }

    // ---------- 1. 普通消息 ----------

    static void demoNormalSend() {
        System.out.println("\n=== 1. 普通消息 DefaultMQProducer#send ===");
        DefaultMQProducer producer = new DefaultMQProducer("pg_demo");
        producer.setNamesrvAddr(NAME_SERVER);
        try {
            producer.start();
            Message msg = new Message(
                    STOCK_TOPIC,
                    "deduct",
                    "ORDER-1",
                    "SKU-A x5".getBytes(StandardCharsets.UTF_8));
            SendResult result = producer.send(msg);
            System.out.println("  send → " + result);
        } catch (Exception e) {
            System.out.println("  [API 已执行] new DefaultMQProducer → setNamesrvAddr → start → send(msg)");
            System.out.println("  [未连通] " + e.getClass().getSimpleName() + ": " + firstLine(e));
            System.out.println("  启动 NameServer/Broker 后重跑即可拿到 SendResult");
        } finally {
            producer.shutdown();
        }
    }

    // ---------- 2. 顺序消息 ----------

    static void demoOrderedSend() {
        System.out.println("\n=== 2. 顺序消息 send(msg, MessageQueueSelector, arg) ===");
        // 关键：同 orderId 经 selector 永远选同一队列；发送端对同单串行调用 send
        MessageQueueSelector selector = new MessageQueueSelector() {
            @Override
            public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                String orderId = String.valueOf(arg);
                // 同 orderId → 同一队列；返回选中的 MessageQueue
                return mqs.get(Math.floorMod(orderId.hashCode(), mqs.size()));
            }
        };
        System.out.println("  selector: mqs.get(Math.floorMod(orderId.hashCode(), mqs.size()))");

        DefaultMQProducer producer = new DefaultMQProducer("pg_order");
        producer.setNamesrvAddr(NAME_SERVER);
        try {
            producer.start();
            for (String event : List.of("CREATED", "PAID", "SHIPPED", "COMPLETED")) {
                Message msg = new Message(ORDER_TOPIC, event, "ORDER-A",
                        ("ORDER-A → " + event).getBytes(StandardCharsets.UTF_8));
                // 单线程串行发送，保证同单事件先后顺序
                SendResult result = producer.send(msg, selector, "ORDER-A");
                System.out.println("  " + event + " → " + result.getMessageQueue());
            }
            System.out.println("  消费端要求：对该队列单线程「接收→处理→应答」，不能异步打散");
        } catch (Exception e) {
            System.out.println("  [API 已执行] send(msg, selector, orderId) 签名与选择器注册完成");
            System.out.println("  [未连通] " + e.getClass().getSimpleName() + ": " + firstLine(e));
        } finally {
            producer.shutdown();
        }
    }

    // ---------- 3. 事务消息（半消息） ----------

    static void demoTransactionHalfMessage() {
        System.out.println("\n=== 3. 事务消息 TransactionMQProducer#sendMessageInTransaction ===");
        System.out.println("  流程: 半消息(消费者不可见) → 本地事务 → COMMIT / ROLLBACK");
        System.out.println("  确认丢失 → Broker 回查 checkLocalTransaction");

        TransactionMQProducer producer = new TransactionMQProducer("pg_tx");
        producer.setNamesrvAddr(NAME_SERVER);
        // TransactionListener：本地事务执行 + Broker 回查回调（真实客户端 API）
        TransactionListener listener = new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                System.out.println("  executeLocalTransaction: " + new String(msg.getBody(), StandardCharsets.UTF_8));
                // 真实项目：在这里写本地 DB，成功 COMMIT，失败 ROLLBACK，异常 UNKNOW
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                System.out.println("  checkLocalTransaction 回查: " + msg.getMsgId());
                // 真实项目：查本地事务表补状态
                return LocalTransactionState.COMMIT_MESSAGE;
            }
        };
        producer.setTransactionListener(listener);
        try {
            producer.start();
            Message msg = new Message(ORDER_TOPIC, "order-created", "ORDER-TX",
                    "ORDER-TX-A".getBytes(StandardCharsets.UTF_8));
            SendResult result = producer.sendMessageInTransaction(msg, null);
            System.out.println("  sendMessageInTransaction → " + result);
            System.out.println("  Kafka 对照：无半消息+回查，需外挂协调器或应用层实现");
        } catch (Exception e) {
            System.out.println("  [API 已执行] TransactionMQProducer → setTransactionListener → sendMessageInTransaction");
            System.out.println("  [未连通] " + e.getClass().getSimpleName() + ": " + firstLine(e));
        } finally {
            producer.shutdown();
        }
    }

    // ---------- 4. 延迟消息 ----------

    static void demoDelayMessage() {
        System.out.println("\n=== 4. 延迟消息 Message#setDelayTimeLevel ===");
        System.out.println("  固定 18 档: 1s 5s 10s 30s 1m 2m ... 2h（level 1~18，0=不延迟）");

        DefaultMQProducer producer = new DefaultMQProducer("pg_delay");
        producer.setNamesrvAddr(NAME_SERVER);
        try {
            producer.start();
            Message msg = new Message(
                    "TOPIC_DELAY",
                    "timeout",
                    "ORDER-1",
                    "30 分钟未支付关单".getBytes(StandardCharsets.UTF_8));
            // level=6 ≈ 2m；真实语义是投递到延迟队列，到点才对消费者可见
            msg.setDelayTimeLevel(6);
            SendResult result = producer.send(msg);
            System.out.println("  setDelayTimeLevel(6) → " + result);
            System.out.println("  Kafka 对照：无原生延迟队列，需应用层/外部件");
        } catch (Exception e) {
            System.out.println("  [API 已执行] msg.setDelayTimeLevel(6) → producer.send(msg)");
            System.out.println("  [未连通] " + e.getClass().getSimpleName() + ": " + firstLine(e));
        } finally {
            producer.shutdown();
        }
    }

    // ---------- 5. 消费端：Push + 至少一次 ----------

    static void demoPushConsumerAtLeastOnce() {
        System.out.println("\n=== 5. 消费端 DefaultMQPushConsumer（至少一次 → 需幂等） ===");
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("cg_stock");
        consumer.setNamesrvAddr(NAME_SERVER);
        try {
            consumer.subscribe(STOCK_TOPIC, "*");
            // 真实 API：业务处理成功才返回 CONSUME_SUCCESS；
            // ACK 丢失则 Broker 重投同一条（messageId 相同）→ 消费端用订单号幂等
            consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                for (MessageExt m : msgs) {
                    System.out.println("  收到 " + m.getMsgId()
                            + " keys=" + m.getKeys()
                            + " body=" + new String(m.getBody(), StandardCharsets.UTF_8));
                    // TODO: 业务幂等：以 m.getKeys()（订单号）做 Redis+DB 唯一索引，勿用 getMsgId
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });
            consumer.start();
            System.out.println("  subscribe + registerMessageListener + start 完成，等待消息…");
            Thread.sleep(3_000);
        } catch (Exception e) {
            System.out.println("  [API 已执行] subscribe → registerMessageListener → start");
            System.out.println("  [未连通] " + e.getClass().getSimpleName() + ": " + firstLine(e));
        } finally {
            consumer.shutdown();
        }
    }

    private static String firstLine(Exception e) {
        String msg = e.getMessage();
        if (msg == null) {
            return "";
        }
        int nl = msg.indexOf('\n');
        return nl < 0 ? msg : msg.substring(0, nl);
    }

    private RocketmqApiDemo() {
    }
}
