package com.advancedjava.redis;

import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 演示 5：ZSET + Lua 延迟队列的原子领取。
 *
 * 典型场景：下单 30 分钟未支付自动关单。任务以 score=到期时间戳 放进 ZSET，
 * 消费时"查到期 -> 取出 -> 从队列删除"必须原子，否则两个消费者领到同一任务重复执行。
 * 领取动作把任务原子迁移进"处理中"队列，宕机后可按领取超时重投（崩溃恢复）。
 *
 * 【部署形态提示】Cluster 模式：本 Demo 一条 EVAL 带 2 个 KEYS（延迟队列 + 处理中队列），
 * 默认命名会落不同 slot -> CROSSSLOT 拒绝。解法是 hash tag 把它们钉进同一槽：
 *   {demo:delayq}:orders 与 {demo:delayq}:processing —— CRC16 只算花括号内，同槽后脚本照常原子
 *   （两个队列名都必须走 KEYS 声明传入，Cluster 禁止脚本触碰未声明的 key）。
 * 主从注意：领取 = ZADD+ZREM 写操作，必须走 master；多消费者并发领取的互斥由
 * master 单线程排队保证，replica 只提供"还剩多少任务"这类只读观测。
 *
 * 运行：mvn exec:java -Dexec.mainClass=com.advancedjava.redis.DelayQueueLuaDemo
 */
public class DelayQueueLuaDemo {

    /** KEYS=[延迟队列, 处理中队列] ARGV=[当前ms, 批量上限]；返回领到的任务列表 */
    private static final String CLAIM_SCRIPT = """
            local due = redis.call('ZRANGEBYSCORE', KEYS[1], '-inf', ARGV[1], 'LIMIT', 0, tonumber(ARGV[2]))
            for i = 1, #due do
                redis.call('ZADD', KEYS[2], ARGV[1], due[i])
                redis.call('ZREM', KEYS[1], due[i])
            end
            return due
            """;

    public static void main(String[] args) throws InterruptedException {
        try (Jedis jedis = new Jedis("127.0.0.1", 6379)) {
            String queue = "demo:delayq:orders";
            String processing = "demo:delayq:orders:processing";
            jedis.del(queue, processing);

            long now = System.currentTimeMillis();
            // 投递 3 个不同延迟的任务
            jedis.zadd(queue, now + 200, "close-order-A(200ms后)");
            jedis.zadd(queue, now + 400, "close-order-B(400ms后)");
            jedis.zadd(queue, now + 600, "close-order-C(600ms后)");
            System.out.println("已投递 3 个延迟任务（200/400/600ms）");

            // 立刻领取：全部未到期 -> 空列表
            System.out.println("立即领取: " + claim(jedis, queue, processing));

            TimeUnit.MILLISECONDS.sleep(300);
            System.out.println("300ms 后领取: " + claim(jedis, queue, processing) + "（只有 A 到期）");

            TimeUnit.MILLISECONDS.sleep(200);
            System.out.println("再过 200ms 领取: " + claim(jedis, queue, processing) + "（B 到期，C 还需等待）");

            System.out.println("剩余延迟队列长度: " + jedis.zcard(queue) + "（只剩未到期的 C）");
        }
    }

    private static Object claim(Jedis jedis, String queue, String processing) {
        return jedis.eval(CLAIM_SCRIPT, List.of(queue, processing),
                List.of(String.valueOf(System.currentTimeMillis()), "10"));
    }
}
