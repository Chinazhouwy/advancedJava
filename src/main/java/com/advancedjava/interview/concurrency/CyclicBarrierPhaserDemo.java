package com.advancedjava.interview.concurrency;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

/**
 * CyclicBarrier + Phaser 协调示例。
 *
 * 学习点：
 * 1. CyclicBarrier：固定参与者在同一阶段“会合”后再继续。
 * 2. Phaser：支持多阶段和动态注册/注销，适合复杂流水线。
 */
public final class CyclicBarrierPhaserDemo {

    private CyclicBarrierPhaserDemo() {
    }

    public static void run() throws InterruptedException {
        System.out.println("\n=== 12) CyclicBarrier + Phaser 多阶段协作 ===");
        runBarrierStage();
        runPhaserStage();
    }

    private static void runBarrierStage() throws InterruptedException {
        CyclicBarrier barrier = new CyclicBarrier(3, () -> System.out.println("barrier: all ready, start together"));

        Runnable worker = () -> {
            try {
                sleep(80);
                barrier.await();
                System.out.println(Thread.currentThread().getName() + " passed barrier");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        Thread t1 = new Thread(worker, "barrier-A");
        Thread t2 = new Thread(worker, "barrier-B");
        Thread t3 = new Thread(worker, "barrier-C");
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
    }

    private static void runPhaserStage() {
        Phaser phaser = new Phaser(1); // 主线程先注册

        for (int i = 0; i < 3; i++) {
            final int workerId = i;
            phaser.register();
            Thread thread = new Thread(() -> {
                try {
                    System.out.printf("phaser-worker-%d phase-0 work%n", workerId);
                    sleep(60 + workerId * 20L);
                    phaser.arriveAndAwaitAdvance();

                    System.out.printf("phaser-worker-%d phase-1 work%n", workerId);
                    sleep(70 + workerId * 20L);
                    phaser.arriveAndDeregister();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, "phaser-" + workerId);
            thread.start();
        }

        // 主线程与子线程一起推进 phase
        phaser.arriveAndAwaitAdvance(); // 等 phase-0 完成
        System.out.println("main observed phase-0 complete");
        phaser.arriveAndDeregister();
    }

    private static void sleep(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
