package com.advancedjava.springwebflux;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * WebFlux 学习用接口集合（教学版）。
 *
 * <p>本类目标：让你在一个 Controller 里把 Mono、Flux、SSE、Sink、错误恢复、背压都跑通。
 *
 * <p>换句话说，这个类不是为了演示“业务建模”，而是为了集中演示
 * “一个响应式 HTTP 接口通常会返回什么、如何持续推送、如何恢复异常、如何观察流式节奏”。
 *
 * <p>核心注解说明：
 * 1. @RestController: 返回值默认按 JSON/文本响应，不走模板视图。
 * 2. @RequestMapping("/learn/webflux"): 给本类所有接口加统一前缀。
 */
@RestController
@RequestMapping("/learn/webflux")
public class WebFluxLearningController {

    /**
     * 多播消息总线（聊天消息）。
     *
     * <p>Sinks.Many<String> 可以被理解为“手动推送数据到 Flux 流”的入口。
     *
     * <p>调用链解释：
     * 1. Sinks.many(): 创建“可发送多条数据”的 Sink。
     * 2. multicast(): 一个消息发给所有当前订阅者（适合 SSE 群发）。
     * 3. onBackpressureBuffer(): 订阅者慢时先进入缓冲区，避免立刻丢数据。
     */
    private final Sinks.Many<String> chatSink = Sinks.many().multicast().onBackpressureBuffer();

    /**
     * Mono 示例：返回单个异步结果。
     *
     * <p>适合理解为“最终只会产生 0 或 1 个结果”的响应。
     *
     * @param name 请求参数，URL 形式：?name=alice。不给时默认 world。
     * @return Mono 包裹的 Map（最终序列化为 JSON）。
     */
    @GetMapping("/mono/hello")
    public Mono<Map<String, Object>> monoHello(@RequestParam(defaultValue = "world") String name) {
        // Mono.just(value): 立即产生 1 个值并完成。
        return Mono.just(
                Map.of(
                        "type", "mono",
                        "message", "hello, " + name,
                        "at", Instant.now().toString()));
    }

    /**
     * Flux 示例：返回多个异步结果。
     *
     * <p>客户端不会等所有元素都准备好才一次性收到，
     * 而是会随着元素产生逐步接收响应内容。
     *
     * @param count 期望数量。这里做了安全限制，避免传入过大值。
     * @return Flux 流，每 200ms 推送一条 JSON。
     */
    @GetMapping("/flux/numbers")
    public Flux<Map<String, Object>> fluxNumbers(@RequestParam(defaultValue = "5") int count) {
        // 限制 count 在 [1, 50]，防止演示时一次返回太多。
        int safeCount = Math.max(1, Math.min(count, 50));

        return Flux.range(1, safeCount)
                // delayElements: 给每个元素增加间隔，便于观察“流式返回”。
                .delayElements(Duration.ofMillis(200))
                .map(
                        i ->
                                Map.of(
                                        "type", "flux",
                                        "index", i,
                                        "square", i * i,
                                        "at", Instant.now().toString()));
    }

    /**
     * SSE 示例：每秒推送一个 tick 事件。
     *
     * <p>produces = TEXT_EVENT_STREAM_VALUE 很关键，告诉客户端这是 SSE 流。
     *
     * @return Flux<ServerSentEvent<...>>，浏览器/EventSource/curl -N 都可持续接收。
     */
    @GetMapping(value = "/sse/ticks", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Map<String, Object>>> sseTicks() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(
                        tick ->
                                // ServerSentEvent.builder(): 构建 SSE 协议事件。
                                // event: 事件名；id: 事件 ID；data: 事件数据。
                                ServerSentEvent.<Map<String, Object>>builder()
                                        .event("tick")
                                        .id(String.valueOf(tick))
                                        .data(
                                                Map.of(
                                                        "tick", tick,
                                                        "at", Instant.now().toString()))
                                        .build());
    }

