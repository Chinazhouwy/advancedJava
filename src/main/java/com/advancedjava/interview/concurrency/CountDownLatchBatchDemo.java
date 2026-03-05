package com.advancedjava.interview.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * CountDownLatch 批任务汇聚示例。
 *
 * 模拟场景：
 * 一次评测需要并行调用多个子任务（规则检查、语义分析、风险检查），
 * 主线程必须等待所有子任务结束后再汇总结果。
 */
public final class CountDownLatchBatchDemo {

    private CountDownLatchBatchDemo() {
    }

    public static void run() throws InterruptedException {
        System.out.println("\n=== 4) CountDownLatch 批量任务汇聚 ===");
        int workers = 5;
        /**
         * CountDownLatch 用法说明
         * CountDownLatch done = new CountDownLatch(workers); 创建一个计数器为 5 的闭锁。
         * 工作原理：
         * 初始化：new CountDownLatch(5) 设置内部计数器为 5
         * 递减：每个 worker 任务完成时调用 done.countDown()，计数器减 1
         * 阻塞：主线程在 done.await() 处阻塞，直到计数器归零
         * 唤醒：当所有 5 个 worker 都调用了 countDown() 后，计数器变为 0，主线程被唤醒
         * 应用场景：
         * 主线程需要等待多个并行子任务全部完成后再继续
         * 本例中：等待 5 个 worker 都完成评测后，主线程才能汇总结果
         * 关键方法：
         * 方法
         * 作用
         * countDown()
         * 计数器减 1
         * await()
         * 阻塞等待计数器归零
         * await(timeout, unit)
         * 阻塞等待，指定超时时间
         * getCount()
         * 获取当前计数值
         */
        CountDownLatch done = new CountDownLatch(workers);
        ExecutorService pool = Executors.newFixedThreadPool(workers);

        for (int i = 0; i < workers; i++) {
            final int workerId = i;
            pool.submit(() -> {
                try {
                    int cost = ThreadLocalRandom.current().nextInt(100, 350);
                    TimeUnit.MILLISECONDS.sleep(cost);
                    System.out.printf("worker-%d finished in %d ms%n", workerId, cost);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        done.await();
        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.SECONDS);
        System.out.println("all workers finished, now merge report");
    }

    public static void main(String[] args) throws InterruptedException {
        run();
    }
}
