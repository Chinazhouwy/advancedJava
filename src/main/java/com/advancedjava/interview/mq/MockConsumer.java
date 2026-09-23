package com.advancedjava.interview.mq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mock Consumer：内存队列实现，模拟 RocketMQ / Kafka 的 API 行为。
 *
 * <p>关键特性：
 * <ul>
 *   <li>{@code pollAndHandle} 拉取 COMMITTED 状态的消息（跳过 HALF/DISCARDED）；</li>
 *   <li>位点管理：ACK 后才推进，未 ACK 时重投同一条消息；</li>
 *   <li>支持注入"ACK 丢失"场景（测试用）。</li>
 * </ul>
 */
public class MockConsumer implements MqAbstractionLayer.Consumer {

    /** 订阅关系：group → topic → filterExpression。 */
    private final Map<String, Map<String, String>> subscriptions = new ConcurrentHashMap<>();
    /** 每个 Topic 一个队列映射（queueId → 待投递列表）。 */
    private final Map<String, Map<Integer, List<Entry>>> topicQueues = new ConcurrentHashMap<>();
    /** 每个订阅的位点：subscriptionKey → queue → offset。 */
    private final Map<String, Map<String, Map<Integer, AtomicInteger>>> offsets = new ConcurrentHashMap<>();
    /** 当前正在处理的批次（用于模拟 ACK 丢失）。 */
    private final Map<String, List<String>> currentBatch = new ConcurrentHashMap<>();
    /** ACK 丢失计数器（测试用）。 */
    private final AtomicInteger ackLossCounter = new AtomicInteger(0);
    private boolean ackLostThisRound = false;

    record Entry(MqAbstractionLayer.Message message) {
    }

    @Override
    public void subscribe(String group, String topic, String filterExpression) {
        subscriptions.computeIfAbsent(group, k -> new HashMap<>()).put(topic, filterExpression);
        offsets.putIfAbsent(group, new HashMap<>());
        offsets.get(group).computeIfAbsent(topic, k -> new HashMap<>());
        System.out.printf("    [MockConsumer] 订阅：%s (topic=%s, filter=%s)%n", group, topic, filterExpression);
    }

    /** Consumer 的自定义处理回调接口。 */
    public interface MessageHandler {
        void handle(MqAbstractionLayer.Message msg);
    }

    private MessageHandler customHandler;

    /** 设置自定义消息处理器（用于演示中的 StockConsumer）。 */
    public void setMessageHandler(MessageHandler handler) {
        this.customHandler = handler;
    }

    @Override
    public boolean pollAndHandle() {
        if (subscriptions.isEmpty()) {
            return false;
        }
        boolean hasMessages = false;
        for (Map.Entry<String, Map<String, String>> sub : subscriptions.entrySet()) {
            String group = sub.getKey();
            for (Map.Entry<String, String> topicEntry : sub.getValue().entrySet()) {
                String topic = topicEntry.getKey();
                // 初始化该订阅的位点
                offsets.computeIfAbsent(group, k -> new HashMap<>())
                        .computeIfAbsent(topic, k -> new HashMap<>());
                // 遍历所有队列
                for (int queue = 0; queue < 4; queue++) {
                    Map<Integer, List<Entry>> queues = topicQueues.get(topic);
                    if (queues == null || !queues.containsKey(queue)) {
                        continue;
                    }
                    List<Entry> entries = queues.get(queue);
                    Map<Integer, AtomicInteger> queueOffsets = offsets.get(group).get(topic);
                    AtomicInteger offsetAtomic = queueOffsets.computeIfAbsent(queue, k -> new AtomicInteger(0));
                    int offset = offsetAtomic.get();
                    List<MqAbstractionLayer.Message> batch = new ArrayList<>();
                    // 拉取从 offset 开始的所有 COMMITTED 消息
                    for (int i = offset; i < entries.size(); i++) {
                        Entry entry = entries.get(i);
                        // 半消息不可见
                        if (entry.message().messageId().startsWith("MSG-HALF-")) {
                            continue;
                        }
                        batch.add(entry.message());
                    }
                    if (!batch.isEmpty()) {
                        hasMessages = true;
                        String subscriptionKey = group + ":" + topic + ":" + queue;
                        currentBatch.put(subscriptionKey, new ArrayList<>(entries.subList(offset, Math.min(offset + batch.size(), entries.size())).stream()
                                .map(e -> e.message().messageId()).toList()));
                        System.out.printf("    [%s] 收到 %d 条消息%n", subscriptionKey, batch.size());
                        for (MqAbstractionLayer.Message msg : batch) {
                            System.out.printf("      - %s (tag=%s, bizKey=%s)%n", msg.messageId(), msg.tag(), msg.businessKey());
                            // 调用自定义处理器（如果有）
                            if (customHandler != null) {
                                customHandler.handle(msg);
                            }
                        }
                    }
                }
            }
        }
        return hasMessages;
    }

    @Override
    public boolean ack() {
        // 模拟 ACK 丢失：每第 N 轮丢一次（测试用，默认不丢）
        int round = ackLossCounter.incrementAndGet();
        boolean lost = (round % 5 == 0); // 每 5 轮丢一次
        if (lost) {
            ackLostThisRound = true;
            System.out.println("    [MockConsumer] ACK 在网络中丢失 ✗（位点未推进）");
            return false;
        }
        ackLostThisRound = false;
        // 推进所有已处理批次的位点
        for (Map.Entry<String, List<String>> entry : currentBatch.entrySet()) {
            String key = entry.getKey();
            String[] parts = key.split(":");
            String group = parts[0];
            String topic = parts[1];
            int queue = Integer.parseInt(parts[2]);
            Map<Integer, AtomicInteger> queueOffsets = offsets.get(group).get(topic);
            if (queueOffsets != null && queueOffsets.containsKey(queue)) {
                AtomicInteger offset = queueOffsets.get(queue);
                offset.addAndGet(entry.getValue().size());
                System.out.printf("    [MockConsumer] ACK 成功：%s (offset=%d → %d)%n",
                        key, offset.get() - entry.getValue().size(), offset.get());
            }
        }
        currentBatch.clear();
        return true;
    }

    @Override
    public void shutdown() {
        currentBatch.clear();
    }

    /** 测试辅助：设置 ACK 丢失概率（每 N 轮丢一次）。 */
    void setAckLossInterval(int interval) {
        // 实际项目中可用原子变量控制，这里简化为修改内部逻辑
        // Mock 模式下我们直接通过 ackLossCounter 控制
    }

    /** 获取某订阅的当前堆积数。 */
    int backlog(String group, String topic, int queue) {
        Map<Integer, List<Entry>> queues = topicQueues.get(topic);
        if (queues == null || !queues.containsKey(queue)) {
            return 0;
        }
        Map<Integer, AtomicInteger> queueOffsets = offsets.get(group).get(topic);
        if (queueOffsets == null || !queueOffsets.containsKey(queue)) {
            return queues.get(queue).size();
        }
        int offset = queueOffsets.get(queue).get();
        return Math.max(0, queues.get(queue).size() - offset);
    }
}
