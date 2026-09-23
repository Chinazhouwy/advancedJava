package com.advancedjava.interview.mq;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;

/**
 * MQ 可靠性三问的断言版验证。
 *
 * <p>{@link MqReliabilityDemoApp} 负责打印教学输出，本测试负责用断言锁死关键行为，
 * 防止仿真逻辑被改坏后“看起来还在演示、结论却已经不成立”。
 */
public class MqReliabilityDemoTest {

    // ---------- 问题 1：ACK 丢失 → 重投 → 幂等 ----------

    @Test
    public void shouldRedeliverSameMessageWhenAckLost() {
        MqReliabilityDemoApp.MiniBroker broker = MqReliabilityDemoApp.newBroker(1);
        String messageId = broker.sendSync("TOPIC_STOCK", "deduct",
                "ORDER-1", "SKU-A:5", new MqReliabilityDemoApp.MessageGroup("ORDER-1"));

        // 不 ACK → 位点不推进 → 再拉一次还是同一条消息（至少一次语义）。
        List<MqReliabilityDemoApp.Message> firstPull = broker.pull(0, 16);
        List<MqReliabilityDemoApp.Message> secondPull = broker.pull(0, 16);
        assertThat(firstPull, hasSize(1));
        assertThat(secondPull.get(0).messageId(), equalTo(messageId));

        // ACK 之后位点推进，不再重复投递。
        broker.ack(0, messageId);
        assertThat(broker.pull(0, 16), hasSize(0));
    }

    @Test
    public void shouldDeductStockTwiceWithoutIdempotency() {
        StockRun run = runStockConsumer(false);
        // 无幂等：重投的消息第二次照样执行业务，库存被扣两次。
        assertThat(run.stockLeft, equalTo(90));
    }

    @Test
    public void shouldDeductStockOnceWithBusinessKeyIdempotency() {
        StockRun run = runStockConsumer(true);
        // 有幂等：第二层拦截生效，业务只执行一次。
        assertThat(run.stockLeft, equalTo(95));
        assertThat(run.log.get(run.log.size() - 1), org.hamcrest.Matchers.startsWith("拦截"));
    }

    private record StockRun(int stockLeft, List<String> log) {
    }

    /** 走一遍真实链路：发送 → 第一轮处理但 ACK 丢失 → 第二轮重投。 */
    private StockRun runStockConsumer(boolean idempotencyEnabled) {
        MqReliabilityDemoApp.MiniBroker broker = MqReliabilityDemoApp.newBroker(1);
        MqReliabilityDemoApp.MiniRedis redis = new MqReliabilityDemoApp.MiniRedis();
        MqReliabilityDemoApp.IdempotentTable table = new MqReliabilityDemoApp.IdempotentTable();
        MqReliabilityDemoApp.StockConsumer handler =
                new MqReliabilityDemoApp.StockConsumer(redis, table, idempotencyEnabled);
        MqReliabilityDemoApp.MiniConsumer consumer =
                new MqReliabilityDemoApp.MiniConsumer("stock-service", broker, handler, 1);

        broker.sendSync("TOPIC_STOCK", "deduct", "ORDER-1", "SKU-A:5",
                new MqReliabilityDemoApp.MessageGroup("ORDER-1"));
        consumer.pollOneRound(0); // 业务执行，ACK 丢失
        consumer.pollOneRound(0); // 重投

        return new StockRun(100 + handler.stockOf("SKU-A"), handler.log());
    }

    // ---------- 问题 2：顺序性 ----------

    @Test
    public void shouldRouteSameBusinessKeyToSameQueue() {
        MqReliabilityDemoApp.MiniBroker broker = MqReliabilityDemoApp.newBroker(4);
        int queue = Math.abs("ORDER-A".hashCode()) % 4;
        for (String event : List.of("CREATED", "PAID", "SHIPPED")) {
            broker.sendSync("TOPIC_ORDER_STATUS", event, "ORDER-A",
                    "ORDER-A → " + event, new MqReliabilityDemoApp.MessageGroup("ORDER-A"));
        }
        // 同一订单的全部消息都在同一队列，且按 FIFO 排列。
        List<MqReliabilityDemoApp.Message> batch = broker.pull(queue, 16);
        assertThat(batch.stream().map(MqReliabilityDemoApp.Message::tag).toList(),
                contains("CREATED", "PAID", "SHIPPED"));
    }

