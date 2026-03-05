package com.advancedjava.interview.concurrency;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * CopyOnWriteArrayList 示例（读多写少）。
 *
 * 学习点：
 * 1. 迭代时拿到的是快照，遍历期间不会看到后续写入。
 * 2. 写入会复制底层数组，写开销大，不适合高频写场景。
 * 3. 典型用途：监听器列表、配置快照、白名单缓存。
 */
public final class CopyOnWriteArrayListReadMostlyDemo {

    private CopyOnWriteArrayListReadMostlyDemo() {
    }

    public static void run() throws InterruptedException {
        System.out.println("\n=== 10) CopyOnWriteArrayList 读多写少 ===");
        CopyOnWriteArrayList<String> listeners = new CopyOnWriteArrayList<>();
        listeners.add("L1");
        listeners.add("L2");
        listeners.add("L3");

        Thread reader = new Thread(() -> {
            // 这里拿到的迭代器是快照，不会受并发写影响。
            for (String listener : listeners) {
                System.out.println("reader sees " + listener);
                sleep(80);
            }
        }, "cow-reader");

        Thread writer = new Thread(() -> {
            sleep(100);
            listeners.add("L4");
            System.out.println("writer added L4");
        }, "cow-writer");

        reader.start();
        writer.start();
        reader.join();
        writer.join();

        System.out.println("after iteration, real list = " + listeners);
    }

    private static void sleep(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
