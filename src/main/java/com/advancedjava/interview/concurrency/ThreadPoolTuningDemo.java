package com.advancedjava.interview.concurrency;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池参数与拒绝策略示例。
 *
 * 重点：
 * 1. corePoolSize / maximumPoolSize / queueCapacity 如何影响吞吐与排队。
 * 2. 当线程 + 队列都满时，CallerRunsPolicy 会把任务回退给提交线程执行（背压效果）。
 */
public final class ThreadPoolTuningDemo {

    private ThreadPoolTuningDemo() {
    }

    public static void run() throws InterruptedException {
        System.out.println("\n=== 1) ThreadPoolExecutor 参数与拒绝策略 ===");
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                2,
                4,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),
                /**
                 * CallerRunsPolicy 是一种拒绝策略，当线程池的核心线程、最大线程和队列都满时，新提交的任务会直接在提交任务的线程中执行，而不是被拒绝。
                 * 在这个示例中的效果：
                 * 核心线程数：2
                 * 最大线程数：4
                 * 队列容量：2
                 * 当提交第 7 个任务时：
                 * 2 个核心线程已占用
                 * 4 个最大线程已占用
                 * 2 个队列位置已满
                 * 第 7 个任务无处可去 → 由主线程（提交线程）直接执行
                 * 优势：提供背压效果，防止任务堆积，让提交者感受到压力并调整提交速度
                 */
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        int taskCount = 10;

        /**
         * 这行代码创建了一个 CountDownLatch 对象，用于线程同步。
         * 作用：
         * CountDownLatch(taskCount) 初始化一个计数器，初始值为 taskCount（10）
         * 它是一个同步工具，用来让主线程等待所有 10 个任务完成
         * 在这段代码中的流程：
         * 第 45 行：创建计数器为 10 的 CountDownLatch
         * 第 62 行：done.await() 主线程阻塞，等待计数器归零
         * 第 57 行：每个任务完成时调用 done.countDown()，将计数器减 1
         * 当所有 10 个任务都调用 countDown() 后，计数器变为 0，主线程被唤醒，继续执行关闭线程池的逻辑
         * 简单理解： CountDownLatch 相当于一个"任务完成计数器"，确保主线程不会在任务还未执行完毕时就关闭线程池。`
         */
        CountDownLatch done = new CountDownLatch(taskCount);

        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            pool.execute(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(250);
                    System.out.printf("task-%d executed by %-18s | queueSize=%d%n",
                            taskId, Thread.currentThread().getName(), pool.getQueue().size());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        done.await();
        pool.shutdown();
        /**
         * pool.awaitTermination(3, TimeUnit.SECONDS) 的作用是让主线程等待线程池中的所有任务完成执行，最多等待 3 秒钟。
         * 详细说明：
         * shutdown()：通知线程池不再接收新任务，但已提交的任务会继续执行
         * awaitTermination(3, TimeUnit.SECONDS)：主线程阻塞等待，直到以下任一情况发生：
         * 线程池中所有任务都执行完毕（正常完成）
         * 等待时间超过 3 秒（超时）
         * 执行流程：
         * 第 76 行：调用 shutdown() 关闭线程池
         * 第 77 行：主线程最多等待 3 秒，让剩余任务完成
         * 如果 3 秒内所有任务完成，方法返回 true；否则返回 false
         * 作用： 确保线程池的优雅关闭，给予任务足够的时间完成，避免线程池被立即强制终止而导致任务丢失。
         */
        pool.awaitTermination(3, TimeUnit.SECONDS);
    }
}