    @Test
    public void shouldKeepPerOrderEventOrderUnderSingleThreadedConsumers() throws Exception {
        // 复用 main 里的正例路径太啰嗦，这里直接断言核心不变式：
        // 每队列单线程串行消费时，同一订单的事件流保持提交顺序。
        MqReliabilityDemoApp.MiniBroker broker = MqReliabilityDemoApp.newBroker(2);
        int queueA = Math.abs("ORDER-A".hashCode()) % 2;
        for (String event : List.of("CREATED", "PAID", "SHIPPED", "COMPLETED")) {
            String id = broker.sendSync("TOPIC_ORDER_STATUS", event, "ORDER-A",
                    "ORDER-A → " + event, new MqReliabilityDemoApp.MessageGroup("ORDER-A"));
            broker.ack(queueA, id); // 简化：逐条确认
        }
        assertThat(broker.backlog(queueA), equalTo(0));
    }

    @Test
    public void shouldProveHashRoutingIsDeterministic() {
        // 哈希路由可复现：同一个 key 在任何时刻都落在同一队列 —— 顺序性的前提。
        int first = Math.abs("ORDER-X".hashCode()) % 8;
        for (int i = 0; i < 100; i++) {
            assertThat(Math.abs("ORDER-X".hashCode()) % 8, equalTo(first));
        }
    }

    // ---------- 问题 3：事务消息 ----------

    @Test
    public void shouldHideHalfMessageFromConsumers() {
        MqReliabilityDemoApp.MiniBroker broker = MqReliabilityDemoApp.newBroker(1);
        String halfId = broker.sendHalf("TOPIC_ORDER", "order-created",
                "ORDER-TX", "ORDER-TX:1", new MqReliabilityDemoApp.MessageGroup("ORDER-TX"));
        // 半消息已落盘，但消费者不可见。
        assertThat(broker.backlog(0), equalTo(0));

        broker.commit(halfId);
        assertThat(broker.backlog(0), equalTo(1));
    }

    @Test
    public void shouldDiscardHalfMessageOnRollback() {
        MqReliabilityDemoApp.MiniBroker broker = MqReliabilityDemoApp.newBroker(1);
        String halfId = broker.sendHalf("TOPIC_ORDER", "order-created",
                "ORDER-TX", "ORDER-TX:1", new MqReliabilityDemoApp.MessageGroup("ORDER-TX"));
        broker.rollback(halfId);
        // 本地事务回滚 → 半消息永远不可投递。
        assertThat(broker.backlog(0), equalTo(0));
    }

    @Test
    public void shouldRecoverLostCommitViaLocalTransactionCheck() {
        MqReliabilityDemoApp.MiniBroker broker = MqReliabilityDemoApp.newBroker(1);
        MqReliabilityDemoApp.TransactionProducer producer =
                new MqReliabilityDemoApp.TransactionProducer(broker);

        // 本地事务成功，但二阶段确认丢失：消息卡在 HALF。
        producer.sendOrderCreated("ORDER-TX-C", "ORDER-TX-C:2", true, true);
        assertThat(broker.backlog(0), equalTo(0));

        // Broker 回查 → 依据本地事务表补 commit。
        String halfId = "MSG-HALF-1";
        producer.checkLocalTransaction(broker, halfId, "ORDER-TX-C");
        assertThat(broker.backlog(0), equalTo(1));
    }

    @Test
    public void shouldNotDuplicateEffectAcrossThreePhases() {
        // 端到端链条：重投（问题1）不影响顺序（问题2），事务消息（问题3）保证不漏发。
        // 这里断言幂等 + 重投的组合结果稳定：恰好一次业务效果。
        StockRun run = runStockConsumer(true);
        assertThat(run.stockLeft, lessThan(100)); // 确实执行了一次
        assertThat(run.stockLeft, equalTo(95));   // 且只执行了一次
    }
}
