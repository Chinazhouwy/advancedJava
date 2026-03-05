package com.advancedjava.interview.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;

/**
 * AQS 自定义互斥锁示例。
 *
 * 核心学习点：
 * 1. AQS 的 state=0/1 可表示锁空闲/已占用。
 * 2. tryAcquire/tryRelease 只关注状态切换，排队与阻塞由 AQS 框架处理。
 * 3. 这是不可重入锁（同一线程再次获取会失败），用于理解 AQS 最小骨架。
 */
public final class AqsCustomMutexDemo {

    /**
     * 私有构造函数，防止实例化。
     * 此类仅作为工具类/演示入口。
     */
    private AqsCustomMutexDemo() {
    }

    /**
     * 演示基于 AQS 的自定义互斥锁的使用。
     * 启动 6 个工作线程，每个线程对共享计数器执行 3000 次自增操作。
     * 预期总计数值：6 * 3000 = 18000。
     *
     * @throws InterruptedException 如果主线程在等待工作线程完成时被中断。
     */
    public static void run() throws InterruptedException {
        System.out.println("\n=== 7) AQS 自定义 Mutex ===");
        
        // 实例化自定义锁和共享资源（计数器）
        SimpleMutex mutex = new SimpleMutex();
        Counter counter = new Counter();
        List<Thread> workers = new ArrayList<>();

        // 创建并启动 6 个工作线程
        for (int i = 0; i < 6; i++) {
            Thread thread = new Thread(() -> {
                // 每个线程执行 3000 次递增
                for (int j = 0; j < 3000; j++) {
                    // 在修改共享状态前获取锁
                    mutex.lock();
                    try {
                        // 临界区：受互斥锁保护的非原子递增操作
                        counter.value++;
                    } finally {
                        // 始终在 finally 块中释放锁，防止死锁
                        mutex.unlock();
                    }
                }
            }, "aqs-worker-" + i);
            
            workers.add(thread);
            thread.start();
        }

        // 等待所有工作线程执行完毕
        for (Thread worker : workers) {
            worker.join();
        }

        // 验证结果。如果锁工作正常，最终值应恰好为 18000。
        System.out.println("counter value=" + counter.value + " (expected=18000)");
    }

    /**
     * 一个简单的容器类，用于存放共享的整数值。
     * 在实际场景中，这可以是任何需要互斥访问的共享资源。
     */
    private static final class Counter {
        private int value;
    }

    /**
     * 基于 AbstractQueuedSynchronizer (AQS) 构建的自定义互斥锁实现。
     * 这是一个不可重入的独占锁。
     */
    private static final class SimpleMutex {
        // 内部同步器，负责处理 AQS 的核心逻辑
        private final Sync sync = new Sync();

        /**
         * 以独占模式获取锁。
         * 如果锁已被其他线程持有，当前线程将阻塞等待。
         */
        void lock() {
            sync.acquire(1);
        }

        /**
         * 尝试在指定超时时间内获取锁。
         *
         * @param timeout 最大等待时间
         * @param unit 时间单位
         * @return 如果成功获取锁返回 true，超时则返回 false
         * @throws InterruptedException 如果等待过程中被中断
         */
        boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
            return sync.tryAcquireNanos(1, unit.toNanos(timeout));
        }

        /**
         * 释放锁。
         * 如果调用线程未持有该锁，将抛出异常。
         */
        void unlock() {
            sync.release(1);
        }

        /**
         * 返回与此锁关联的新 Condition 实例。
         * 用于线程在释放锁的同时等待特定条件。
         *
         * @return 新的 Condition 对象
         */
        Condition newCondition() {
            return sync.newCondition();
        }

        /**
         * 内部类，继承自 AQS，用于实现具体的锁逻辑。
         * AQS 负责管理等待队列，我们只需定义状态的转换规则。
         */

//        什么是 AbstractQueuedSynchronizer？
//        AQS 是 Java 并发包（JUC）中的一个框架类，用来构建各种锁和同步器。它是很多并发工具的基础，比如：
//        ReentrantLock（可重入锁）
//        CountDownLatch（倒计时门闩）
//        Semaphore（信号量）
//        ReentrantReadWriteLock（读写锁）
//        核心概念
        private static final class Sync extends AbstractQueuedSynchronizer {
            
            /**
             * 尝试以独占模式获取锁。
             * 逻辑：如果 state 为 0（未锁定），尝试通过 CAS 将其设置为 1（已锁定）。
             * 由于状态直接从 0 变为 1，因此该锁是不可重入的。
             *
             * @param arg 获取参数（此处未使用，惯例为 1）
             * @return 如果获取成功返回 true
             */
            @Override
            protected boolean tryAcquire(int arg) {
                // 只有当当前状态为 0 时才成功。CAS 保证了原子性。
                if (compareAndSetState(0, 1)) {
                    // 将当前线程设置为独占所有者
                    setExclusiveOwnerThread(Thread.currentThread());
                    return true;
                }
                // 如果 state != 0，说明锁已被占用（包括被当前线程再次请求，导致获取失败，体现不可重入性）
                return false;
            }

            /**
             * 尝试以独占模式释放锁。
             * 逻辑：验证当前线程是否拥有锁，然后将状态重置为 0。
             *
             * @param arg 释放参数（此处未使用）
             * @return 如果锁被完全释放（状态变为 0）返回 true
             */
            @Override
            protected boolean tryRelease(int arg) {
                // 校验：状态必须为 1 且所有者必须是当前线程
                if (getState() == 0 || getExclusiveOwnerThread() != Thread.currentThread()) {
                    throw new IllegalMonitorStateException("not lock owner");
                }
                // 清除所有权并将状态重置为未锁定 (0)
                setExclusiveOwnerThread(null);
                setState(0);
                return true;
            }

            /**
             * 检查同步状态是否由当前线程独占持有。
             * 这是 Condition 支持所必需的方法。
             *
             * @return 如果是独占持有返回 true
             */
            @Override
            protected boolean isHeldExclusively() {
                return getState() == 1 && getExclusiveOwnerThread() == Thread.currentThread();
            }

            /**
             * 创建一个绑定到此 Sync 的 ConditionObject。
             *
             * @return 新的 ConditionObject
             */
            private Condition newCondition() {
                return new ConditionObject();
            }
        }
    }
/*
    Condition 是 Java 并发包中的一个接口，用于实现线程间的协作等待。
    它允许线程在某个条件满足之前挂起（等待），并在条件满足时被唤醒。

 */
}
