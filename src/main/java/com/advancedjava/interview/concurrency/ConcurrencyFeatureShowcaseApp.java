package com.advancedjava.interview.concurrency;

/**
 * 并发知识点串联入口。
 * 一次运行会顺序执行多个小 demo，方便学习时对照输出看行为差异。
 */
public class ConcurrencyFeatureShowcaseApp {

    public static void main(String[] args) throws Exception {
        ThreadPoolTuningDemo.run();
        CompletableFutureOrchestrationDemo.run();
        SemaphoreBulkheadDemo.run();
        CountDownLatchBatchDemo.run();
        LongAdderVsAtomicLongDemo.run();
        StampedLockOptimisticReadDemo.run();
        AqsCustomMutexDemo.run();
        LockSupportHandshakeDemo.run();
        ConcurrentSkipListMapRankingDemo.run();
        CopyOnWriteArrayListReadMostlyDemo.run();
        ConcurrentHashMapComputeDemo.run();
        CyclicBarrierPhaserDemo.run();
    }
}
