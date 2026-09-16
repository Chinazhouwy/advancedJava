# 本地灰度分流与 ALB 模拟 Demo

详细的原理、代码拆解、测试矩阵和面试表达见：[灰度分流与 ALB 模拟实验](../../docs/gray-routing-alb.md)。

启动后也可以直接打开同源验证页：[http://localhost:8080/test](http://localhost:8080/test)。

这个 Demo 模拟面试中提到的链路：

```text
请求
  -> OpenResty（本地 ALB/网关模拟器）
  -> Lua 读取 X-Gray-Token
  -> Redis 检查灰度白名单
  -> normal 机器组 / gray 机器组
```

## ALB 是什么

ALB 通常指 Application Load Balancer，应用型负载均衡器。它工作在 HTTP/HTTPS 等七层协议上，除了轮询后端机器，还可以按照域名、路径、HTTP Header、Cookie 等条件把请求转发到不同的后端服务器组。

云上的 ALB 是托管服务；本机不能安装一台真正的云 ALB，但可以用 OpenResty、Nginx、HAProxy 或 Envoy 模拟它的核心行为。本 Demo 使用 OpenResty，因为它同时支持 Nginx 反向代理和 Lua 脚本。

## 启动

在项目根目录执行：

```bash
docker compose -f dev/gray-routing/docker-compose.yml up -d
docker compose -f dev/gray-routing/docker-compose.yml ps
```

入口地址：

```text
http://localhost:8080
```

## 观察正常流量和灰度流量

### 1. 没有 Token：进入正常机器组

```bash
curl -i http://localhost:8080/
```

预期响应头和内容中包含：

```text
X-Gray-Route: normal
X-Backend-Group: normal
"backendGroup":"normal"
```

### 2. 白名单 Token：进入灰度机器组

```bash
curl -i -H 'X-Gray-Token: gray-user-001' http://localhost:8080/
```

`gray-user-001` 被放在 Redis 的 `gray:whitelist` 集合中，预期进入 gray 机器组：

```text
X-Gray-Route: gray
X-Backend-Group: gray
"backendGroup":"gray"
```

### 3. Redis 路由键：也进入灰度机器组

```bash
curl -i -H 'X-Gray-Token: gray-user-002' http://localhost:8080/
```

这个 Token 使用的是：

```text
gray:token:gray-user-002 = gray
```

### 4. 普通 Token：回到正常机器组

```bash
curl -i -H 'X-Gray-Token: normal-user-001' http://localhost:8080/
```

因为 Redis 中没有这个 Token 的灰度记录，所以回到 normal 机器组。

## 查看和修改灰度名单

查看白名单：

```bash
docker compose -f dev/gray-routing/docker-compose.yml exec redis \
  redis-cli SMEMBERS gray:whitelist
```

临时增加一个灰度用户：

```bash
docker compose -f dev/gray-routing/docker-compose.yml exec redis \
  redis-cli SADD gray:whitelist interview-gray-token

curl -i -H 'X-Gray-Token: interview-gray-token' http://localhost:8080/
```

移除灰度用户：

```bash
docker compose -f dev/gray-routing/docker-compose.yml exec redis \
  redis-cli SREM gray:whitelist interview-gray-token
```

停止环境：

```bash
docker compose -f dev/gray-routing/docker-compose.yml down
```

## 代码对应的业务含义

```lua
local whitelisted = red:sismember("gray:whitelist", token)
local token_route = red:get("gray:token:" .. token)
```

这两行分别对应：

```text
Token 是否在灰度白名单中
Token 是否有明确的 gray 路由配置
```

判断为灰度后：

```lua
return ngx.exec("@gray")
```

请求进入 `sf-gray` 目标组；否则进入 `sf-normal` 目标组。

## 你遇到的 DMZ 调用 SF 问题

问题可以这样描述：

```text
DMZ 灰度实例收到灰度请求
  -> 代码只按 sf.example.com 调用 SF
  -> 没有继续传递灰度 Token
  -> SF 入口无法判断灰度身份
  -> 默认路由到 normal 机器组
```

域名只解决“请求到哪个入口”，不一定能表达“请求属于哪个灰度批次”。常见解决办法有三种：

1. **透传灰度 Header**：DMZ 调用 SF 时继续带上 `X-Gray-Token`，SF 的 ALB/OpenResty 再次判断。
2. **灰度专用域名或服务发现标签**：例如 `sf-gray.example.com`，或者给 gray 实例使用独立的服务标签。
3. **内部可信路由 Header**：边缘层验证外部 Token 后，重写成内部可信的 `X-Gray-Route: gray`，下游只信任网关写入的 Header。

Java 使用 `HttpClient` 调用下游时，最小的透传方式类似：

```java
HttpRequest request = HttpRequest.newBuilder(URI.create("http://sf.example.com/api"))
        .header("X-Gray-Token", inboundGrayToken)
        .header("X-Request-Id", requestId)
        .GET()
        .build();
```

实际生产环境还应考虑：Token 签名、过期时间、Header 防伪、日志脱敏，以及 Redis 故障时的降级策略。

## 面试表达

> 我们在接入层通过 Nginx/OpenResty 的 Lua 脚本读取请求 Header 中的灰度 Token，再查询 Redis 中的灰度白名单或路由配置，将请求转发到 normal 或 gray 机器组。后来发现 DMZ 调用 SF 时虽然代码使用同一个域名，但没有完整透传灰度上下文，导致灰度请求在 SF 入口被当成普通流量。后续通过灰度 Header 透传、灰度专用目标组以及入口层统一路由，保证一次请求链路上的灰度身份保持一致。云上可以使用 ALB 的七层监听器和转发规则承载入口，本地则用 OpenResty/Nginx 模拟验证。
