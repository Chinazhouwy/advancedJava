# Spring WebFlux Learning Guide

这个目录是一套从入门到上手的最小示例，目标是让你看懂：
- `Mono` / `Flux` 是什么
- WebFlux Controller 为什么返回 `Mono/Flux`
- SSE 是怎么持续推送的
- `Sinks` 为什么能做“外部输入 -> 唤醒内部流”

## 1. 先建立最小认知

1. `Mono<T>`: 0 或 1 个异步结果。
2. `Flux<T>`: 0 到 N 个异步结果。
3. WebFlux 核心是“非阻塞 + 事件流”，不是“每个请求占一个线程阻塞等待”。

## 2. 文件说明

- `SpringWebFluxDemoApplication`: 启动类。
- `WebFluxLearningController`: 学习用接口集合（建议主看）。
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

## 4. 接口逐个解释（含参数）

### 4.1 `GET /learn/webflux/mono/hello`
- 参数：`name`（可选，默认 `world`）
- 返回：`Mono<Map<String,Object>>`
- 你会看到：一次性返回一个 JSON

示例：
```bash
curl "http://localhost:8080/learn/webflux/mono/hello?name=alice"
```

### 4.2 `GET /learn/webflux/flux/numbers`
- 参数：`count`（可选，默认 5，代码里限制在 1~50）
- 返回：`Flux<Map<String,Object>>`
- 行为：每 200ms 返回一条数据（index、square）

示例：
```bash
curl "http://localhost:8080/learn/webflux/flux/numbers?count=8"
```

### 4.3 `GET /learn/webflux/sse/ticks`
- 参数：无
- 返回：`Flux<ServerSentEvent<Map<String,Object>>>`
- 行为：每 1 秒推送一个 SSE `event=tick`
- 关键：`produces = text/event-stream`

示例：
```bash
curl -N "http://localhost:8080/learn/webflux/sse/ticks"
```

### 4.4 `GET /learn/webflux/sse/chat`
- 参数：无
- 返回：SSE 流（`heartbeat` + `chat` 两类事件）
- 行为：
  - 心跳每 3 秒推送一次
  - `chat` 来自 `/chat/push` 手动发送

先订阅：
```bash
curl -N "http://localhost:8080/learn/webflux/sse/chat"
```

### 4.5 `POST /learn/webflux/chat/push`
- 请求体：`{"message":"hello"}`
- 返回：是否发送成功 + `emitResult`
- 你需要先有 `sse/chat` 订阅者，才更容易看到效果

示例：
```bash
curl -X POST "http://localhost:8080/learn/webflux/chat/push" \
  -H "Content-Type: application/json" \
  -d '{"message":"hello from curl"}'
```

### 4.6 `GET /learn/webflux/error/fallback`
- 参数：无
- 行为：随机抛错，然后用 `onErrorResume` 返回兜底数据
- 重点：错误也能在流中被“转换”为正常响应

示例：
```bash
curl "http://localhost:8080/learn/webflux/error/fallback"
```

### 4.7 `GET /learn/webflux/backpressure/slow-consumer`
- 参数：无
- 行为：生产快、消费慢，观察背压缓冲效果

示例：
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

## 6. 常见操作符速记

- `map(fn)`: 同步一对一转换
- `filter(fn)`: 过滤不满足条件的元素
- `flatMap(fn)`: 映射为异步 Publisher 后再摊平
- `zip(a,b)`: 等多个异步源都返回后再组合
- `onErrorReturn(v)`: 出错时返回固定值
- `onErrorResume(fn)`: 出错时切换到备用流
- `retryWhen(...)`: 失败重试
- `timeout(t, fallback)`: 超时走兜底

## 7. 推荐学习顺序

1. 先跑 `ReactorCoreDemo`，掌握 `map/flatMap`。
2. 再看 `WebFluxLearningController` 的 `mono/flux` 接口。
3. 再看 `sse/chat + push`，理解事件驱动。
4. 最后看 `AsyncInputDemo`，对照狼人项目里的 `WebUserInput`。

## 8. 编译说明

当前工程里 `com.advancedjava.ta4j.ClassicMAChartDemo` 存在与本目录无关的编译错误，
所以全量 `mvn compile` 可能失败。你学习 `springwebflux` 不受影响，可直接运行 Web 启动类和本目录 Demo。
