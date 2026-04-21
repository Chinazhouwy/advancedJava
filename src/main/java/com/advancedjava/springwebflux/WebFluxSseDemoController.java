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
 * Minimal WebFlux SSE demo.
 *
 * Endpoints:
 * - GET  /webflux-demo/stream : SSE stream (heartbeat + pushed messages)
 * - POST /webflux-demo/push   : push a message to current SSE subscribers
 */
@RestController
@RequestMapping("/webflux-demo")
public class WebFluxSseDemoController {

    private final Sinks.Many<String> pushSink = Sinks.many().multicast().onBackpressureBuffer();

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Map<String, Object>>> stream() {
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

        Flux<ServerSentEvent<Map<String, Object>>> pushed =
                pushSink.asFlux()
                        .map(
                                msg ->
                                        ServerSentEvent.<Map<String, Object>>builder()
                                                .event("message")
                                                .data(
                                                        Map.of(
                                                                "source", "push",
                                                                "message", msg,
                                                                "at", Instant.now().toString()))
                                                .build());

        return Flux.merge(heartbeat, pushed);
    }

    @PostMapping("/push")
    public Map<String, Object> push(@RequestBody Map<String, String> body) {
        String message = body == null ? null : body.get("message");
        if (message == null || message.isBlank()) {
            return Map.of("success", false, "error", "message is required");
        }

        Sinks.EmitResult result = pushSink.tryEmitNext(message.trim());
        return Map.of("success", result.isSuccess(), "emitResult", result.name());
    }
}
