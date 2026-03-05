package com.advancedjava.interview.concurrency;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

/**
 * ConcurrentHashMap compute 原子更新示例（防超卖）。
 *
 * 学习点：
 * 1. 并发更新同一个 key 时，使用 compute/merge 可以把读改写合并为原子操作。
 * 2. 避免“先 get 再 put”导致的竞态条件。
 */
public final class ConcurrentHashMapComputeDemo {

    private ConcurrentHashMapComputeDemo() {
    }

    public static void run() throws InterruptedException {
        System.out.println("\n=== 11) ConcurrentHashMap compute 原子扣库存 ===");
        ConcurrentHashMap<String, Integer> inventory = new ConcurrentHashMap<>();
        inventory.put("java-book", 30);

        int buyers = 80;
        CountDownLatch done = new CountDownLatch(buyers);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        LongAdder success = new LongAdder();
        LongAdder fail = new LongAdder();

        for (int i = 0; i < buyers; i++) {
            pool.submit(() -> {
                try {
                    inventory.compute("java-book", (sku, stock) -> {
                        if (stock == null || stock <= 0) {
                            fail.increment();
                            return stock;
                        }
                        success.increment();
                        return stock - 1;
                    });
                } finally {
                    done.countDown();
                }
            });
        }

        done.await();
        pool.shutdown();
        System.out.printf("success=%d, fail=%d, finalStock=%d%n",
                success.sum(), fail.sum(), inventory.get("java-book"));
    }
}
