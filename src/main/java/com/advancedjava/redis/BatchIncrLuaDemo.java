package com.advancedjava.redis;

import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * 演示 3：一次 EVAL 原子批量累加 N 个计数器。
 *
 * 对比三种批量写法：
 * 1. 循环单条 INCRBY —— N 次网络往返；
 * 2. Pipeline —— 合并了网络包，但仍是 N 条独立命令、非原子；
 * 3. Lua 脚本 —— 1 次往返，且整批在 Redis 单线程内原子生效（同一时刻快照）。
 *
 * 【部署形态提示】Cluster 模式：本 Demo 是"多 key 一次 EVAL"的典型——N 个 key 大概率
 * 散落在不同 slot，服务端会直接报 CROSSSLOT。两种解法：
 *   a) hash tag 归槽：把计数器命名成 {demo:cnt}:pv / {demo:cnt}:uv / {demo:cnt}:click，
 *      CRC16 只对花括号内计算，三个 key 落进同一 slot，一次 EVAL 照常原子
 *      （Cluster 下脚本禁止触碰未声明的 key，所以前缀必须作为 KEYS 传入，不能写死在脚本里）；
 *   b) 客户端按槽分组：算出每个 key 的 slot，同槽一组、每组一次 EVAL（失去跨组原子性）。
 * 主从注意：计数只写 master，replica 读到的是复制延迟内的旧值，监控面板可接受即可。
 *
 * 运行：mvn exec:java -Dexec.mainClass=com.advancedjava.redis.BatchIncrLuaDemo
 */
public class BatchIncrLuaDemo {

    /** KEYS/ARGV 一一对应，返回每个 key 累加后的最新值列表 */
    private static final String BATCH_INCR_SCRIPT = """
            local results = {}
            for i = 1, #KEYS do
                results[i] = redis.call('INCRBY', KEYS[i], tonumber(ARGV[i]))
            end
            return results
            """;

    public static void main(String[] args) {
        try (Jedis jedis = new Jedis("127.0.0.1", 6379)) {
            List<String> keys = List.of("demo:cnt:pv", "demo:cnt:uv", "demo:cnt:click");
            jedis.del(keys.toArray(new String[0]));

            List<String> deltas = List.of("10", "3", "7");
            Object result = jedis.eval(BATCH_INCR_SCRIPT, keys, deltas);
            System.out.println("一次 EVAL 累加 " + deltas + " -> " + result);

            Object again = jedis.eval(BATCH_INCR_SCRIPT, keys, List.of("1", "1", "1"));
            System.out.println("再来一轮 [1,1,1]      -> " + again);
            System.out.println("全程只有 2 次网络往返（而不是 6 次），且每轮整批原子生效");
        }
    }
}
