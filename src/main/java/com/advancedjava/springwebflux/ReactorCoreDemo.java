package com.advancedjava.springwebflux;

import java.time.Duration;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactor Core 基础示例（控制台版本）。
 *
 * <p>学习目标：
 * 1. 知道 Mono/Flux 分别适合什么场景。
 * 2. 理解常见操作符 map/filter/flatMap/zip。
 * 3. 看懂 block() 在 Demo 里的用途，以及为什么在线上 WebFlux 代码里要避免 block。
 */
public class ReactorCoreDemo {

    public static void main(String[] args) {
        monoDemo();
        fluxMapFilterDemo();
        zipAndFlatMapDemo();
    }

    /**
     * Mono 示例：单个异步值。
     */
    private static void monoDemo() {
        System.out.println("\n=== Mono Demo ===");

        Mono<String> mono = Mono.just("spring-webflux")
                // map: 一对一转换。
                .map(String::toUpperCase)
                // doOnNext: 观察数据流经过此节点（不改变数据）。
                .doOnNext(v -> System.out.println("mono value = " + v));

        // block: 阻塞等待结果，只建议在 main/test/demo 中使用。
        // 在 WebFlux Controller 里不要 block，而应直接返回 Mono/Flux 给框架处理。
        mono.block();
    }

    /**
     * Flux map/filter 示例：多个元素的流处理。
     */
    private static void fluxMapFilterDemo() {
        System.out.println("\n=== Flux map/filter Demo ===");

        List<Integer> result = Flux.range(1, 10)
                // map: 每个元素 *2。
                .map(n -> n * 2)
                // filter: 只保留可被 4 整除的元素。
                .filter(n -> n % 4 == 0)
                .doOnNext(v -> System.out.println("processed = " + v))
                // collectList: 把 Flux 聚合成 Mono<List<T>>。
                .collectList()
                .block();

        System.out.println("result = " + result);
    }

    /**
     * zip + flatMap 示例。
     */
    private static void zipAndFlatMapDemo() {
        System.out.println("\n=== zip + flatMap Demo ===");

        // 两个独立异步源（模拟两个下游服务）。
        Mono<String> userMono = Mono.just("u-1001").delayElement(Duration.ofMillis(100));
        Mono<String> profileMono = Mono.just("profile-A").delayElement(Duration.ofMillis(120));

        // zip: 等两个 Mono 都有结果后组合成 Tuple。
        String zipped = Mono.zip(userMono, profileMono)
                .map(tuple -> "user=" + tuple.getT1() + ", profile=" + tuple.getT2())
                .block();

        System.out.println("zipped = " + zipped);

        // flatMap: 一个输入可能映射成一个异步 Publisher，再把它“摊平”。
        List<String> expanded = Flux.just("A", "B", "C")
                .flatMap(ch -> Mono.just("item-" + ch).delayElement(Duration.ofMillis(50)))
                .collectList()
                .block();

        System.out.println("flatMap result = " + expanded);
    }
}
