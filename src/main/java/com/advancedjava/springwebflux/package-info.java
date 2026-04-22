/**
 * Spring WebFlux 教学示例包。
 *
 * <p>本包不是为了提供复杂业务功能，而是为了把响应式编程里最常见的概念拆成几个可单独运行、
 * 可单独观察的小示例，方便在学习和面试复习时逐个理解。
 *
 * <p>内容结构说明：
 * 1. {@code SpringWebFluxDemoApplication}: 启动 WebFlux 示例应用。
 * 2. {@code WebFluxLearningController}: 演示 Mono、Flux、SSE、Sink、错误恢复、背压等常见接口形态。
 * 3. {@code WebFluxSseDemoController}: 聚焦 SSE 长连接与服务端主动推送。
 * 4. {@code ReactorCoreDemo}: 演示 Reactor Core 基础操作符。
 * 5. {@code ReactorErrorHandlingDemo}: 演示响应式错误处理与超时兜底。
 * 6. {@code ReactorBackpressureDemo}: 演示生产快、消费慢时的背压行为。
 * 7. {@code AsyncInputDemo}: 演示如何用 Sink 把“外部输入”桥接回响应式链路。
 *
 * <p>阅读建议：
 * 1. 先看 {@code ReactorCoreDemo} 建立 Mono/Flux 基础认知。
 * 2. 再看 {@code WebFluxLearningController}，理解这些类型如何映射成 HTTP 接口。
 * 3. 最后看错误处理、背压、SSE 和异步输入桥接，理解真实系统里常见的控制流问题。
 *
 * <p>学习重点：
 * 1. 在 WebFlux 中尽量返回 {@code Mono}/{@code Flux}，让框架负责订阅与回写响应。
 * 2. 只有在 main 方法、测试、教学 Demo 中才适合使用 {@code block()}。
 * 3. {@code Sinks} 适合把“框架外部事件”安全地桥接进响应式流。
 * 4. 错误、超时、重试、背压都要作为“数据流生命周期的一部分”来理解。
 */
package com.advancedjava.springwebflux;
