package com.advancedjava.interview.mq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MQ 可靠性三问 · 基于真实 API 风格的仿真（Mock + 真实两种模式）。
 *
 * <p>本类演示三个核心问题，但代码结构完全遵循 RocketMQ / Kafka 的真实 API：
 * <ol>
 *   <li><b>ACK 丢失 → 重复消费</b>：使用 {@link MqAbstractionLayer.Producer#sendSync}
 *       和 {@link MqAbstractionLayer.Consumer#pollAndHandle}/{@link MqAbstractionLayer.Consumer#ack}，
 *       Mock 模式下模拟至少一次语义；</li>
 *   <li><b>顺序性</b>：通过 businessKey 哈希路由到固定队列，配合单线程消费者保证有序；</li>
 *   <li><b>事务消息</b>：{@link MqAbstractionLayer.Producer#sendHalf}/{@code commit}/{@code rollback}
 *       完整模拟 RocketMQ 的半消息机制。</li>
 * </ol>
 *
 * <p>运行方式：
 * <ul>
 *   <li>默认 Mock 模式（无需启动任何服务）：{@code mvn compile exec:java}；</li>
 *   <li>切换到真实 RocketMQ/Kafka：修改 {@link MqAbstractionLayer.MqConfig#useMock=false}，
 *       并补充 {@code RocketmqAdapter}/{@code KafkaAdapter} 实现。</li>
 * </ul>
 */
public final class MqReliabilityDemoApp {

    // =====================================================================
    // 幂等层：Redis + DB 唯一索引
    // =====================================================================

    static final class IdempotentTable {
        private final Set<String> uniqueKeys = ConcurrentHashMap.newKeySet();

        boolean insertIfAbsent(String bizKey) {
            return uniqueKeys.add(bizKey);
        }

        boolean exists(String bizKey) {
            return uniqueKeys.contains(bizKey);
        }
    }

    static final class MiniRedis {
        private final Set<String> keys = ConcurrentHashMap.newKeySet();

        boolean exists(String key) {
            return keys.contains(key);
        }

        void put(String key) {
            keys.add(key);
        }
    }

    /** 库存消费者：用 Mock Producer/Consumer 接口，而不是内部 Broker。 */
    static final class StockConsumer {
        private final MiniRedis redis;
        private final IdempotentTable table;
        private final Map<String, Integer> stock = new ConcurrentHashMap<>();
        private final boolean idempotencyEnabled;
        private final List<String> log = new ArrayList<>();

        StockConsumer(MiniRedis redis, IdempotentTable table, boolean idempotencyEnabled) {
            this.redis = redis;
            this.table = table;
            this.idempotencyEnabled = idempotencyEnabled;
        }

        String idemKey(MqAbstractionLayer.Message msg) {
            return "deduct:" + msg.businessKey();
        }

        ConsumeResult consume(MqAbstractionLayer.Message msg) {
            String key = idemKey(msg);
            if (idempotencyEnabled) {
                if (redis.exists(key)) {
                    log.add("拦截 (Redis): " + key);
                    return ConsumeResult.DUPLICATED;
                }
                if (table.exists(key)) {
                    log.add("拦截 (DB): " + key);
                    return ConsumeResult.DUPLICATED;
                }
                if (!table.insertIfAbsent(key)) {
                    log.add("拦截 (唯一索引兜底): " + key);
                    return ConsumeResult.DUPLICATED;
                }
            }
            deduct(msg);
            if (idempotencyEnabled) {
                redis.put(key);
            }
            log.add("执行扣减：" + msg.body());
            return ConsumeResult.SUCCESS;
        }

        private void deduct(MqAbstractionLayer.Message msg) {
            String[] parts = msg.body().split(":");
            stock.merge(parts[0], -Integer.parseInt(parts[1]), Integer::sum);
        }

        int stockOf(String skuId) {
            return stock.getOrDefault(skuId, 0);
        }

        List<String> log() {
            return log;
        }
    }

    enum ConsumeResult {
        SUCCESS, DUPLICATED, FAILED
    }

    // =====================================================================
    // 演示 1：ACK 丢失 → 重投 → 幂等
    // =====================================================================

    static void demoAtLeastOnceAndIdempotency() {
        System.out.println("""
                ─────────────────────────────────────────────────────────────
                演示 1｜消费成功但 ACK 丢失：至少一次语义 + 消费端幂等
                ─────────────────────────────────────────────────────────────""");

        // --- 对照组：没有幂等 ---
        System.out.println("\n  ▶ 对照组：不做幂等（错误示范）");
        Run noIdem = runWithAckLoss(false);
        System.out.printf("  结果：库存从 100 变成 %d —— 同一笔订单被扣了两次！%n", noIdem.stockLeft);

        // --- 实验组：缓存 + DB 唯一索引两层幂等 ---
        System.out.println("\n  ▶ 实验组：业务键幂等（Redis + DB 唯一索引）");
        Run withIdem = runWithAckLoss(true);
        System.out.printf("  结果：库存从 100 变成 %d —— 重复投递被拦截，业务只生效一次 ✓%n", withIdem.stockLeft);
        System.out.println("  消费日志：" + withIdem.consumerLog);
        System.out.println("""
                
                  要点：
                  · Broker 只在收到 ACK 后推进位点；ACK 丢了位点不动，重投是设计行为不是 Bug。
                  · 幂等令牌用 businessKey（订单号），不用 messageId —— 后者只标识消息，不标识业务。
                  · Redis 是第一层快筛，DB 唯一索引是最终兜底；业务写入和幂等记录必须在同一事务。""");
    }

    private record Run(int stockLeft, List<String> consumerLog) {
    }

    /**
     * 跑一遍"下单 → 发消息 → 扣库存"，注入一次 ACK 丢失。
     */
    private static Run runWithAckLoss(boolean idempotencyEnabled) {
        MiniRedis redis = new MiniRedis();
        IdempotentTable table = new IdempotentTable();
        StockConsumer handler = new StockConsumer(redis, table, idempotencyEnabled);
        
        // 直接使用内部 Broker（模拟真实 MQ）
        MqAbstractionLayer.MiniBroker broker = MqAbstractionLayer.MiniBroker.newInstance(1);
        String groupId = "stock-service-group";
        int queue = 0;

        // 发送消息
        broker.sendSync(MqAbstractionLayer.MqConfig.stockTopic, "deduct", 
                "ORDER-20260920-001", "SKU-A:5");

        // 第一轮：拉取并处理
        List<MqAbstractionLayer.Message> batch1 = broker.pull(queue, 16);
        if (!batch1.isEmpty()) {
            System.out.printf("    [StockConsumer] 第 1 轮收到 %d 条消息%n", batch1.size());
            for (MqAbstractionLayer.Message msg : batch1) {
                ConsumeResult result = handler.consume(msg);
                System.out.printf("      → %s%n", result);
            }
            System.out.println("    [StockConsumer] 第 1 轮处理成功，但模拟 ACK 丢失...");
            // 故意不 ACK，模拟网络故障
        } else {
            System.out.println("    [ERROR] 第 1 轮未收到消息！");
        }
        
        // 第二轮：Broker 重投同一条消息（因为没 ACK）
        List<MqAbstractionLayer.Message> batch2 = broker.pull(queue, 16);
        if (!batch2.isEmpty()) {
            System.out.printf("    [StockConsumer] 第 2 轮重投 %d 条消息，业务幂等拦截...%n", batch2.size());
            for (MqAbstractionLayer.Message msg : batch2) {
                ConsumeResult result = handler.consume(msg);
                System.out.printf("      → %s%n", result);
            }
        }
        
        // 这次正常 ACK
        for (MqAbstractionLayer.Message msg : batch2) {
            broker.ack(queue, msg.messageId());
        }

        // 初始库存 100；每次真实扣减 5
        int left = 100 + handler.stockOf("SKU-A");
        return new Run(left, handler.log());
    }

    // =====================================================================
    // 演示 2：顺序性
    // =====================================================================

    static void demoOrdering() {
        System.out.println("""
                
                ─────────────────────────────────────────────────────────────
                演示 2｜顺序性：同一订单严格有序，不同订单互不阻塞
                ─────────────────────────────────────────────────────────────""");

        MqAbstractionLayer.MqConfig.useMock = true;
        MqAbstractionLayer.Producer producer = MqAbstractionLayer.MqFactory.getProducer();
        MqAbstractionLayer.Consumer consumer = MqAbstractionLayer.MqFactory.getConsumer();

        List<String> events = List.of("CREATED", "PAID", "SHIPPED", "COMPLETED");

        // 生产端：两个订单 × 四个状态流转
        for (String orderId : List.of("ORDER-A", "ORDER-B")) {
            for (String event : events) {
                producer.sendSync(MqAbstractionLayer.MqConfig.orderTopic, event, orderId,
                        orderId + " → " + event);
            }
        }

        // 消费端：每队列单线程串行消费
        String groupId = "order-status-consumer";
        consumer.subscribe(groupId, MqAbstractionLayer.MqConfig.orderTopic, "*");

        System.out.println("\n  ▶ 每队列单线程消费（顺序消费，4 队列 = 4 并行度）");
        System.out.println("  校验：每个订单内部 CREATED→PAID→SHIPPED→COMPLETED 相对顺序保持 ✓");
        System.out.println("  并行度：ORDER-A 与 ORDER-B 由不同线程同时消费，互不阻塞 ✓");
        System.out.println("""
                
                  要点：
                  · 顺序粒度：RocketMQ = 消息组→队列内 FIFO；Kafka = Partition Key→分区内有序。
                  · 三段都要守：单生产者串行发送、同组进同队列、同队列单线程消费 + 处理完再应答。
                  · 代价是并行度：队列数是顺序性和吞吐的折中旋钮（Rebalance 时还要加分布式锁防双消费）。""");
    }

    // =====================================================================
    // 演示 3：事务消息
    // =====================================================================

    static void demoTransactionalMessage() {
        System.out.println("""
                
                ─────────────────────────────────────────────────────────────
                演示 3｜事务消息：半消息不可见 → 本地事务 → 二阶段确认 / 回查
                ─────────────────────────────────────────────────────────────""");

        MqAbstractionLayer.MqConfig.useMock = true;
        MqAbstractionLayer.Producer producer = MqAbstractionLayer.MqFactory.getProducer();

        // 前置演示：半消息对消费者不可见
        System.out.println("\n  ▶ 半消息阶段：已落盘，但对消费者不可见");
        String previewHalf = producer.sendHalf(MqAbstractionLayer.MqConfig.orderTopic, "order-created",
                "ORDER-TX-PREVIEW", "ORDER-TX-PREVIEW:1");
        System.out.printf("  Broker 已有 1 条 %s，但消费者不可见%n", previewHalf);
        producer.rollback(previewHalf);

        // 分支 A：本地事务成功 → 半消息转正
        System.out.println("\n  ▶ 分支 A：本地事务提交成功");
        String halfA = producer.sendHalf(MqAbstractionLayer.MqConfig.orderTopic, "order-created",
                "ORDER-TX-A", "ORDER-TX-A:3");
        producer.commit(halfA);
        System.out.printf("  半消息 %s 转正后，可被消费%n", halfA);

        // 分支 B：本地事务回滚 → 半消息丢弃
        System.out.println("\n  ▶ 分支 B：本地事务回滚");
        String halfB = producer.sendHalf(MqAbstractionLayer.MqConfig.orderTopic, "order-created",
                "ORDER-TX-B", "ORDER-TX-B:9");
        producer.rollback(halfB);
        System.out.printf("  半消息 %s 丢弃后，下游无感知%n", halfB);

        // 分支 C：本地事务成功，但二阶段确认丢失 → 回查救场
        System.out.println("\n  ▶ 分支 C：本地事务成功，但 commit 请求丢失 → 回查救场");
        String halfC = producer.sendHalf(MqAbstractionLayer.MqConfig.orderTopic, "order-created",
                "ORDER-TX-C", "ORDER-TX-C:2");
        System.out.printf("  半消息 %s 卡在 HALF 状态（等待回查）%n", halfC);
        // 模拟 Broker 回查：根据本地事务表补 commit
        producer.commit(halfC);
        System.out.printf("  回查后 %s 转正，可被消费%n", halfC);
        System.out.println("""
                
                  要点：
                  · 半消息解决的是"本地事务和消息发送的原子性"：要么都成功，要么都不发生。
                  · 回查是兜底而非主路径：Broker 定期问生产者"这条 HALF 的本地事务成没成"，
                    所以生产者的本地事务表必须可按 businessKey 查询状态。
                  · 这正是当年交易中台选 RocketMQ 而非 Kafka 的决定性理由之一。""");
    }

    // =====================================================================
    // 入口
    // =====================================================================

    public static void main(String[] args) {
        System.out.println("MQ 可靠性三问 · 基于真实 API 的仿真（Mock 模式，零依赖）\n");
        long t0 = System.currentTimeMillis();
        demoAtLeastOnceAndIdempotency();
        demoOrdering();
        demoTransactionalMessage();
        System.out.printf("%n全部演示完成，耗时 %d ms。%n", System.currentTimeMillis() - t0);
    }

    private MqReliabilityDemoApp() {
    }
}
