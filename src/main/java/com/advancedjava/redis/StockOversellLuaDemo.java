package com.advancedjava.redis;

import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 演示 6：Lua 脚本防超卖 + Redis 7 FUNCTION 按名调用。
 *
 * 防超卖核心："查余量 -> 判断 -> 扣减" 三步拆开必超卖——余量 1 时两个线程
 * 同时读到 1、各自通过判断、各扣一次。单条 EVAL 让三步原子执行。
 *
 * 进阶：EVAL 每次都要把脚本文本发给服务端；FUNCTION LOAD 注册进 Redis 后
 * 用 FCALL 按名字调用，省流量、可版本化。本 Demo 两条路径都跑一遍。
 *
 * 【部署形态提示】Cluster 模式：扣减只有 1 个 KEYS，天然同槽，零改动；
 * 但 FUNCTION 库要在**每个 master 节点各加载一次**（FUNCTION LOAD 不跨节点传播，
 * FCALL 路由到哪个节点就要求那个节点注册过 inventory 库）。
 * 主从注意：库存扣减是强一致写，必须 master 执行——若读到 replica 的旧余量再判断，
 * 复制延迟窗口内的并发请求会双双通过检查造成超卖（比拆命令更隐蔽的竞态来源）。
 * 读写分离场景下，商品详情页可以读 replica，下单扣减一律打 master。
 *
 * 运行：mvn exec:java -Dexec.mainClass=com.advancedjava.redis.StockOversellLuaDemo
 */
public class StockOversellLuaDemo {

    /** KEYS[1]=库存key ARGV[1]=扣减数量；返回 >=0 余量 / -1 不足 / -2 商品不存在 */
    private static final String STOCK_DECR_SCRIPT = """
            local stock = redis.call('get', KEYS[1])
            if not stock then
                return -2
            end
            local need = tonumber(ARGV[1])
            if tonumber(stock) < need then
                return -1
            end
            return redis.call('decrby', KEYS[1], need)
            """;

    /** FUNCTION 版库源码：shebang 声明引擎 + register_function 注册（不能走 EVAL 调用）。
     *  注：Redis 7.0 的 API 是 redis.register_function(name, fn)；7.4+ 才有带 flags 的新签名。 */
    private static final String STOCK_FUNCTION_LIB = """
            #!lua name=inventory
            local function stockDecr(keys, args)
                local stock = redis.call('get', keys[1])
                if not stock then
                    return -2
                end
                local need = tonumber(args[1])
                if tonumber(stock) < need then
                    return -1
                end
                return redis.call('decrby', keys[1], need)
            end
            redis.register_function('stock_decr', stockDecr)
            """;

    public static void main(String[] args) throws InterruptedException {
        try (Jedis jedis = new Jedis("127.0.0.1", 6379)) {
            String sku = "demo:stock:sku-1";

            // ---------- 路径 1：EVAL 内联脚本，10 线程抢 5 件 ----------
            // 注意：Jedis 不是线程安全的，每个线程必须用自己的连接
            jedis.set(sku, "5");
            AtomicInteger success = new AtomicInteger();
            AtomicInteger rejected = new AtomicInteger();
            List<Thread> buyers = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                Thread t = new Thread(() -> {
                    try (Jedis own = new Jedis("127.0.0.1", 6379)) {
                        long r = (Long) own.eval(STOCK_DECR_SCRIPT, List.of(sku), List.of("1"));
                        if (r >= 0) {
                            success.incrementAndGet();
                        } else if (r == -1) {
                            rejected.incrementAndGet();
                        }
                    }
                }, "buyer-" + i);
                buyers.add(t);
                t.start();
            }
            for (Thread t : buyers) {
                t.join();
            }
            System.out.println("EVAL 抢购: 抢到 " + success.get() + " 人, 库存不足被拒 " + rejected.get()
                    + " 人, 最终余量 " + jedis.get(sku) + "（若拆成 GET+DECRBY 必然超卖）");

            // ---------- 路径 2：FUNCTION LOAD + FCALL 按名调用 ----------
            // 先清掉可能存在的旧库，再加载（重复 LOAD 会报 Function exists）
            try {
                jedis.functionDelete("inventory");
            } catch (Exception ignored) {
                // 库本来就不存在，忽略
            }
            jedis.functionLoad(STOCK_FUNCTION_LIB);
            jedis.set(sku, "3");
            // FCALL 函数名 1 <key> <数量>：按名调用已注册函数，不再发送脚本文本
            Object viaFcall = jedis.fcall("stock_decr", List.of(sku), List.of("1"));
            System.out.println("FCALL stock_decr 扣 1 件 -> 余量 " + viaFcall
                    + "（函数只注册一次，之后按名调用，省流量、可版本化）");
        }
    }
}
