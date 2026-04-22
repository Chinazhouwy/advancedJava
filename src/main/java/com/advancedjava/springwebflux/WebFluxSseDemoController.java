package com.advancedjava.springwebflux;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * 最小 SSE 示例（专注展示 SSE，不掺杂太多业务）。
 *
 * <p>接口：
 * 1. GET  /webflux-demo/stream: 订阅事件流（心跳 + 手动 push 的消息）。
 * 2. POST /webflux-demo/push:  往事件流里推一条消息。
 *
 * <p>建议你边运行边用浏览器 EventSource、Postman 或 curl -N 观察，
 * 这样最容易体会“HTTP 连接建立后，服务端持续往同一个连接推送消息”的行为。
 */
@RestController
@RequestMapping("/webflux-demo")
public class WebFluxSseDemoController {

    /**
     * 一个可手动发送消息的多播 Sink。
     *
     * <p>你可以把它看作“写入端”，而 stream() 里的 asFlux() 是“读取端”。
     */
    private final Sinks.Many<String> pushSink = Sinks.many().multicast().onBackpressureBuffer();

    /**
     * SSE 订阅接口。
     *
     * <p>这个接口返回后不会立刻关闭连接，而是把响应保持为一个持续输出的文本事件流。
     * 浏览器端通常会用 EventSource 持续监听。
     *
     * @return Flux 形式的 SSE 事件流；连接建立后会持续推送。
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Map<String, Object>>> stream() {

        // 心跳流：每秒一个 heartbeat，确保客户端能看到流在持续工作。
        Flux<ServerSentEvent<Map<String, Object>>> heartbeat =
                Flux.interval(Duration.ofSeconds(1))
                        .map(
                                tick ->
                                        ServerSentEvent.<Map<String, Object>>builder()
                                                .event("heartbeat")
                                                .data(
                                                        Map.of(
                                                                "source", "timer",
                                                                "tick", tick,
                                                                "at", Instant.now().toString()))
                                                .build());

        // 外部 push 流：来自 POST /push 的消息。
        Flux<ServerSentEvent<Map<String, Object>>> pushed =
                pushSink.asFlux()
                        .map(
                                msg ->
                                        // 这里把普通字符串包装成标准 SSE 事件结构，
                                        // 便于客户端按 event/data 维度处理。
                                        ServerSentEvent.<Map<String, Object>>builder()
                                                .event("message")
                                                .data(
                                                        Map.of(
                                                                "source", "push",
                                                                "message", msg,
                                                                "at", Instant.now().toString()))
                                                .build());

        // 合并两个流，客户端订阅一个 stream 就能同时收到两类事件。
        return Flux.merge(heartbeat, pushed);
    }

    /**
     * 推送消息接口。
     *
     * @param body 请求体 JSON，要求包含 message 字段。
     * @return 简单结果，包含 success 和 emitResult。
     */
    @PostMapping("/push")
    public Map<String, Object> push(@RequestBody Map<String, String> body) {
        String message = body == null ? null : body.get("message");
        if (message == null || message.isBlank()) {
            return Map.of("success", false, "error", "message is required");
        }

        // tryEmitNext 不会阻塞当前请求线程。
        // 它的职责只是“尝试把消息投递到流里”，具体是否投递成功由 EmitResult 说明。
        Sinks.EmitResult result = pushSink.tryEmitNext(message.trim());
        return Map.of("success", result.isSuccess(), "emitResult", result.name());
    }
}
