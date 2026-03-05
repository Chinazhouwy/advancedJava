package com.advancedjava.interview.concurrency;

import java.util.concurrent.locks.LockSupport;

/**
 * LockSupport 协作示例：两个线程交替打印。
 *
 * 学习点：
 * 1. LockSupport 使用 permit（许可）模型。
 * 2. unpark 可以先于 park 调用，许可不会丢（最多保留 1 个）。
 * 3. 常用于 AQS、线程调度器等底层并发框架。
 */
public final class LockSupportHandshakeDemo {

    private LockSupportHandshakeDemo() {
    }

    public static void run() throws InterruptedException {
        System.out.println("\n=== 8) LockSupport 线程握手 ===");
        int rounds = 5;
        Thread[] holder = new Thread[2];

        Thread workerA = new Thread(() -> {
            for (int i = 1; i <= rounds; i++) {
                System.out.println("A" + i);
                LockSupport.unpark(holder[1]);
                if (i < rounds) {
                    LockSupport.park();
                }
            }
        }, "locker-A");

        Thread workerB = new Thread(() -> {
            for (int i = 1; i <= rounds; i++) {
                LockSupport.park();
                System.out.println("B" + i);
                LockSupport.unpark(holder[0]);
            }
        }, "locker-B");

        holder[0] = workerA;
        holder[1] = workerB;

        workerA.start();
        workerB.start();
        workerA.join();
        workerB.join();
    }
}
