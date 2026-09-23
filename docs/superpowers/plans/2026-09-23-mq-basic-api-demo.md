# MQ 基础 API 展示（两版类 · 无 Broker）实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 删除 `interview/mq` 旧包，在 `com.advancedjava.mq` 用**恰好两个类**以标准 RocketMQ API 风格展示顺序消息、事务/半消息、延迟消息等基础能力；不写内部 Broker，不启动任何 MQ 服务。

**Architecture:** 纯 API 用法展示：方法签名与语义对齐 RocketMQ 常见生产端 API（`send` / 顺序选择器 / `sendMessageInTransaction` 半消息 / `setDelayTimeLevel`），实现体内用几行内存状态或直接打印说明，**不做**队列位点仿真。`main` 只演示调用方式。

**Tech Stack:** Java 17, Maven（不新增依赖、不改 pom）

---

## File Structure

| 动作 | 路径 | 职责 |
|---|---|---|
| 删除 | `src/main/java/com/advancedjava/interview/mq/`（5 文件） | 旧三问仿真 + 断裂 Mock |
| 删除 | `src/test/java/com/advancedjava/interview/mq/`（2 文件） | 编译失败的测试 |
| 创建 | `src/main/java/com/advancedjava/mq/MqBasicApiDemo.java` | 类 1：`main` + 各 API 演示方法 |
| 创建 | `src/main/java/com/advancedjava/mq/RocketmqApiStyle.java` | 类 2：标准 API 风格的方法外壳（静态方法，展示签名与语义） |

不创建测试类、不创建 Broker/Producer/Consumer 多类体系（用户明确只要两个类）。

---

### Task 1: 删除旧包

**Files:**
- Delete: `src/main/java/com/advancedjava/interview/mq/`
- Delete: `src/test/java/com/advancedjava/interview/mq/`

- [ ] **Step 1: 删除目录**

```bash
rm -rf src/main/java/com/advancedjava/interview/mq \
       src/test/java/com/advancedjava/interview/mq
```

- [ ] **Step 2: 确认无 Java 引用残留**

Run: `rg -n 'interview\.mq|MqReliability|MqAbstraction|MockProducer|MockConsumer' --glob '*.java' || true`
Expected: 无输出

---

### Task 2: 创建 `RocketmqApiStyle`（类 2 · 标准 API 方法外壳）

**Files:**
- Create: `src/main/java/com/advancedjava/mq/RocketmqApiStyle.java`

- [ ] **Step 1: 写入完整文件**

