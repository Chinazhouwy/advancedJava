package com.advancedjava.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.params.XReadGroupParams;
import redis.clients.jedis.resps.StreamEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 演示 7：Redis Stream——像 Kafka 一样的消息队列（5.0 起的核心数据结构）。
 *
 * 和 List 轮询、Pub/Sub 的区别（面试常考三者对比）：
 * - Pub/Sub：发完即走，订阅者不在线就永久丢消息，无积压能力；
 * - List + BRPOP：消费即删除，处理失败消息就没了，也没法多消费者分组；
 * - Stream：消息持久堆积（XADD 追加），每条自动分配递增 ID（<毫秒>-<序号>）；
 *   消费组（XGROUP/XREADGROUP）把消息分给组内不同消费者，同一条只投一次；
 *   XACK 确认才算完成，没 ACK 的挂在 PEL（Pending Entries List）里，
 *   可用 XPENDING 查看、XAUTOCLAIM 让别的消费者接管重试——
 *   "至少一次投递 + 崩溃恢复"就是它当轻量 MQ 的本钱。
 *
 * 【部署形态提示】Cluster：Stream 所有命令只碰一个 key，天然同槽零改动；
 * 消费组元数据存在流内部，failover 后随数据复制到新主，消费者重连继续即可。
 *
 * 运行：mvn exec:java -Dexec.mainClass=com.advancedjava.redis.StreamConsumeDemo
 */
public class StreamConsumeDemo {

    private static final String STREAM = "demo:stream:orders";
    private static final String GROUP = "demo-group";

    public static void main(String[] args) throws InterruptedException {
        try (Jedis jedis = new Jedis("127.0.0.1", 6379)) {
            jedis.del(STREAM);

            // ---------- 1) 生产：XADD 追加三条订单消息 ----------
            for (int i = 1; i <= 3; i++) {
                Map<String, String> body = new HashMap<>();
                body.put("orderId", "ORD-" + i);
                body.put("amount", String.valueOf(i * 10));
                // null ID = 服务端自动生成 <毫秒时间戳>-<序号>，保证单调递增
                StreamEntryID id = jedis.xadd(STREAM, (StreamEntryID) null, body);
                System.out.println("XADD -> " + id + "  " + body);
            }
            System.out.println("流长度: " + jedis.xlen(STREAM));

            // ---------- 2) 建消费组，从流的开头(0-0)开始消费 ----------
            // mkstream=true：流不存在时顺带创建
            System.out.println("XGROUP CREATE: "
                    + jedis.xgroupCreate(STREAM, GROUP, new StreamEntryID("0-0"), true));

            // ---------- 3) 两个消费者按 ">" 领取新消息 ----------
            // ">" 的含义：领取组内还没投递给我的任何消息（区别于具体 ID = 重读自己的 PEL）
            consume(jedis, "worker-A");
            consume(jedis, "worker-B");
            // 三条消息按到达顺序分给两个消费者——A 领过的不会重复投递给 B（组内分发，不是广播）

            // ---------- 4) 观察 PEL：领了还没 ACK 的消息都挂在待确认列表里 ----------
            var summary = jedis.xpending(STREAM, GROUP);
            System.out.println("XPENDING 汇总: 总数=" + summary.getTotal()
                    + ", 范围=[" + summary.getMinId() + " ~ " + summary.getMaxId() + "]"
                    + ", 按消费者=" + summary.getConsumerMessageCount());

            // ---------- 5) ACK：确认消费完成，从 PEL 移除 ----------
            if (FIRST_IDS[0] != null) {
                long acked = jedis.xack(STREAM, GROUP, FIRST_IDS[0]);
                System.out.println("XACK worker-A 的第一条(" + FIRST_IDS[0] + "): " + acked + "（1=移除）");
                System.out.println("ACK 后再看 XPENDING 总数: " + jedis.xpending(STREAM, GROUP).getTotal());
            }

            System.out.println("\n说明：两个消费者把 3 条消息分了——同一条只会投给组内一个消费者（不是广播）。");
            System.out.println("若消费者读取后未 XACK 就宕机，消息留在它的 PEL 里；其他消费者可用 "
                    + "XAUTOCLAIM（空闲超阈值自动接管）或 XPENDING+XCLAIM 手动领取重试，"
                    + "这就是 at-least-once 投递。上面 worker-B 领到却未 ACK 的那条就处于这种挂账状态。");
        }
    }

    /** 记录 A 领到的第一条消息 ID，供第 5 步 ACK 用。 */
    private static final StreamEntryID[] FIRST_IDS = new StreamEntryID[1];

    private static void consume(Jedis jedis, String consumer) {
        // 关键：ID 必须是 ">"（未投递的新消息），即 StreamEntryID.XREADGROUP_UNDELIVERED_ENTRY。
        // 传 new StreamEntryID() 会序列化成空字符串，服务端当成"已投递位置"从而读不到任何消息。
        Map<String, StreamEntryID> streams = Map.of(STREAM, StreamEntryID.XREADGROUP_UNDELIVERED_ENTRY);
        // block=100ms：没有新消息最多等 100 毫秒就返回
        // count(2)：一次只领 2 条，好让另一条分给下一个消费者，体现组内分发
        List<Map.Entry<String, List<StreamEntry>>> result = jedis.xreadGroup(
                GROUP, consumer, XReadGroupParams.xReadGroupParams().count(2).block(100), streams);
        if (result == null || result.isEmpty()) {
            System.out.println(consumer + ": 无新消息");
            return;
        }
        boolean gotAny = false;
        for (Map.Entry<String, List<StreamEntry>> stream : result) {
            for (StreamEntry e : stream.getValue()) {
                gotAny = true;
                System.out.println(consumer + " 领到 " + e.getID() + " " + e.getFields());
                if ("worker-A".equals(consumer) && FIRST_IDS[0] == null) {
                    FIRST_IDS[0] = e.getID();
                }
            }
        }
        if (!gotAny) {
            System.out.println(consumer + ": 无新消息");
        }
    }
}
