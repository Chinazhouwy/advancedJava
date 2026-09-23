package com.advancedjava.mq;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

/**
 * Kafka 官方客户端（kafka-clients）基础 API 演示。
 *
 * <p>与 {@link RocketmqApiDemo} 对照：
 * <ul>
 *   <li>顺序：同 Key → 同分区，配合 {@code max.in.flight=1} + 幂等生产者防重试乱序；</li>
 *   <li>事务：{@code initTransactions/beginTransaction/commitTransaction}（EOS，非 RocketMQ 半消息）；</li>
 *   <li>延迟：无原生 API，需自建；</li>
 *   <li>消费：手动 poll + 自己提交 offset（关闭 enable.auto.commit）。</li>
 * </ul>
 *
 * <p>本机无 Broker 时：配置与 Producer 对象构建会完成，{@code send/beginTransaction}
 * 等网络调用捕获异常打印说明。
 */
public final class KafkaApiDemo {

    private static final String BOOTSTRAP = "localhost:9092";
    private static final String ORDER_TOPIC = "order_status";

    public static void main(String[] args) {
        demoNormalAndOrderedSend();
        demoIdempotentOrderedConfig();
        demoTransactionApi();
        demoNoDelayApi();
        demoManualPollCommit();
        demoRocketmqVsKafka();
    }