```java
package com.advancedjava.mq;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RocketMQ 常见生产端 API 的「用法风格」展示（静态方法，不连接 Broker、不需启动服务）。
 *
 * <p>命名与语义对齐真实客户端惯用法，便于对照官方文档：
 * <ul>
 *   <li>{@link #send} —— 普通消息；</li>
 *   <li>{@link #sendOrdered} —— 顺序消息（MessageQueueSelector：同 key 选同一队列）；</li>
 *   <li>{@link #sendMessageInTransaction} —— 事务消息：半消息 + 本地事务；</li>
 *   <li>{@link #sendDelay} —— 延迟消息（setDelayTimeLevel 语义）；</li>
 *   <li>{@link #pull} / {@link #ack} —— 消费侧最小示意（至少一次）。</li>
 * </ul>
 *
 * <p>实现仅用内存 Map 保住「能打印的演示结果」，刻意不做 Offset/Rebalance 等 Broker 细节。
 */
public final class RocketmqApiStyle {

    /** 演示用消息（等价于客户端 Message 的常用字段）。 */
    public record DemoMessage(String msgId, String topic, String tags, String keys, String body) {
    }

    /** 事务状态：对应本地事务执行结果。 */
    public enum LocalTransactionState { COMMIT_MESSAGE, ROLLBACK_MESSAGE, UNKNOW }

    /** 进程内“已发出/已可见”的消息列表，仅供演示打印，不是 Broker。 */
    private final Map<String, DemoMessage> sent = new LinkedHashMap<>();
    private final List<DemoMessage> visible = new ArrayList<>();
    private final AtomicInteger idSeq = new AtomicInteger();
    private int delayLevelOfLastSend;

    // ---------- 1. 普通消息 ----------

    /** 普通发送：producer.send(message)。 */
    public String send(String topic, String tags, String keys, String body) {
        String msgId = nextId("MSG");
        DemoMessage msg = new DemoMessage(msgId, topic, tags, keys, body);
        sent.put(msgId, msg);
        visible.add(msg);
        return msgId;
    }

    // ---------- 2. 顺序消息 ----------

    /**
     * 顺序发送：真实 API 为 {@code producer.send(msg, selector, arg)}。
     * 这里用等价的 keys 哈希选队列，展示「同业务键 → 同队列 → FIFO」。
     *
     * @param queueNums 模拟队列数（真实场景 = topic 的 queue 数）
     * @return 被选中的队列下标（演示顺序性时打印用）
     */
    public int sendOrdered(String topic, String tags, String keys, String body, int queueNums) {
        if (queueNums <= 0) {
            throw new IllegalArgumentException("queueNums 必须为正数: " + queueNums);
        }
        // MessageQueueSelector 经典实现：Math.abs(keys.hashCode()) % size
        int queueId = Math.floorMod(keys.hashCode(), queueNums);
        String msgId = nextId("MSG-ORDERED");
        sent.put(msgId, new DemoMessage(msgId, topic, tags, keys, body));
        return queueId;
    }

    // ---------- 3. 事务消息 + 半消息 ----------

    /**
     * 事务消息：{@code producer.sendMessageInTransaction(msg, listener, arg)} 两阶段语义。
     *
     * <ol>
     *   <li>先落「半消息」（对消费者不可见）；</li>
     *   <li>执行本地事务，按返回值 commit / rollback；</li>
     *   <li>UNKNOW → 真实场景等 Broker 回查 {@link #checkLocalTransaction}。</li>
     * </ol>
     *
     * @return 二阶段结果说明（半消息 ID 在日志语义里，这里返回状态名便于断言打印）
     */
    public LocalTransactionState sendMessageInTransaction(
            String topic, String tags, String keys, String body,
            boolean localTxSuccess) {
        String halfId = nextId("MSG-HALF");
        // 半消息：已记录但不加入 visible —— 消费者不可见
        sent.put(halfId, new DemoMessage(halfId, topic, tags, keys, body));

        LocalTransactionState state = localTxSuccess
                ? LocalTransactionState.COMMIT_MESSAGE
                : LocalTransactionState.ROLLBACK_MESSAGE;
        return finishHalf(halfId, state);
    }

    /**
     * Broker 回查：真实 API 为 TransactionListener#executeLocalTransaction 的对偶方法
     * {@code checkLocalTransaction}。演示：回查时按“本地事务表”补 commit。
     */
    public LocalTransactionState checkLocalTransaction(String halfMessageId, boolean localTxCommitted) {
        LocalTransactionState state = localTxCommitted
                ? LocalTransactionState.COMMIT_MESSAGE
                : LocalTransactionState.ROLLBACK_MESSAGE;
        return finishHalf(halfMessageId, state);
    }

    private LocalTransactionState finishHalf(String halfId, LocalTransactionState state) {
        DemoMessage half = sent.get(halfId);
        if (half == null || !half.msgId().startsWith("MSG-HALF")) {
            throw new IllegalArgumentException("未知半消息: " + halfId);
        }
        if (state == LocalTransactionState.COMMIT_MESSAGE) {
            // 半消息转正 → 消费者可见
            if (!containsId(visible, halfId)) {
                visible.add(half);
            }
        }
        // ROLLBACK / UNKNOW：保持不在 visible 中（丢弃或继续等回查）
        return state;
    }

    /** 演示用：当前消费者可见消息数（半消息/已回滚不计入）。 */
    public int visibleCount() {
        return visible.size();
    }

    /** 演示用：可见消息快照。 */
    public List<DemoMessage> visibleMessages() {
        return List.copyOf(visible);
    }

    /** 按 msgId 查询是否仍为半消息未决（HALF 且未 commit）。 */
    public boolean isHalfPending(String msgId) {
        DemoMessage m = sent.get(msgId);
        return m != null && m.msgId().startsWith("MSG-HALF") && !containsId(visible, msgId);
    }

    // ---------- 4. 延迟消息 ----------

    /**
     * 延迟消息：真实 API 为 {@code message.setDelayTimeLevel(level)} 后 send。
     * level 1~18 对应 RocketMQ 固定延迟档（1s 5s 10s 30s 1m ... 2h）；level 0 = 不延迟。
     *
     * @param delayLevel 1~18；0 表示不延迟
     * @return 消息 ID；同时记录最近一次 delayLevel 供演示打印
     */
    public String sendDelay(String topic, String tags, String keys, String body, int delayLevel) {
        if (delayLevel < 0 || delayLevel > 18) {
            throw new IllegalArgumentException("delayLevel 应在 0~18: " + delayLevel);
        }
        this.delayLevelOfLastSend = delayLevel;
        String msgId = nextId("MSG-DELAY-L" + delayLevel);
        DemoMessage msg = new DemoMessage(msgId, topic, tags, keys, body);
        sent.put(msgId, msg);
        if (delayLevel == 0) {
            visible.add(msg);
        }
        // delayLevel > 0：真实 Broker 会进延迟队列，到点才投递；
        // 这里不模拟时间轮，仅标记——演示侧重 API 用法而非调度器。
        return msgId;
    }

    public int delayLevelOfLastSend() {
        return delayLevelOfLastSend;
    }

    // ---------- 5. 消费侧最小示意（至少一次） ----------

    /**
     * 拉取消息（Pull API 风格）：返回当前全部可见消息。
     * 真实场景还有 push 模式（DefaultMQPushConsumer），此处仅示意。
     */
    public List<DemoMessage> pull() {
        return List.copyOf(visible);
    }

    /**
     * ACK 示意：真实场景由 offset 提交完成；此处用集合移除表达「确认后不再投递」。
     * 不调用 ack 时再次 pull 仍能看到 → 至少一次语义。
     *
     * @return 是否成功确认
     */
    public boolean ack(String msgId) {
        return visible.removeIf(m -> m.msgId().equals(msgId));
    }

    private String nextId(String prefix) {
        return prefix + "-" + idSeq.incrementAndGet();
    }

    private static boolean containsId(List<DemoMessage> list, String msgId) {
        for (DemoMessage m : list) {
            if (m.msgId().equals(msgId)) {
                return true;
            }
        }
        return false;
    }
}
```