    /**
     * SSE 聊天流：合并心跳流 + 用户消息流。
     *
     * <p>为什么要心跳：有些代理/连接会长时间空闲后断开，心跳可保持连接活跃。
     *
     * <p>这个接口很适合拿来理解“服务端主动推消息”的基本结构：
     * 一个定时流负责保活，一个业务流负责发送真正消息，最后 merge 成一个统一输出流。
     *
     * @return 持续事件流：event=heartbeat 或 event=chat。
     */
    @GetMapping(value = "/sse/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Map<String, Object>>> sseChat() {
        Flux<ServerSentEvent<Map<String, Object>>> heartbeat =
                Flux.interval(Duration.ofSeconds(3))
                        .map(
                                i ->
                                        ServerSentEvent.<Map<String, Object>>builder()
                                                .event("heartbeat")
                                                .data(
                                                        Map.of(
                                                                "message", "ping",
                                                                "at", Instant.now().toString()))
                                                .build());

        Flux<ServerSentEvent<Map<String, Object>>> chat =
                // asFlux(): 把 sink 转成订阅流。谁订阅这个流，谁就能收到 push 进来的消息。
                chatSink.asFlux()
                        .map(
                                msg ->
                                        ServerSentEvent.<Map<String, Object>>builder()
                                                .event("chat")
                                                .data(
                                                        Map.of(
                                                                "message", msg,
                                                                "at", Instant.now().toString()))
                                                .build());

        // merge: 把两个 Flux 并发合并成一个 Flux。
        return Flux.merge(heartbeat, chat);
    }

    /**
     * 手动推送聊天消息到 SSE 流。
     *
     * @param body 请求体 JSON，例如 {"message":"hello"}
     * @return Mono 包裹的结果，包含是否成功以及 emitResult 细节。
     */
    @PostMapping("/chat/push")
    public Mono<Map<String, Object>> pushChat(@RequestBody Map<String, String> body) {
        String message = body == null ? null : body.get("message");
        if (message == null || message.isBlank()) {
            return Mono.just(Map.of("success", false, "error", "message is required"));
        }

        // tryEmitNext: 尝试发送一个元素到 Sink。
        // 可能失败（例如终止状态/并发冲突），所以返回 EmitResult 而不是直接抛异常。
        Sinks.EmitResult emitResult = chatSink.tryEmitNext(message.trim());
        return Mono.just(Map.of("success", emitResult.isSuccess(), "emitResult", emitResult.name()));
    }

    /**
     * 错误恢复示例。
     *
     * <p>流程：
     * 1. fromCallable 里随机制造异常。
     * 2. onErrorResume 捕获异常并返回兜底数据。
     * 3. 对调用方来说，请求仍然是 200 响应，只是业务字段里体现了 fallback 结果。
     *
     * @return 即使失败也会有响应（不会让请求直接 500）。
     */
    @GetMapping("/error/fallback")
    public Mono<Map<String, Object>> errorFallback() {
        return Mono.fromCallable(() -> {
                    int n = ThreadLocalRandom.current().nextInt(10);
                    if (n < 6) {
                        throw new IllegalStateException("random failure n=" + n);
                    }
                    return Map.<String, Object>of("success", true, "value", n);
                })
                .onErrorResume(
                        ex ->
                                Mono.just(
                                        Map.of(
                                                "success", false,
                                                "fallback", "cache-value",
                                                "error", ex.getMessage())));
    }

    /**
     * 背压观察接口：生产快、消费慢。
     *
     * <p>说明：
     * 1. interval(20ms) 生产较快。
     * 2. delayElements(80ms) 人为模拟慢消费。
     * 3. onBackpressureBuffer(32) 缓冲最多 32 条。
     *
     * @return 一个流式 JSON 序列，便于观察数据节奏。
     */
    @GetMapping("/backpressure/slow-consumer")
    public Flux<Map<String, Object>> backpressureSlowConsumer() {
        return Flux.interval(Duration.ofMillis(20))
                .take(200)
                .onBackpressureBuffer(32)
                .delayElements(Duration.ofMillis(80))
                .map(
                        i ->
                                Map.of(
                                        "index", i,
                                        "at", Instant.now().toString()));
    }
}
