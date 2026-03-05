package com.advancedjava.interview.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * LongAdder 与 AtomicLong 对比示例。
 *
 * 经验规则：
 * - 高并发计数（写多）常用 LongAdder
 * - 强一致读写（每次 get 都必须精确）通常仍用 AtomicLong
 */

/**
 * ## LongAdder vs AtomicLong 区别与原理
 *
 * ### 核心区别
 *
 * | 特性 | AtomicLong | LongAdder |
 * |------|-----------|----------|
 * | **内部结构** | 单个 volatile long | Cell 数组 + base |
 * | **并发策略** | CAS 自旋 | 分散累加（Striped） |
 * | **写性能** | 高竞争下差 | 高竞争下优 |
 * | **读性能** | O(1)，强一致 | O(n)，最终一致 |
 *
 * ### 工作原理
 *
 * **AtomicLong**：所有线程竞争同一个 volatile 变量，使用 CAS 循环更新
 * ```
 * Thread1 --\
 * Thread2 ---|----> [volatile value] <---- CAS 自旋竞争
 * Thread3 --/
 * ```
 *
 * **LongAdder**：线程分散到不同 Cell，减少竞争
 * ```
 * Thread1 ---> Cell[0]
 * Thread2 ---> Cell[1]  \
 * Thread3 ---> Cell[2]  |---> sum() 求和
 * Thread4 ---> Cell[3]  /
 * ```
 *
 * ### 使用场景
 *
 * **用 LongAdder**（你的代码场景）：
 * - ✅ 高并发计数统计
 * - ✅ 写操作远多于读操作
 * - ✅ 对读值精度要求不高
 *
 * **用 AtomicLong**：
 * - ✅ 需要强一致性读写
 * - ✅ 竞争不激烈
 * - ✅ 频繁读取精确值
 *
 * ### 性能对比
 *
 * 你的基准测试中，LongAdder 在 8 线程 \* 200K 次操作下通常快 **3\~10 倍**，因为它避免了 CAS 失败重试的开销。
 */
public final class LongAdderVsAtomicLongDemo {

    private LongAdderVsAtomicLongDemo() {
    }

    public static void run() throws InterruptedException {
        System.out.println("\n=== 5) LongAdder vs AtomicLong（高竞争计数） ===");
        int threads = 8;
        int loopsPerThread = 200_000;

        AtomicLong atomic = new AtomicLong();
        long atomicCost = benchmarkAtomic(threads, loopsPerThread, atomic);

        LongAdder adder = new LongAdder();
        long adderCost = benchmarkAdder(threads, loopsPerThread, adder);

        long expected = (long) threads * loopsPerThread;
        System.out.printf("AtomicLong value=%d, cost=%d ms%n", atomic.get(), atomicCost);
        System.out.printf("LongAdder  value=%d, cost=%d ms%n", adder.sum(), adderCost);
        System.out.println("expected value=" + expected);
    }

    private static long benchmarkAtomic(int threads, int loopsPerThread, AtomicLong atomic) throws InterruptedException {
        long start = System.nanoTime();
        List<Thread> workers = new ArrayList<>(threads);
        for (int i = 0; i < threads; i++) {
            Thread worker = new Thread(() -> {
                for (int j = 0; j < loopsPerThread; j++) {
                    atomic.incrementAndGet();
                }
            });
            workers.add(worker);
            worker.start();
        }
        for (Thread worker : workers) {
            worker.join();
        }
        return (System.nanoTime() - start) / 1_000_000;
    }

    private static long benchmarkAdder(int threads, int loopsPerThread, LongAdder adder) throws InterruptedException {
        long start = System.nanoTime();
        List<Thread> workers = new ArrayList<>(threads);
        for (int i = 0; i < threads; i++) {
            Thread worker = new Thread(() -> {
                for (int j = 0; j < loopsPerThread; j++) {
                    adder.increment();
                }
            });
            workers.add(worker);
            worker.start();
        }
        for (Thread worker : workers) {
            worker.join();
        }
        return (System.nanoTime() - start) / 1_000_000;
    }
}
