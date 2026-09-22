package com.advancedjava.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 演示 1：分布式锁的 Lua 原子解锁。
 *
 * 运行前提：本地有 Redis（docker run -d --name redis-demo -p 6379:6379 redis:7-alpine）。
 * 运行方式：先 mvn compile，再
 *   mvn exec:java -Dexec.mainClass=com.advancedjava.redis.DistributedLockLuaDemo
 *
 * 核心问题：解锁如果拆成"GET 判断持有者 + DEL"两条命令，中间锁可能刚好过期
 * 并被别的线程抢走，再去 DEL 就误删了别人的锁。
 * Lua 脚本让"读 + 判 + 删"在 Redis 单线程里一次跑完，天然原子。
 *
 * 【部署形态提示】Cluster 模式：本 Demo 只有 1 个 KEYS，天然同槽，脚本零改动；
 * 真正的坑在主从——锁写入 master 后异步复制给 replica，master 宕机时哨兵/集群
 * 把 replica 提为新主，那把还没同步出去的锁就"凭空消失"了（另一个客户端随即加锁成功，
 * 出现双主持锁）。要严谨互斥用 RedLock（多数派节点独立加锁），或接受小概率风险并配
 * fencing token（锁携带单调递增版本号，下游资源校验旧版本拒绝执行）。
 */
public class DistributedLockLuaDemo {

    /** 解锁脚本：校验持有者后才删除。 */
    private static final String UNLOCK_SCRIPT = """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            else
                return 0
            end
            """;

    public static void main(String[] args) {
        try (Jedis jedis = new Jedis("127.0.0.1", 6379)) {
            String lockKey = "demo:lock:order-42";
            String token = UUID.randomUUID().toString();   // 持有者标识

            // 加锁：SET NX EX 一条命令完成"占位 + 设过期"
            // （绝不能拆成 SETNX + EXPIRE，中间宕机会留下永不过期的死锁）
            String ok = jedis.set(lockKey, token, SetParams.setParams().nx().ex(30));
            System.out.println("加锁: " + ("OK".equals(ok) ? "成功, token=" + token.substring(0, 8) : "失败"));

            // 用错误 token 解锁：脚本里的 get 比对不通过 -> 返回 0，锁保持不动
            Object wrong = jedis.eval(UNLOCK_SCRIPT, List.of(lockKey), List.of("wrong-token"));
            System.out.println("错误 token 解锁: " + wrong + "（0=被拒绝，防误删他人锁）");

            // 正确 token 解锁：get 比对通过 -> DEL 返回 1
            Object right = jedis.eval(UNLOCK_SCRIPT, List.of(lockKey), List.of(token));
            System.out.println("正确 token 解锁: " + right + "（1=删除成功）");
            System.out.println("锁是否还存在: " + jedis.exists(lockKey) + "（false=已释放）");
        }
    }
}
