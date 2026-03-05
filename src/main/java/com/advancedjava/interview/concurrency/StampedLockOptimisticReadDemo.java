package com.advancedjava.interview.concurrency;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.StampedLock;

/**
 * StampedLock 乐观读示例。
 *
 * 典型场景：读多写少。
 * - 先用 tryOptimisticRead() 做无阻塞读
 * - 若校验失败再回退到悲观读锁
 */
public final class StampedLockOptimisticReadDemo {

    private StampedLockOptimisticReadDemo() {
    }

    public static void main(String[] args) throws InterruptedException {
        run();
    }

    public static void run() throws InterruptedException {
        System.out.println("\n=== 6) StampedLock 乐观读 ===");
        InventoryState state = new InventoryState(100);
        LongAdder optimisticSuccess = new LongAdder();
        LongAdder fallbackReadLock = new LongAdder();

        Thread writer = new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                state.add(Thread.currentThread().getName(), 1);
                sleep(60);
            }
        }, "writer");

        Thread readerA = new Thread(() -> readLoop(state, optimisticSuccess, fallbackReadLock), "reader-A");
        Thread readerB = new Thread(() -> readLoop(state, optimisticSuccess, fallbackReadLock), "reader-B");

        writer.start();
        readerA.start();
        readerB.start();

        writer.join();
        readerA.join();
        readerB.join();

        System.out.printf("final stock=%d, optimisticSuccess=%d, fallbackReadLock=%d%n",
                state.getAccurateStock(), optimisticSuccess.sum(), fallbackReadLock.sum());
    }

    private static void readLoop(InventoryState state, LongAdder optimisticSuccess, LongAdder fallbackReadLock) {
        for (int i = 0; i < 40; i++) {
            int value = state.readMostlyOptimistic(optimisticSuccess, fallbackReadLock);
            if (i % 15 == 0) {
                System.out.printf("%s reads stock=%d%n", Thread.currentThread().getName(), value);
            }
            sleep(25);
        }
    }

    private static void sleep(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static final class InventoryState {
        /**
         * 这段代码定义了 InventoryState 内部类的两个私有字段：
         * StampedLock lock
         * 用于控制对 stock 字段的并发访问
         * StampedLock 支持三种锁模式：写锁、读锁、乐观读
         * 返回一个 "stamp"（时间戳）来追踪锁的版本，用于验证乐观读是否有效
         * int stock
         * 存储库存数量
         * 需要通过 lock 保护，因为多个线程会并发读写
         * 使用流程：
         * 写操作（第 77-87 行）：获取写锁 → 修改 stock → 释放写锁
         * 乐观读（第 89-104 行）：先无锁读取 → 验证 stamp 是否有效 → 若失败则降级为读锁
         * 安全读（第 106-113 行）：获取读锁 → 读取 stock → 释放读锁
         * 这个设计适合读多写少的场景，因为乐优化了高频读操作的性能。
         */
        private final StampedLock lock = new StampedLock();
        private int stock;

        private InventoryState(int initial) {
            this.stock = initial;
        }

        private void add(String who, int delta) {
            long stamp = lock.writeLock();
            try {
                stock += delta;
                if (stock % 10 == 0) {
                    System.out.printf("%s updated stock=%d%n", who, stock);
                }
            } finally {
                lock.unlockWrite(stamp);
            }
        }

        private int readMostlyOptimistic(LongAdder optimisticSuccess, LongAdder fallbackReadLock) {
            long stamp = lock.tryOptimisticRead();
            int current = stock;
            // 验证乐观读的 stamp 是否有效（即获取戳记后是否有写操作发生）。
            if (lock.validate(stamp)) {
                optimisticSuccess.increment();
                return current;
            }

            long readStamp = lock.readLock(); //
            try {
                fallbackReadLock.increment();
                return stock;
            } finally {
                lock.unlockRead(readStamp);
            }
        }

        private int getAccurateStock() {
            long stamp = lock.readLock();
            try {
                return stock;
            } finally {
                lock.unlockRead(stamp);
            }
        }
    }
}
