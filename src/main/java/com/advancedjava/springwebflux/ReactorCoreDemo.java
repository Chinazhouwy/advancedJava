package com.advancedjava.springwebflux;

import java.time.Duration;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactor Core basics: Mono/Flux creation + common operators.
 */
public class ReactorCoreDemo {

    public static void main(String[] args) {
        monoDemo();
        fluxMapFilterDemo();
        zipAndFlatMapDemo();
    }

    private static void monoDemo() {
        System.out.println("\n=== Mono Demo ===");

        Mono<String> mono = Mono.just("spring-webflux")
                .map(String::toUpperCase)
                .doOnNext(v -> System.out.println("mono value = " + v));

        mono.block();
    }

    private static void fluxMapFilterDemo() {
        System.out.println("\n=== Flux map/filter Demo ===");

        List<Integer> result = Flux.range(1, 10)
                .map(n -> n * 2)
                .filter(n -> n % 4 == 0)
                .doOnNext(v -> System.out.println("processed = " + v))
                .collectList()
                .block();

        System.out.println("result = " + result);
    }

    private static void zipAndFlatMapDemo() {
        System.out.println("\n=== zip + flatMap Demo ===");

        Mono<String> userMono = Mono.just("u-1001").delayElement(Duration.ofMillis(100));
        Mono<String> profileMono = Mono.just("profile-A").delayElement(Duration.ofMillis(120));

        String zipped = Mono.zip(userMono, profileMono)
                .map(tuple -> "user=" + tuple.getT1() + ", profile=" + tuple.getT2())
                .block();

        System.out.println("zipped = " + zipped);

        List<String> expanded = Flux.just("A", "B", "C")
                .flatMap(ch -> Mono.just("item-" + ch).delayElement(Duration.ofMillis(50)))
                .collectList()
                .block();

        System.out.println("flatMap result = " + expanded);
    }
}