- [ ] **Step 2: 编译**

Run: `mvn -q compile`
Expected: BUILD SUCCESS

---

### Task 3: 创建 `MqBasicApiDemo`（类 1 · main 展示入口）

**Files:**
- Create: `src/main/java/com/advancedjava/mq/MqBasicApiDemo.java`

- [ ] **Step 1: 写入完整文件**

```java
package com.advancedjava.mq;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MQ 基础 API 展示入口（不启动任何 MQ 服务）。
 *
 * <p>两个文件分工：
 * <ul>
 *   <li>{@link RocketmqApiStyle} —— 标准 API 方法外壳；</li>
 *   <li>本类 —— main 依次调用并打印：普通 / 顺序 / 事务半消息 / 延迟 / 至少一次，
 *       外加 RocketMQ vs Kafka 能力对比方法。</li>
 * </ul>
 */
public final class MqBasicApiDemo {

    public static void main(String[] args) {
        demoNormalSend();
        demoOrdering();
        demoTransactionAndHalfMessage();
        demoDelayMessage();
        demoAtLeastOnce();
        demoRocketmqVsKafka();
    }

    static void demoNormalSend() {
        System.out.println("\n=== 1. 普通消息 send ===");
        RocketmqApiStyle mq = new RocketmqApiStyle();
        String id = mq.send("TOPIC_ORDER", "created", "ORDER-1", "订单已创建");
        System.out.println("  send → " + id);
        System.out.println("  pull → " + bodies(mq.pull()));
        mq.ack(id);
        System.out.println("  ack 后 pull → " + bodies(mq.pull()));
    }

    static void demoOrdering() {
        System.out.println("\n=== 2. 顺序消息 sendOrdered（同 keys 同队列） ===");
        RocketmqApiStyle mq = new RocketmqApiStyle();
        int q1 = -1;
        for (String e : List.of("CREATED", "PAID", "SHIPPED", "COMPLETED")) {
            int q = mq.sendOrdered("TOPIC_ORDER_STATUS", e, "ORDER-A", "ORDER-A → " + e, 4);
            if (q1 < 0) {
                q1 = q;
            }
            System.out.printf("  %s → queue=%d%s%n", e, q, q == q1 ? "" : "  ← 不应出现");
        }
        System.out.println("  四个事件全部落入 queue=" + q1 + "，队列内 FIFO 即业务顺序");
        System.out.println("  Kafka 对照：等价于用 OrderId 做 partition key，单分区内有序");
    }

    static void demoTransactionAndHalfMessage() {
        System.out.println("\n=== 3. 事务消息 sendMessageInTransaction + 半消息 ===");
        RocketmqApiStyle mq = new RocketmqApiStyle();

        System.out.println("  ▶ 本地事务成功");
        int before = mq.visibleCount();
        RocketmqApiStyle.LocalTransactionState ok =
                mq.sendMessageInTransaction("TOPIC_ORDER", "created", "ORDER-TX-A", "下单成功", true);
        System.out.println("    结果=" + ok + "，可见消息 " + before + " → " + mq.visibleCount()
                + "（半消息已转正）");

        System.out.println("  ▶ 本地事务回滚");
        RocketmqApiStyle.LocalTransactionState rb =
                mq.sendMessageInTransaction("TOPIC_ORDER", "created", "ORDER-TX-B", "下单失败", false);
        System.out.println("    结果=" + rb + "，可见消息仍为 " + mq.visibleCount()
                + "（半消息丢弃，下游无感知）");

        System.out.println("  ▶ 二阶段确认丢失 → checkLocalTransaction 回查");
        RocketmqApiStyle mq2 = new RocketmqApiStyle();
        // 先制造一条“卡住的半消息”：走事务入口但模拟 commit 丢失——
        // 直接用回查 API 演示补提交路径
        RocketmqApiStyle.LocalTransactionState halfState =
                mq2.sendMessageInTransaction("TOPIC_ORDER", "created", "ORDER-TX-C", "回查演示", false);
        System.out.println("    半消息未决时可见数=" + mq2.visibleCount() + "，rollback 语义=" + halfState);
        // 再演示：若本地其实已提交，回查补 commit
        String halfId = "MSG-HALF-1";
        RocketmqApiStyle.LocalTransactionState checked =
                mq2.checkLocalTransaction(halfId, true);
        System.out.println("    checkLocalTransaction(" + halfId + ", 已提交) → " + checked
                + "，可见数=" + mq2.visibleCount());
        System.out.println("  要点：半消息解决「本地事务与发消息的原子性」；Kafka 原生不具备，需外挂协调");
    }

    static void demoDelayMessage() {
        System.out.println("\n=== 4. 延迟消息 setDelayTimeLevel ===");
        RocketmqApiStyle mq = new RocketmqApiStyle();
        String id = mq.sendDelay("TOPIC_DELAY", "timeout", "ORDER-1", "30 分钟未支付关单", 6);
        System.out.println("  sendDelay(level=6) → " + id
                + "，level=" + mq.delayLevelOfLastSend());
        System.out.println("  RocketMQ 固定档：1s,5s,10s,30s,1m,2m,3m,...,2h（level=6 ≈ 2m）");
        System.out.println("  到点前不投递；Kafka 无原生延迟队列，需应用层或外部组件");
    }

    static void demoAtLeastOnce() {
        System.out.println("\n=== 5. 至少一次：业务成功但未 ack → 重复可见 ===");
        RocketmqApiStyle mq = new RocketmqApiStyle();
        String id = mq.send("TOPIC_STOCK", "deduct", "ORDER-1", "SKU-A x5");
        List<RocketmqApiStyle.DemoMessage> first = mq.pull();
        System.out.println("  第1轮 pull: " + first.get(0).msgId() + "（处理成功，模拟 ACK 丢失）");
        List<RocketmqApiStyle.DemoMessage> second = mq.pull();
        System.out.println("  第2轮 pull: " + second.get(0).msgId()
                + "，同一条=" + first.get(0).msgId().equals(second.get(0).msgId()));
        mq.ack(id);
        System.out.println("  ack 后可见=" + mq.pull().size() + " → 消费端要用业务键做幂等");
    }

    static void demoRocketmqVsKafka() {
        System.out.println("\n=== 6. RocketMQ vs Kafka 基础能力 ===");
        System.out.printf("  %-18s %-10s %-10s%n", "能力", "RocketMQ", "Kafka");
        for (Feature f : Feature.values()) {
            System.out.printf("  %-18s %-10s %-10s%n", f.label,
                    rocketmqSupports(f) ? "原生支持" : "不支持",
                    kafkaSupports(f) ? "原生支持" : "不支持");
        }
        System.out.println("  选型：事务/延迟/消费重试/业务顺序 → RocketMQ；高吞吐日志大数据 → Kafka");
    }

    /** 对比维度（方法形式的“表格行”）。 */
    enum Feature {
        ORDERED("顺序消息"),
        TRANSACTION("事务消息"),
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
            case ORDERED, TRANSACTION, DELAY, CONSUMER_RETRY, SERVER_FILTER -> true;
            case HUGE_BACKLOG -> false;
        };
    }

    static boolean kafkaSupports(Feature f) {
        return switch (f) {
            case ORDERED, HUGE_BACKLOG -> true;
            case TRANSACTION, DELAY, CONSUMER_RETRY, SERVER_FILTER -> false;
        };
    }

    private static List<String> bodies(List<RocketmqApiStyle.DemoMessage> msgs) {
        return msgs.stream().map(RocketmqApiStyle.DemoMessage::body).collect(Collectors.toList());
    }

    private MqBasicApiDemo() {
    }
}
```

