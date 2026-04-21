# Spring WebFlux Learning Guide

这个目录是一套从入门到上手的最小示例。

## 1. 你先理解的三件事

1. `Mono<T>`: 0 或 1 个异步结果。
2. `Flux<T>`: 0 到 N 个异步结果。
3. WebFlux 核心是“非阻塞 + 事件流”，不是“多线程阻塞等待”。

## 2. 文件说明

- `SpringWebFluxDemoApplication`: 启动类。
- `WebFluxLearningController`: 学习用接口集合。
- `WebFluxSseDemoController`: 更聚焦 SSE 推送的简化版示例。
- `AsyncInputDemo`: 用 `Sinks.One` 演示“等待输入 -> 提交输入 -> 唤醒”的桥接。
- `ReactorCoreDemo`: Reactor 基础操作符。
- `ReactorBackpressureDemo`: 背压与慢消费者。
- `ReactorErrorHandlingDemo`: 错误恢复、重试、超时兜底。

## 3. 启动 Web 示例

```bash
cd /Users/chinazhouwy/doc/code/advancedJava
mvn spring-boot:run -Dspring-boot.run.main-class=com.advancedjava.springwebflux.SpringWebFluxDemoApplication
```

## 4. 按顺序练接口

### Step A: Mono

```bash
curl "http://localhost:8080/learn/webflux/mono/hello?name=alice"
```

### Step B: Flux

```bash
curl "http://localhost:8080/learn/webflux/flux/numbers?count=8"
```

### Step C: SSE

```bash
curl -N "http://localhost:8080/learn/webflux/sse/ticks"
```

### Step D: SSE + 手动推送

先订阅：

```bash
curl -N "http://localhost:8080/learn/webflux/sse/chat"
```

再推送（另开终端）：

```bash
curl -X POST "http://localhost:8080/learn/webflux/chat/push" \
  -H "Content-Type: application/json" \
  -d '{"message":"hello from curl"}'
```

### Step E: 错误恢复

```bash
curl "http://localhost:8080/learn/webflux/error/fallback"
```

### Step F: 慢消费者/背压观感

```bash
curl "http://localhost:8080/learn/webflux/backpressure/slow-consumer"
```

## 5. 控制台 Demo（不依赖 HTTP）

```bash
cd /Users/chinazhouwy/doc/code/advancedJava
mvn -q -DskipTests compile
java -cp target/classes com.advancedjava.springwebflux.ReactorCoreDemo
java -cp target/classes com.advancedjava.springwebflux.ReactorBackpressureDemo
java -cp target/classes com.advancedjava.springwebflux.ReactorErrorHandlingDemo
java -cp target/classes com.advancedjava.springwebflux.AsyncInputDemo
```

## 6. 推荐学习顺序

1. 先跑 `ReactorCoreDemo`，掌握 `map/flatMap`。
2. 再看 `WebFluxLearningController` 里的 `mono/flux` 接口。
3. 再看 `sse/chat` + `push`，理解事件驱动。
4. 最后看 `AsyncInputDemo`，对照狼人项目里的 `WebUserInput` 机制。
