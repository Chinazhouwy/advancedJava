package com.advancedjava.interview.mq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MQ 抽象层：统一 Producer / Consumer 接口，支持 Mock / RocketMQ / Kafka 三种实现。
 *
 * <p>设计原则：
 * <ul>
 *   <li>业务代码只依赖本包中的接口，不直接耦合具体 MQ；</li>
 *   <li>Mock 实现默认启用，无需外部服务即可运行所有演示；</li>
 *   <li>切换到真实 MQ 只需在 {@link MqConfig} 中修改开关。</li>
 * </ul>
 */
public final class MqAbstractionLayer {

    /** MQ 配置开关。 */
    public static class MqConfig {
        /** 使用 Mock 实现（默认 true，无需启动任何服务）。 */
        public static boolean useMock = true;
        /** 目标 Topic（Mock/RocketMQ/Kafka 通用）。 */
        public static String orderTopic = "TOPIC_ORDER_STATUS";
        public static String stockTopic = "TOPIC_STOCK_DEDUCT";
        /** RocketMQ 相关（仅当 useMock=false 时生效）。 */
        public static String rocketmqGroup = "ORDER_CONSUMER_GROUP";
        public static String rocketmqNamesrv = "127.0.0.1:9876";
        /** Kafka 相关（仅当 useMock=false 且 mqType=kafka 时生效）。 */
        public static String kafkaBrokers = "localhost:9092";
        public static String kafkaGroupId = "order_consumer_group";
    }

    /** 消息对象抽象。 */
    public record Message(String messageId, String topic, String tag, String businessKey, String body) {
    }

    /** 内部迷你 Broker：模拟真实 MQ 的队列 + 位点管理。 */
    public static class MiniBroker {
        private final Map<Integer, List<Entry>> queues = new ConcurrentHashMap<>();
        private final Map<Integer, AtomicInteger> offsets = new ConcurrentHashMap<>();
        private final AtomicInteger messageSeq = new AtomicInteger();
        private final int queueCount;

        public record Entry(Message message) {}

        MiniBroker(int queueCount) {
            this.queueCount = queueCount;
            for (int i = 0; i < queueCount; i++) {
                queues.put(i, new ArrayList<>());
                offsets.put(i, new AtomicInteger(0));
            }
        }

        public static MiniBroker newInstance(int queueCount) {
            return new MiniBroker(queueCount);
        }

        String sendSync(String topic, String tag, String businessKey, String body) {
            int queue = Math.abs(businessKey.hashCode()) % queueCount;
            String messageId = "MSG-" + messageSeq.incrementAndGet();
            Message msg = new Message(messageId, topic, tag, businessKey, body);
            queues.computeIfAbsent(queue, k -> new ArrayList<>()).add(new Entry(msg));
            System.out.printf("    [MiniBroker] 发送消息：%s (topic=%s, queue=%d)%n", messageId, topic, queue);
            return messageId;
        }

        List<Message> pull(int queue, int maxNum) {
            List<Entry> entries = queues.get(queue);
            if (entries == null) return List.of();
            int offset = offsets.get(queue).get();
            List<Message> result = new ArrayList<>();
            for (int i = offset; i < entries.size() && result.size() < maxNum; i++) {
                result.add(entries.get(i).message());
            }
            return result;
        }

        void ack(int queue, String messageId) {
            List<Entry> entries = queues.get(queue);
            if (entries == null) return;
            int finalIndex = -1;
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).message().messageId().equals(messageId)) {
                    finalIndex = i;
                    break;
                }
            }
            if (finalIndex >= 0) {
                int idx = finalIndex + 1;
                offsets.get(queue).updateAndGet(v -> Math.max(v, idx));
                System.out.printf("    [MiniBroker] ACK 成功：%s (offset=%d → %d)%n",
                        messageId, finalIndex, offsets.get(queue).get());
            }
        }

        int backlog(int queue) {
            List<Entry> entries = queues.get(queue);
            if (entries == null) return 0;
            int offset = offsets.get(queue).get();
            return Math.max(0, entries.size() - offset);
        }
    }

    /** 生产者接口。 */
    public interface Producer {
        /**
         * 发送普通消息。
         * @return 消息 ID（Mock 模式下生成唯一 ID，真实模式下由 Broker 分配）
         */
        String sendSync(String topic, String tag, String businessKey, String body);

        /**
         * 发送事务消息（RocketMQ 特有语义）。
         * @return 半消息 ID
         */
        String sendHalf(String topic, String tag, String businessKey, String body);

        /** 本地事务提交 → 半消息转正。 */
        void commit(String halfMessageId);

        /** 本地事务回滚 → 半消息丢弃。 */
        void rollback(String halfMessageId);
    }

    /** 消费者接口。 */
    public interface Consumer {
        /**
         * 订阅 Topic。
         * @param group 消费组
         * @param topic Topic 名称
         * @param filterExpression Tag 或 SQL 表达式
         */
        void subscribe(String group, String topic, String filterExpression);

        /**
         * 拉取一批消息并处理。
         * @return 是否至少有一条消息被成功处理
         */
        boolean pollAndHandle();

        /**
         * 确认当前批次的消息（ACK）。
         * @return 是否成功
         */
        boolean ack();

        /** 停止消费。 */
        void shutdown();
    }

    /**
     * 工厂类：根据配置返回对应的 Producer / Consumer。
     */
    public static class MqFactory {

        private static Producer producer;
        private static Consumer consumer;

        static {
            if (MqConfig.useMock) {
                producer = new MockProducer();
                consumer = new MockConsumer();
            } else {
                // TODO: 可拓展为真实 RocketMQ / Kafka 实现
                throw new UnsupportedOperationException("真实 MQ 实现待补充，当前仅支持 Mock 模式");
            }
        }

        public static Producer getProducer() {
            return producer;
        }

        public static Consumer getConsumer() {
            return consumer;
        }

        /** 切换为真实 MQ 实现（需自行实现 RocketMQKafkaAdapter）。 */
        public static void switchToRealMq() {
            MqConfig.useMock = false;
            // 重新初始化
            producer = null;
            consumer = null;
        }
    }
}