- [ ] **Step 2: 编译并运行**

Run: `mvn -q compile && java -cp target/classes com.advancedjava.mq.MqBasicApiDemo`
Expected: 6 节输出，退出码 0，无异常

---

### Task 4: 全量验证

- [ ] **Step 1: 全量测试（删除旧失败测试后应通过）**

Run: `mvn -q test`
Expected: BUILD SUCCESS

- [ ] **Step 2: 确认旧引用清除、新类仅两个**

Run: `rg -l 'interview\.mq|MqReliability|MqAbstraction' --glob '*.java' || true`  
Run: `ls src/main/java/com/advancedjava/mq/`
Expected: 前者无输出；后者仅 `MqBasicApiDemo.java`、`RocketmqApiStyle.java`

---

## Self-Review

1. **Spec:** 移出 interview ✓；恰好两 class ✓；顺序/事务/半消息/延迟 API 展示 ✓；对比 supports 方法 ✓；无内部 Broker（无 queue offset 仿真架构，仅 LinkedHashMap/List 存演示数据）✓；不启动服务、不改 pom ✓；删除坏测试 ✓。
2. **Placeholder:** 无 TBD，两文件均为完整代码。
3. **类型一致:** `send/sendOrdered/sendMessageInTransaction/checkLocalTransaction/sendDelay/pull/ack/visibleCount/state` 在 Demo 与 Style 间签名一致；`LocalTransactionState`、`Feature` 枚举名一致。
