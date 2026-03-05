package com.advancedjava.interview.concurrency;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CompletableFuture 编排示例。
 *
 * 模拟场景：
 * - 并行查询候选人简历信息和测评成绩
 * - 汇总成一个结果
 * - 对上游超时和异常做兜底，避免整条链路失败
 */
public final class CompletableFutureOrchestrationDemo {

    private CompletableFutureOrchestrationDemo() {
    }

    public static void run() {
        System.out.println("\n=== 2) CompletableFuture 并行编排 + 超时兜底 ===");
        ExecutorService ioPool = Executors.newFixedThreadPool(4);

        try {
            CompletableFuture<String> profileFuture = CompletableFuture.supplyAsync(() -> {
                sleep(180);
                return "Alice(Java/Spring)";
            }, ioPool);

            CompletableFuture<Integer> scoreFuture = CompletableFuture.supplyAsync(() -> {
                sleep(220);
                return 86;
            }, ioPool);

            String summary = profileFuture
                    .thenCombine(scoreFuture, (profile, score) -> "candidate=" + profile + ", score=" + score)
                    .orTimeout(1, TimeUnit.SECONDS)
                    .exceptionally(ex -> "fallback: score service timeout")
                    .join();

            System.out.println(summary);
        } finally {
            ioPool.shutdown();
        }
    }

    private static void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        run();
    }
}
