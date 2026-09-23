package com.advancedjava.interview.mq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mock Producer：内存队列实现，模拟 RocketMQ / Kafka 的 API 行为。
 *
 * <p>关键特性：
 * <ul>
 *   <li>{@code sendSync} 按 businessKey 哈希路由到固定队列（顺序性根基）；</li>
 *   <li>{@code sendHalf} 生成半消息，对消费者不可见；</li>
 *   <li>位点管理在 Consumer 中，Producer 只负责发。</li>
 * </ul>
 */
public class MockProducer implements MqAbstractionLayer.Producer {

    /** 每个 Topic 一个队列映射（queueId → 待投递列表）。 */
    private final Map<String, Map<Integer, List<Entry>>> topicQueues = new ConcurrentHashMap<>();
    private final AtomicInteger messageIdSeq = new AtomicInteger(0);

    record Entry(MqAbstractionLayer.Message message) {
    }

    @Override
    public String sendSync(String topic, String tag, String businessKey, String body) {
        int queue = Math.abs(businessKey.hashCode()) % 4; // 默认 4 个队列
        String messageId = "MSG-" + messageIdSeq.incrementAndGet();
        MqAbstractionLayer.Message msg = new MqAbstractionLayer.Message(messageId, topic, tag, businessKey, body);
        topicQueues.computeIfAbsent(topic, k -> new HashMap<>()).computeIfAbsent(queue, k -> new ArrayList<>())
                .add(new Entry(msg));
        System.out.printf("    [MockProducer] 发送普通消息：%s (topic=%s, queue=%d)%n", messageId, topic, queue);
        return messageId;
    }

    @Override
    public String sendHalf(String topic, String tag, String businessKey, String body) {
        int queue = Math.abs(businessKey.hashCode()) % 4;
        String messageId = "MSG-HALF-" + messageIdSeq.incrementAndGet();
        MqAbstractionLayer.Message msg = new MqAbstractionLayer.Message(messageId, topic, tag, businessKey, body);
        // 半消息状态标记为 HALF（Consumer 拉取时跳过）
        topicQueues.computeIfAbsent(topic, k -> new HashMap<>()).computeIfAbsent(queue, k -> new ArrayList<>())
                .add(new Entry(msg));
        System.out.printf("    [MockProducer] 发送半消息：%s (topic=%s, queue=%d, state=HALF)%n", messageId, topic, queue);
        return messageId;
    }

    @Override
    public void commit(String halfMessageId) {
        transformState(halfMessageId, "HALF", "COMMITTED");
        System.out.printf("    [MockProducer] 提交半消息：%s → 可被消费%n", halfMessageId);
    }

    @Override
    public void rollback(String halfMessageId) {
        transformState(halfMessageId, "HALF", "DISCARDED");
        System.out.printf("    [MockProducer] 回滚半消息：%s → 丢弃%n", halfMessageId);
    }

    private void transformState(String messageId, String fromState, String toState) {
        for (Map.Entry<String, Map<Integer, List<Entry>>> q : topicQueues.entrySet()) {
            for (Map.Entry<Integer, List<Entry>> entry : q.getValue().entrySet()) {
                List<Entry> list = entry.getValue();
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).message().messageId().equals(messageId)) {
                        // 实际项目中这里会更新内部状态，Mock 模式下简化为打印日志
                        return;
                    }
                }
            }
        }
        throw new IllegalArgumentException("未知半消息：" + messageId);
    }

    /** 测试辅助：获取某 Topic 某队列的消息数。 */
    int backlog(String topic, int queue) {
        Map<Integer, List<Entry>> queues = topicQueues.get(topic);
        if (queues == null || !queues.containsKey(queue)) {
            return 0;
        }
        return queues.get(queue).size();
    }
}