    private static Properties producerProps() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        // 无 Broker 时快速失败，避免 send 阻塞默认 60s
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3_000);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 3_000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 5_000);
        return props;
    }

    // ---------- 1. 普通 + 顺序（按 Key 路由分区） ----------

    static void demoNormalAndOrderedSend() {
        System.out.println("\n=== 1. 发送 send(ProducerRecord)；顺序 = 同 Key 同分区 ===");
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps())) {
            // 普通：value 可无 key（轮询分区，不保序）
            System.out.println("  无 Key send → future（分区轮询，不保序）");

            // 顺序：orderId 做 key → 同单进同一分区 → 分区内 FIFO
            for (String event : List.of("CREATED", "PAID", "SHIPPED", "COMPLETED")) {
                ProducerRecord<String, String> rec =
                        new ProducerRecord<>(ORDER_TOPIC, "ORDER-A", "ORDER-A → " + event);
                Future<RecordMetadata> f = producer.send(rec);
                try {
                    RecordMetadata md = f.get(); // 串行等结果，保证同单发送顺序
                    System.out.println("  " + event + " → partition=" + md.partition()
                            + " offset=" + md.offset());
                } catch (java.util.concurrent.ExecutionException ex) {
                    System.out.println("  " + event + " → [API 已执行 send(key=ORDER-A)] "
                            + firstLine(ex));
                    break;
                }
            }
            producer.flush();
            System.out.println("  消费端：单线程消费该分区 + 手动提交 offset 才保序");
        } catch (Exception e) {
            System.out.println("  [API 已执行] new KafkaProducer → send(ProducerRecord)");
            System.out.println("  [未连通] " + e.getClass().getSimpleName() + ": " + firstLine(e));
        }
    }

    // ---------- 2. 顺序相关的生产者配置 ----------

    static void demoIdempotentOrderedConfig() {
        System.out.println("\n=== 2. 顺序相关配置（生产端） ===");
        Properties props = producerProps();
        // 开幂等：防 broker 侧重复
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        // 未确认请求 ≤1：避免重试插队导致分区内乱序
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        System.out.println("  enable.idempotence=true");
        System.out.println("  max.in.flight.requests.per.connection=1  ← 防重试乱序");
        System.out.println("  acks=all, retries=MAX");
        System.out.println("  （构造一次 Producer 展示配置对象本身，不发网络请求）");
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            System.out.println("  KafkaProducer 已按上述配置创建成功");
        }
    }

    // ---------- 3. 事务 API（EOS，对照 RocketMQ 半消息） ----------

    static void demoTransactionApi() {
        System.out.println("\n=== 3. 事务 API initTransactions / beginTransaction ===");
        System.out.println("  Kafka 语义：跨分区原子写（EOS），不是 RocketMQ「半消息+本地事务回查」");
        Properties props = producerProps();
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "tx-order-demo");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            try {
                producer.initTransactions();
                producer.beginTransaction();
                producer.send(new ProducerRecord<>(ORDER_TOPIC, "ORDER-K", "Kafka 事务内写入"));
                // 同一事务可跨多个 topic/partition 原子提交
                producer.commitTransaction();
                System.out.println("  initTransactions → beginTransaction → send → commitTransaction");
            } catch (Exception e) {
                System.out.println("  [API 已执行] initTransactions → beginTransaction → send");
                System.out.println("  [未连通] " + e.getClass().getSimpleName() + ": " + firstLine(e));
                try {
                    producer.abortTransaction();
                } catch (Exception ignore) {
                    // abort 也可能因无 Broker 失败，忽略
                }
            }
            System.out.println("  对照 RocketMQ：那边是 sendHalf → 本地事务 → commit/rollback + 回查");
        } catch (Exception e) {
            System.out.println("  [API 已执行] 配置 transactional.id + new KafkaProducer");
            System.out.println("  [未连通] " + e.getClass().getSimpleName() + ": " + firstLine(e));
        }
    }

    // ---------- 4. 延迟消息：Kafka 无原生 API ----------

    static void demoNoDelayApi() {
        System.out.println("\n=== 4. 延迟消息：Kafka 无原生 setDelayTimeLevel ===");
        System.out.println("  可行做法：时间轮/定时扫表到点再 send，或外挂延迟队列（如 RocketMQ/时间轮服务）");
        System.out.println("  对照 RocketMQ：msg.setDelayTimeLevel(6) 一行搞定");
    }

    // ---------- 5. 消费端：poll + 手动提交 ----------

    static void demoManualPollCommit() {
        System.out.println("\n=== 5. 消费端 KafkaConsumer#poll（关闭自动提交） ===");
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "cg_stock");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // 关自动提交：处理成功再 commitSync，否则失败也提交会导致丢消息
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.EARLIEST.name().toLowerCase());
        props.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 3_000);
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 3_000);
        props.put(ConsumerConfig.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG, 2_000);
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of(ORDER_TOPIC));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(800));
            for (ConsumerRecord<String, String> r : records) {
                System.out.println("  poll → p=" + r.partition() + " off=" + r.offset()
                        + " key=" + r.key() + " value=" + r.value());
            }
            System.out.println("  处理完成后 consumer.commitSync() 才推进位点（至少一次 → 仍要幂等）");
            System.out.println("  收到 " + records.count() + " 条（无 Broker 时为 0，属预期）");
        } catch (Exception e) {
            System.out.println("  [API 已执行] enable.auto.commit=false → subscribe → poll → (commitSync)");
            System.out.println("  [未连通] " + e.getClass().getSimpleName() + ": " + firstLine(e));
        }
    }

    // ---------- 6. 能力对比（方法形式） ----------

    static void demoRocketmqVsKafka() {
        System.out.println("\n=== 6. RocketMQ vs Kafka（原生能力） ===");
        System.out.printf("  %-18s %-12s %-12s%n", "能力", "RocketMQ", "Kafka");
        for (Feature f : Feature.values()) {
            System.out.printf("  %-18s %-12s %-12s%n", f.label,
                    rocketmqSupports(f) ? "原生支持" : "不支持",
                    kafkaSupports(f) ? "原生支持" : "不支持");
        }
        System.out.println("  选型：事务半消息/延迟/消费重试/业务顺序 → RocketMQ；高吞吐日志大数据 → Kafka");
    }

    enum Feature {
        ORDERED("顺序消息"),
        TRANSACTION("事务消息"),
        HALF_MESSAGE_CHECKBACK("半消息+回查"),
        DELAY("延迟消息"),
        CONSUMER_RETRY("消费重试/死信"),
        SERVER_FILTER("服务端过滤"),
        HUGE_BACKLOG("海量堆积");

        final String label;

        Feature(String label) {
            this.label = label;
        }
    }

    static boolean rocketmqSupports(Feature f) {
        return switch (f) {
            case ORDERED, TRANSACTION, HALF_MESSAGE_CHECKBACK, DELAY,
                 CONSUMER_RETRY, SERVER_FILTER -> true;
            case HUGE_BACKLOG -> false;
        };
    }

    static boolean kafkaSupports(Feature f) {
        return switch (f) {
            case ORDERED, TRANSACTION, HUGE_BACKLOG -> true;
            // Kafka 有 producer 事务（EOS），但无「半消息对消费者不可见 + Broker 回查本地事务」
            case HALF_MESSAGE_CHECKBACK, DELAY, CONSUMER_RETRY, SERVER_FILTER -> false;
        };
    }

    private static String firstLine(Exception e) {
        String msg = e.getMessage();
        if (msg == null) {
            return "";
        }
        int nl = msg.indexOf('\n');
        return nl < 0 ? msg : msg.substring(0, nl);
    }

    private KafkaApiDemo() {
    }
}
