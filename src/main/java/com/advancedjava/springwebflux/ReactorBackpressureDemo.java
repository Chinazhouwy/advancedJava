package com.advancedjava.springwebflux;

import java.time.Duration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

/**
 * Backpressure demo with a fast producer and a slow consumer.
 */
public class ReactorBackpressureDemo {

    public static void main(String[] args) {
        Flux.interval(Duration.ofMillis(10))
                .map(Long::intValue)
                .take(100)
                .onBackpressureBuffer(
                        16,
                        dropped -> System.out.println("buffer overflow, dropped=" + dropped))
                .delayElements(Duration.ofMillis(50))
                .doOnNext(v -> System.out.println("consume=" + v))
                .doFinally(
                        signal -> {
                            if (signal == SignalType.ON_COMPLETE) {
                                System.out.println("done");
                            }
                        })
                .blockLast();
    }
}
