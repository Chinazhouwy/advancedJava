package com.advancedjava.springwebflux;

import java.time.Duration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Reactor 错误处理模式示例。
 *
 * <p>你要先建立一个认知：
 * 在响应式里，异常也是“数据流中的事件”，可以被操作符捕获和转换。
 *
 * <p>因此，学习错误处理时不要只把它理解成 try/catch 的替代品，
 * 而要把它理解成“当上游发出 onError 信号后，下游还有哪些恢复策略可选”。
 */
public class ReactorErrorHandlingDemo {

    public static void main(String[] args) {
        onErrorReturnDemo();
        onErrorResumeDemo();
        retryDemo();
        timeoutFallbackDemo();
    }

    /**
     * onErrorReturn: 发生错误时返回固定值。
     *
     * <p>适合：
     * 1. 默认值明确且简单。
     * 2. 不需要根据异常类型做复杂分支。
     */
    private static void onErrorReturnDemo() {
        System.out.println("\n=== onErrorReturn ===");

        Integer value = Mono.fromCallable(() -> 10 / 0)
                // 出错后直接返回 -1。
                .onErrorReturn(-1)
                .block();

        System.out.println("value=" + value);
    }

    /**
     * onErrorResume: 发生错误时切换到备用流（更灵活）。
     *
     * <p>和 onErrorReturn 的区别在于：
     * onErrorResume 可以根据异常内容决定后续逻辑，还可以切换到另一个 Mono/Flux。
     */
    private static void onErrorResumeDemo() {
        System.out.println("\n=== onErrorResume ===");

        String v = Mono.<String>error(new RuntimeException("service unavailable"))
                // ex 参数就是捕获到的异常对象。
                .onErrorResume(ex -> Mono.just("fallback-by-cache"))
                .block();

        System.out.println("value=" + v);
    }

    /**
     * retryWhen: 失败后重试。
     *
     * <p>要点：
     * 1. 重试本质上是重新订阅上游。
     * 2. 如果上游每次执行都会再次抛错，最终还是会失败。
     * 3. 重试适合瞬时故障，不适合参数错误、业务校验错误这类确定性失败。
     */
    private static void retryDemo() {
        System.out.println("\n=== retryWhen ===");

        Flux<Integer> flux = Flux.range(1, 3)
                .map(i -> {
                    if (i == 2) {
                        throw new IllegalStateException("boom at " + i);
                    }
                    return i;
                })
                // fixedDelay(2, 50ms): 最多重试 2 次，每次间隔 50ms。
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(50)));

        try {
            flux.blockLast();
        } catch (Exception e) {
            // 这里能看到“多次重试后仍然失败”的最终异常。
            System.out.println("final error=" + e.getMessage());
        }
    }

    /**
     * timeout + fallback: 超时兜底。
     *
     * <p>这类模式常用于：
     * 1. 下游服务响应时间不可控。
     * 2. 页面或接口需要尽快返回降级结果。
     */
    private static void timeoutFallbackDemo() {
        System.out.println("\n=== timeout + fallback ===");

        String value = Mono.just("slow-response")
                .delayElement(Duration.ofSeconds(2))
                // 300ms 内没有结果则切换到备用 Mono。
                .timeout(Duration.ofMillis(300), Mono.just("timeout-fallback"))
                .block();

        System.out.println("value=" + value);
    }
}
