package com.advancedjava.interview.concurrency;

/**
 * 并发学习总入口（教学版）。
 *
 * <p>用途：把当前包里多个并发 demo 串成“可按顺序学习”的课程入口。
 * 每个阶段都标注你应该关注的 API 与参数。
 */
public final class ConcurrencyStudyGuide {

    private ConcurrencyStudyGuide() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("\n================ 并发学习路线（教学版） ================");

        step("1. 线程池参数与拒绝策略",
                "关注 ThreadPoolExecutor(core,max,keepAlive,queue,rejectPolicy) 参数联动行为");
        ThreadPoolTuningDemo.run();

        step("2. CompletableFuture 编排",
                "关注 supplyAsync / thenCombine / orTimeout / exceptionally 的调用顺序");
        CompletableFutureOrchestrationDemo.run();

        step("3. 批量协调",
                "关注 CountDownLatch(一次性) vs CyclicBarrier/Phaser(可重复阶段)");
        CountDownLatchBatchDemo.run();
        CyclicBarrierPhaserDemo.run();

        step("4. 并发容器",
                "关注 ConcurrentHashMap.compute 原子更新、CopyOnWrite 读多写少、SkipList 有序并发");
        ConcurrentHashMapComputeDemo.run();
        CopyOnWriteArrayListReadMostlyDemo.run();
        ConcurrentSkipListMapRankingDemo.run();

        step("5. 锁与同步器",
                "关注 Semaphore 限流隔离、StampedLock 乐观读、AQS 自定义互斥、LockSupport 精准唤醒");
        SemaphoreBulkheadDemo.run();
        StampedLockOptimisticReadDemo.run();
        AqsCustomMutexDemo.run();
        LockSupportHandshakeDemo.run();

        step("6. 计数器性能",
                "关注 AtomicLong CAS 热点 vs LongAdder 分段累加");
        LongAdderVsAtomicLongDemo.run();

        System.out.println("\n================ 学习入口执行完成 ================\n");
    }

    /**
     * 打印学习阶段标题。
     *
     * @param title 阶段标题
     * @param focus 本阶段建议关注点
     */
    private static void step(String title, String focus) {
        System.out.println("\n--- " + title + " ---");
        System.out.println("focus: " + focus);
    }
}
