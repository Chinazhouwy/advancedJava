package com.advancedjava.springwebflux;

import java.time.Duration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Error handling patterns in Reactor.
 */
public class ReactorErrorHandlingDemo {

    public static void main(String[] args) {
        onErrorReturnDemo();
        onErrorResumeDemo();
        retryDemo();
        timeoutFallbackDemo();
    }

    private static void onErrorReturnDemo() {
        System.out.println("\n=== onErrorReturn ===");

        Integer value = Mono.fromCallable(() -> 10 / 0)
                .onErrorReturn(-1)
                .block();

        System.out.println("value=" + value);
    }

    private static void onErrorResumeDemo() {
        System.out.println("\n=== onErrorResume ===");

        String v = Mono.<String>error(new RuntimeException("service unavailable"))
                .onErrorResume(ex -> Mono.just("fallback-by-cache"))
                .block();

        System.out.println("value=" + v);
    }

    private static void retryDemo() {
        System.out.println("\n=== retryWhen ===");

        Flux<Integer> flux = Flux.range(1, 3)
                .map(i -> {
                    if (i == 2) {
                        throw new IllegalStateException("boom at " + i);
                    }
                    return i;
                })
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(50)));

        try {
            flux.blockLast();
        } catch (Exception e) {
            System.out.println("final error=" + e.getMessage());
        }
    }

    private static void timeoutFallbackDemo() {
        System.out.println("\n=== timeout + fallback ===");

        String value = Mono.just("slow-response")
                .delayElement(Duration.ofSeconds(2))
                .timeout(Duration.ofMillis(300), Mono.just("timeout-fallback"))
                .block();

        System.out.println("value=" + value);
    }
}
