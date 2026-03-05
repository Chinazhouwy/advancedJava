package com.advancedjava.interview.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Semaphore 舱壁隔离示例。
 *
 * 模拟场景：
 * 下游模型服务最多允许 3 个并发调用，如果直接放开会导致雪崩。
 * 这里用 Semaphore 控制同一时刻进入下游的请求数。
 */
public final class SemaphoreBulkheadDemo {

    private SemaphoreBulkheadDemo() {
    }

    public static void run() throws InterruptedException {
        System.out.println("\n=== 3) Semaphore 限流/舱壁 ===");

        /**
         * 这行代码创建了一个 信号量（Semaphore），用于实现舱壁隔离模式：
         * 核心作用：
         * new Semaphore(3) 创建一个初始许可数为 3 的信号量
         * 这意味着最多同时允许 3 个线程获取许可进入受保护区域
         * 工作流程：
         * 线程调用 semaphore.tryAcquire() 尝试获取许可
         * 若成功获取，计数器减 1，线程可以执行业务逻辑（模拟调用下游服务）
         * 业务逻辑完成后，调用 semaphore.release() 释放许可，计数器加 1
         * 若许可已用完（计数为 0），其他线程会被阻塞或超时
         * 在此场景中的意义：
         * 下游服务最多支持 3 个并发请求
         * 即使线程池有 8 个线程，也只有 3 个能同时进入下游
         * 其余请求在 200ms 超时后被拒绝，防止系统雪崩
         * 这是一个典型的限流和资源隔离实现。
         */

        Semaphore semaphore = new Semaphore(3);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch done = new CountDownLatch(10);
        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        for (int i = 0; i < 10; i++) {
            final int requestId = i;
            pool.submit(() -> {
                boolean acquired = false;
                try {
                    acquired = semaphore.tryAcquire(200, TimeUnit.MILLISECONDS);
                    if (!acquired) {
                        rejected.incrementAndGet();
                        System.out.printf("request-%d rejected (busy)%n", requestId);
                        return;
                    }
                    accepted.incrementAndGet();
                    System.out.printf("request-%d handled by %-18s | inFlight=%d%n",
                            requestId, Thread.currentThread().getName(), 3 - semaphore.availablePermits());
                    TimeUnit.MILLISECONDS.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    if (acquired) {
                        semaphore.release();
                    }
                    done.countDown();
                }
            });
        }

        done.await();
        pool.shutdown();
        pool.awaitTermination(3, TimeUnit.SECONDS);
        System.out.printf("accepted=%d, rejected=%d%n", accepted.get(), rejected.get());
    }

    public static void main(String[] args) {
        try {
            run();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
