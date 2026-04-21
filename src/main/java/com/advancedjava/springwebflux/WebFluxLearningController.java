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
 * Learning-oriented WebFlux endpoints.
 */
@RestController
@RequestMapping("/learn/webflux")
public class WebFluxLearningController {

    private final Sinks.Many<String> chatSink = Sinks.many().multicast().onBackpressureBuffer();

    @GetMapping("/mono/hello")
    public Mono<Map<String, Object>> monoHello(@RequestParam(defaultValue = "world") String name) {
        return Mono.just(
                Map.of(
                        "type", "mono",
                        "message", "hello, " + name,
                        "at", Instant.now().toString()));
    }

    @GetMapping("/flux/numbers")
    public Flux<Map<String, Object>> fluxNumbers(@RequestParam(defaultValue = "5") int count) {
        int safeCount = Math.max(1, Math.min(count, 50));

        return Flux.range(1, safeCount)
                .delayElements(Duration.ofMillis(200))
                .map(
                        i ->
                                Map.of(
                                        "type", "flux",
                                        "index", i,
                                        "square", i * i,
                                        "at", Instant.now().toString()));
    }

    @GetMapping(value = "/sse/ticks", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Map<String, Object>>> sseTicks() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(
                        tick ->
                                ServerSentEvent.<Map<String, Object>>builder()
                                        .event("tick")
                                        .id(String.valueOf(tick))
                                        .data(
                                                Map.of(
                                                        "tick", tick,
                                                        "at", Instant.now().toString()))
                                        .build());
    }

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

        return Flux.merge(heartbeat, chat);
    }

    @PostMapping("/chat/push")
    public Mono<Map<String, Object>> pushChat(@RequestBody Map<String, String> body) {
        String message = body == null ? null : body.get("message");
        if (message == null || message.isBlank()) {
            return Mono.just(Map.of("success", false, "error", "message is required"));
        }

        Sinks.EmitResult emitResult = chatSink.tryEmitNext(message.trim());
        return Mono.just(Map.of("success", emitResult.isSuccess(), "emitResult", emitResult.name()));
    }

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
