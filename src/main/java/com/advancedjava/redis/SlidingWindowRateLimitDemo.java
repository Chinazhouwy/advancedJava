package com.advancedjava.redis;

import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 演示 2：ZSET + Lua 滑动窗口限流。
 *
 * 核心问题：固定窗口计数器在切换瞬间可放行 2 倍流量（第 59 秒打满 + 新窗口第 1 秒再打满）。
 * 滑动窗口用 ZSET 记每个请求的时间戳点，"清理旧点 -> 计数 -> 判断 -> 记录新点"
 * 四步必须原子，拆开发送会超发——Lua 脚本一次执行完。
 *
 * 【部署形态提示】Cluster 模式：单 key，天然同槽，零改动。
 * 主从注意：限流计数只写 master、不往 replica 复制是正确行为（replica 上的读会略旧，
 * 但限流判定永远发生在 master）；若业务允许读写分离，可把"查询剩余配额"放 replica，
 * 放行判定仍走 master 的 EVAL。
 *
 * 运行：mvn exec:java -Dexec.mainClass=com.advancedjava.redis.SlidingWindowRateLimitDemo
 */
public class SlidingWindowRateLimitDemo {

    /** KEYS[1]=窗口key ARGV=[当前ms, 窗口ms, 上限, 请求id]；返回剩余配额，-1=拒绝 */
    private static final String RATE_LIMIT_SCRIPT = """
            local now = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local limit = tonumber(ARGV[3])
            -- 1. 清掉窗口左边界之前的旧请求点（左边界 = now - window）
            redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, now - window)
            -- 2. 剩下的点数就是最近一个窗口内的请求数
            local count = redis.call('ZCARD', KEYS[1])
            -- 3. 未超限则记录本次请求并放行，返回剩余配额
            if count < limit then
                redis.call('ZADD', KEYS[1], now, ARGV[4])
                redis.call('PEXPIRE', KEYS[1], window)
                return limit - count - 1
            end
            -- 4. 已达上限，拒绝
            return -1
            """;

    public static void main(String[] args) throws InterruptedException {
        try (Jedis jedis = new Jedis("127.0.0.1", 6379)) {
            String key = "demo:rate:api";
            jedis.del(key);

            // 规则：1 秒窗口内最多放行 5 个，连发 8 个请求
            for (int i = 1; i <= 8; i++) {
                long now = System.currentTimeMillis();
                Object remaining = jedis.eval(RATE_LIMIT_SCRIPT, List.of(key),
                        List.of(String.valueOf(now), "1000", "5", now + ":" + System.nanoTime()));
                long r = (Long) remaining;
                System.out.printf("请求 %d: %s%n", i, r >= 0 ? "放行（剩余配额 " + r + "）" : "拒绝（窗口内已满 5 个）");
            }

            // 等窗口滑过去，验证恢复放行
            TimeUnit.MILLISECONDS.sleep(1100);
            long now = System.currentTimeMillis();
            Object after = jedis.eval(RATE_LIMIT_SCRIPT, List.of(key),
                    List.of(String.valueOf(now), "1000", "5", now + ":" + System.nanoTime()));
            System.out.println("1.1 秒后再次请求: " + ((Long) after >= 0 ? "恢复放行" : "仍被拒绝")
                    + "（窗口滑过，旧点已清理）");
        }
    }
}
