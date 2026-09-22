package com.advancedjava.redis;

import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * 演示 4：Lua CAS（Compare-And-Swap）原子更新。
 *
 * Redis 原生没有 CAS 命令。传统做法是 WATCH/MULTI/EXEC 乐观事务，命令多、要写重试；
 * 用 Lua 一个脚本"读 + 比较 + 写"就原子完成。
 * 典型场景：带版本号的配置发布——只允许 v1 -> v2 链式推进，防止并发下旧值回退覆盖新值。
 *
 * 【部署形态提示】Cluster 模式：单 key，天然同槽，零改动。
 * 主从注意：CAS 的"读+比较+写"必须全部发生在 master——若 GET 走了 replica，
 * 复制延迟会让客户端拿着旧版本号去 CAS，master 上其实已被别人推进过，
 * 此时脚本比对失败返回 0（安全），但更糟的读写分离配置会把 replica 的旧值当现值写回。
 * 结论：CAS 类命令严禁读写分离，一律 master 执行（Jedis Cluster 对写命令自动路由 master）。
 *
 * 运行：mvn exec:java -Dexec.mainClass=com.advancedjava.redis.CasUpdateLuaDemo
 */
public class CasUpdateLuaDemo {

    /** KEYS[1]=目标key ARGV=[期望旧值, 新值]；返回 1=成功 0=当前值不匹配 */
    private static final String CAS_SCRIPT = """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                redis.call('set', KEYS[1], ARGV[2])
                return 1
            else
                return 0
            end
            """;

    public static void main(String[] args) {
        try (Jedis jedis = new Jedis("127.0.0.1", 6379)) {
            String key = "demo:config:version";
            jedis.set(key, "v1");

            // 拿错误的旧值去改：应被拒绝，且不动原值
            System.out.println("CAS(v0 -> v9): " + jedis.eval(CAS_SCRIPT, List.of(key), List.of("v0", "v9"))
                    + "（0=拒绝）");

            // 正确推进：v1 -> v2
            System.out.println("CAS(v1 -> v2): " + jedis.eval(CAS_SCRIPT, List.of(key), List.of("v1", "v2"))
                    + "（1=成功）");

            // 再拿 v1 去改：值已推进到 v2，拒绝，防止回退覆盖
            System.out.println("CAS(v1 -> v3): " + jedis.eval(CAS_SCRIPT, List.of(key), List.of("v1", "v3"))
                    + "（0=拒绝，已不是 v1）");

            System.out.println("最终值: " + jedis.get(key) + "（只有链式 v1->v2 生效）");
        }
    }
}
