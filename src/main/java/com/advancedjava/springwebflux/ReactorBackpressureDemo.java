package com.advancedjava.springwebflux;

import java.time.Duration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

/**
 * 背压（Backpressure）示例。
 *
 * <p>目标：模拟“生产者很快、消费者很慢”的情况，观察缓冲行为。
 *
 * <p>背压是什么：
 * 当下游处理能力跟不上上游发射速度时，需要一种机制保护系统不被压垮。
 *
 * <p>这个 Demo 的设计思路：
 * 1. 上游每 10ms 产生一个元素，故意让生产速度很快。
 * 2. 下游每处理一个元素都延迟 50ms，故意让消费速度很慢。
 * 3. 中间使用 onBackpressureBuffer 观察“跟不上时先缓存，缓存满了再丢弃”的行为。
 */
public class ReactorBackpressureDemo {

    public static void main(String[] args) {
        Flux.interval(Duration.ofMillis(10))
                // interval 产出 Long，这里转成 int 便于打印。
                .map(Long::intValue)
                // 只演示 100 条，避免无限输出。
                .take(100)
                // 背压缓冲：当下游跟不上时，最多缓存 16 条。
                // 超过后会触发 dropped 回调。
                // 这类写法适合教学观察，不代表生产环境就一定该无限制加 buffer。
                // 真实系统里通常还要结合限流、丢弃策略、批处理策略一起设计。
                .onBackpressureBuffer(
                        16,
                        dropped -> System.out.println("buffer overflow, dropped=" + dropped))
                // 人为模拟“慢消费者”：每条处理需要 50ms。
                // 注意：这里的 delayElements 是在下游阶段引入处理延迟，
                // 所以你会看到上游持续产生，而下游明显来不及消费。
                .delayElements(Duration.ofMillis(50))
                .doOnNext(v -> System.out.println("consume=" + v))
                // finally 无论正常结束还是异常中断都会触发。
                .doFinally(
                        signal -> {
                            if (signal == SignalType.ON_COMPLETE) {
                                System.out.println("done");
                            }
                        })
                // 阻塞直到流结束（仅 Demo 使用）。
                .blockLast();
    }
}
