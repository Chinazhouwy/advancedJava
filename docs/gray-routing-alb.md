# 灰度分流与 ALB 模拟实验

这篇文档把简历中“Header 携带灰度 Token、Nginx/OpenResty + Lua 查询 Redis、请求进入灰度机器组”的经历，拆成一个可以在本机运行和验证的最小实验。

实验重点不是搭建一套生产级网关，而是把一次请求经过的每个环节看清楚：

```text
客户端
  │  X-Gray-Token: gray-user-001
  ▼
OpenResty（本地 ALB 模拟器）
  │  Lua 读取 Header
  │  查询 Redis 白名单/路由键
  ├──────────────► sf-normal（正常机器组）
  └──────────────► sf-gray（灰度机器组）
```

## 1. 先理解几个角色

### 1.1 ALB 是什么

ALB 是 Application Load Balancer，通常指支持 HTTP/HTTPS 七层转发的应用型负载均衡器。它不仅能把请求转发到一组后端机器，也可以根据域名、路径、HTTP Header、Cookie 等条件选择不同的目标组。

云厂商提供的 ALB 是托管服务，本机不需要、也不能直接安装一台“云 ALB”。本实验用 OpenResty 模拟 ALB 的核心行为：接收 HTTP 请求、执行路由规则、反向代理到不同的后端组。

### 1.2 OpenResty 为什么适合这个实验

OpenResty 可以看作 Nginx 加上 Lua 运行能力：

| 部件 | 在实验中的职责 | 面试里的对应概念 |
| --- | --- | --- |
| OpenResty/Nginx | 接收请求、反向代理、设置 Header | 接入层、网关、ALB 前置层 |
| Lua | 读取 Token、查 Redis、决定目标组 | 动态路由规则 |
| Redis | 保存灰度白名单和 Token 路由键 | 灰度开关、用户名单、路由配置 |
| sf-normal | 模拟正常版本服务 | 正常机器组 |
| sf-gray | 模拟灰度版本服务 | 灰度机器组 |

本地 `sf-normal` 和 `sf-gray` 不是两个真正的 Java 服务，而是返回不同 JSON 的轻量 HTTP 后端。这样可以把注意力放在路由链路上。

## 2. 代码目录

```text
dev/gray-routing/
├── docker-compose.yml          # Redis、网关、两个后端的编排
├── openresty/
│   ├── gateway.conf            # 网关监听器、目标组和代理规则
│   └── gray_router.lua         # 灰度判断逻辑
├── backends/
│   ├── normal.conf             # 正常机器组返回值
│   └── gray.conf               # 灰度机器组返回值
├── test/
│   └── gray-test.html          # 浏览器验证页面
└── README.md                   # 快速命令行说明
```

其中详细实验文档在：

```text
docs/gray-routing-alb.md
```

## 3. 启动环境

在 `advancedJava` 项目根目录执行：

```bash
docker compose -f dev/gray-routing/docker-compose.yml up -d
docker compose -f dev/gray-routing/docker-compose.yml ps
```

网关端口是 `8080`，Redis 为了避免和本机其他 Redis 冲突，映射到宿主机 `6380`：

```text
浏览器/客户端： http://localhost:8080
验证页面：     http://localhost:8080/test
宿主机 Redis：  localhost:6380
容器内 Redis：  redis:6379
```

`redis-init` 会在 Redis 健康后写入两种灰度规则：

```text
SADD gray:whitelist gray-user-001
SET  gray:token:gray-user-002 gray
```

这两条数据故意使用不同结构，是为了同时演示“集合白名单”和“单 Token 路由键”。

## 4. 浏览器验证页面

打开：

```text
http://localhost:8080/test
```

页面会真正向同一个网关发送请求，并读取响应头：

```text
X-Gray-Route
X-Gray-Decision
X-Backend-Group
```

页面中有四个快捷场景：

| 场景 | 发送的 Token | 预期路由 | 原因 |
| --- | --- | --- | --- |
| 无 Token | 无 | normal | 没有灰度身份 |
| 白名单 | `gray-user-001` | gray | 命中 `gray:whitelist` |
| 路由键 | `gray-user-002` | gray | 命中 `gray:token:gray-user-002 = gray` |
| 普通 Token | `normal-user-001` | normal | Redis 中没有灰度记录 |

这个页面不是静态假数据。按钮点击后，浏览器会调用 `/`，所以后端响应中的目标组可以直接证明 Lua 的判断结果。

## 5. 命令行验证

### 5.1 无 Token

```bash
curl -i http://localhost:8080/
```

预期看到：

```text
X-Gray-Route: normal
X-Gray-Decision: no-token
X-Backend-Group: normal
{"service":"sf","backendGroup":"normal","message":"normal machine group"}
```

### 5.2 命中白名单

```bash
curl -i -H 'X-Gray-Token: gray-user-001' http://localhost:8080/
```

预期看到：

```text
X-Gray-Route: gray
X-Gray-Decision: gray-token
X-Backend-Group: gray
{"service":"sf","backendGroup":"gray","message":"gray machine group"}
```

### 5.3 命中单 Token 路由键

```bash
curl -i -H 'X-Gray-Token: gray-user-002' http://localhost:8080/
```

预期同样进入 `gray` 机器组，但命中依据是：

```text
GET gray:token:gray-user-002
=> gray
```

### 5.4 普通 Token

```bash
curl -i -H 'X-Gray-Token: normal-user-001' http://localhost:8080/
```

预期：

```text
X-Gray-Route: normal
X-Gray-Decision: not-whitelisted
X-Backend-Group: normal
```

## 6. Redis 中的数据怎么查看

查看白名单集合：

```bash
docker compose -f dev/gray-routing/docker-compose.yml exec redis \
  redis-cli SMEMBERS gray:whitelist
```

查看单 Token 路由键：

```bash
docker compose -f dev/gray-routing/docker-compose.yml exec redis \
  redis-cli GET gray:token:gray-user-002
```

临时增加一个灰度 Token：

```bash
docker compose -f dev/gray-routing/docker-compose.yml exec redis \
  redis-cli SADD gray:whitelist interview-gray-token

curl -i -H 'X-Gray-Token: interview-gray-token' http://localhost:8080/
```

移除它：

```bash
docker compose -f dev/gray-routing/docker-compose.yml exec redis \
  redis-cli SREM gray:whitelist interview-gray-token
```

这里用 `SADD`/`SREM` 是因为白名单是集合；用 `SET`/`GET` 是因为单 Token 路由键是普通字符串。两者都是 Redis 的基础用法。

## 7. 一次请求是如何流转的

### 第一步：客户端携带 Header

客户端发送：

```http
GET / HTTP/1.1
Host: localhost:8080
X-Gray-Token: gray-user-001
```

Header 本身只是一个字符串。它并不天然可信，真正的含义来自网关对它的校验。

### 第二步：OpenResty 进入 access 阶段

`gateway.conf` 的核心入口是：

```nginx
location / {
    access_by_lua_file /etc/openresty/gray_router.lua;
}
```

`access_by_lua_file` 会在真正代理到后端之前执行 Lua。也就是说，Lua 是“先判断，再转发”。

### 第三步：Lua 读取 Token

```lua
local token = ngx.var.http_x_gray_token or ""
```

Nginx 会把请求头 `X-Gray-Token` 映射成变量 `ngx.var.http_x_gray_token`。如果请求没有这个 Header，就使用空字符串，避免后续拼接空值报错。

### 第四步：Lua 连接 Redis

```lua
local redis = require "resty.redis"
local red = redis:new()
red:set_timeout(500)
local ok, connect_error = red:connect("redis", 6379)
```

这里的 `redis` 不是宿主机地址，而是 Docker Compose 网络中的服务名。容器内通过 `redis:6379` 访问 Redis；宿主机才使用映射端口 `localhost:6380`。

### 第五步：查询两种灰度规则

```lua
local whitelisted = red:sismember("gray:whitelist", token)
local token_route = red:get("gray:token:" .. token)
```

逐行理解：

| 代码 | 含义 |
| --- | --- |
| `sismember` | 判断一个值是否在 Redis Set 集合中 |
| `gray:whitelist` | 白名单集合的 Key |
| `get` | 读取普通字符串 Key |
| `"gray:token:" .. token` | 把 Token 拼到 Key 后面，例如 `gray:token:gray-user-002` |

只要满足下面任一条件，就把路由设置为 `gray`：

```lua
if whitelisted == 1 or whitelisted == "1" or token_route == "gray" then
    route = "gray"
    decision = "gray-token"
end
```

Redis Lua 客户端返回的数字在不同版本或场景下可能表现为数字 `1` 或字符串 `"1"`，所以代码同时判断两种形式。

### 第六步：执行内部跳转

```lua
ngx.var.gray_route = route
ngx.var.gray_decision = decision

if route == "gray" then
    return ngx.exec("@gray")
end

return ngx.exec("@normal")
```

`@gray` 和 `@normal` 是 Nginx 的命名 location：

```nginx
location @normal {
    proxy_pass http://sf_normal_backend;
}

location @gray {
    proxy_pass http://sf_gray_backend;
}
```

因此 `ngx.exec("@gray")` 的意思不是返回一段文字，而是把当前请求交给 `@gray` 这条代理规则继续处理。

### 第七步：后端用响应头证明自己

正常后端返回：

```text
X-Backend-Group: normal
```

灰度后端返回：

```text
X-Backend-Group: gray
```

最终可以同时对照三类信息：

```text
X-Gray-Route      网关最终选择的路由
X-Gray-Decision   网关为什么这么选择
X-Backend-Group   实际收到请求的后端组
```

如果前两者和第三者不一致，就说明路由 Header 没有正确传递，或者代理配置存在问题。

### 7.1 `ngx` 是什么

`ngx` 是 OpenResty 的 Lua 扩展提供的全局对象，不是 Lua 标准库的一部分。它把 Nginx 的请求上下文、Header、内部路由、日志和响应控制能力暴露给 Lua：

```text
Lua 脚本
  -> ngx 对象
  -> OpenResty/Nginx 请求处理能力
```

如果用普通 Lua 解释器执行下面的代码，`ngx` 并不存在：

```lua
print(ngx.var.http_x_gray_token)
```

只有 OpenResty 在请求处理阶段执行 Lua 时，才会注入 `ngx` 对象。

当前 Demo 中最重要的几个 API：

| 代码 | 作用 | Java 类比 |
| --- | --- | --- |
| `ngx.var` | 读取或设置 Nginx 变量 | 请求上下文、环境变量 |
| `ngx.req` | 读取或修改当前请求 | `HttpServletRequest` 的 Header 操作 |
| `ngx.exec` | 内部跳转到另一个 Nginx location | `RequestDispatcher.forward` |
| `ngx.log` | 写入 Nginx/OpenResty 日志 | `logger.warn()` |
| `ngx.null` | 表示特殊的空值 | Redis 客户端返回的 NULL 哨兵 |

#### 读取请求 Header

```lua
local token = ngx.var.http_x_gray_token or ""
```

请求中的：

```http
X-Gray-Token: gray-user-001
```

会被 Nginx 变量规则转换成：

```text
ngx.var.http_x_gray_token
```

也可以使用请求 Header API：

```lua
local headers = ngx.req.get_headers()
local token = headers["X-Gray-Token"] or ""
```

本 Demo 使用 `ngx.var`，因为它写法更短。

#### 修改当前请求 Header

```lua
ngx.req.set_header("X-Gray-Route", route)
```

这会修改当前请求的 Header。后面进入代理阶段时，下游服务就可以继续看到这个路由信息。生产环境中不能直接信任客户端传入的同名 Header，应该先由可信入口校验，再由网关重写。

#### 内部跳转到命名 location

```lua
return ngx.exec("@gray")
```

它不是 HTTP 302，也不会让浏览器重新发起请求，而是在当前请求内部交给：

```nginx
location @gray {
    proxy_pass http://sf_gray_backend;
}
```

继续处理。用 Java 的概念类比，它更接近：

```java
request.getRequestDispatcher("/gray").forward(request, response);
```

#### 写日志

```lua
ngx.log(ngx.WARN, "redis unavailable")
```

`ngx.WARN` 表示日志级别。当前 Demo 只记录 Redis 故障，不记录用户 Token，避免把灰度身份直接写进日志。

#### 判断 Redis 的空值

```lua
local token_route = red:get("gray:token:" .. token)

if token_route ~= ngx.null then
    -- Redis 中存在这个 Key
end
```

`resty.redis` 查询不存在的 Key 时，通常返回 `ngx.null`，而不是普通 Lua 的 `nil`。因此代码需要专门判断它。

可以把整段 Lua 逻辑理解成一个“运行在 Nginx 请求生命周期中的 Java 过滤器”：

```text
请求进入
  -> ngx 读取 Header
  -> 查询 Redis
  -> ngx 设置路由变量
  -> ngx.exec 内部转发
  -> proxy_pass 调用下游 SF
```

## 8. 和 DMZ 调用 SF 问题的对应关系

你之前描述的生产问题可以抽象成：

```text
DMZ 灰度实例
  │  代码只调用 sf.example.com
  │  没有携带灰度上下文
  ▼
SF 入口 ALB
  │  看不到灰度 Token
  ▼
默认进入 normal 机器组
```

域名只能告诉请求“访问哪个入口”，不能自动告诉下游“这个请求属于哪个灰度批次”。所以 DMZ 调用 SF 时，需要继续传递灰度上下文。

Java 使用 `HttpClient` 的最小透传写法：

```java
HttpRequest request = HttpRequest.newBuilder(URI.create("http://sf.example.com/api"))
        .header("X-Gray-Token", inboundGrayToken)
        .header("X-Request-Id", requestId)
        .GET()
        .build();
```

如果项目使用 Feign、RestTemplate 或 WebClient，本质也是同一件事：在调用下游前，把入站请求中的灰度上下文写入出站请求。

更稳妥的生产设计通常是：

1. 外部 Header 先在可信入口校验，不能直接信任任意客户端提交的 `X-Gray-Token`。
2. 入口校验通过后，重写一个内部 Header，例如 `X-Gray-Route: gray`。
3. 下游只信任来自内部网关的 Header，边界层要删除外部伪造的同名 Header。
4. Token 具备签名、过期时间和日志脱敏能力。
5. 明确 Redis 不可用时是“故障转正常”还是“拒绝请求”，不能靠默认行为碰运气。

## 9. 测试矩阵

| 编号 | 请求条件 | Redis 命中情况 | 预期 `X-Gray-Route` | 预期后端 |
| --- | --- | --- | --- | --- |
| T1 | 无 Header | 不查询 | `normal` | `normal` |
| T2 | `gray-user-001` | 命中 Set | `gray` | `gray` |
| T3 | `gray-user-002` | 命中 String Key | `gray` | `gray` |
| T4 | `normal-user-001` | 未命中 | `normal` | `normal` |
| T5 | 临时加入白名单的 Token | 命中 Set | `gray` | `gray` |
| T6 | 移除白名单后的 Token | 未命中 | `normal` | `normal` |

前四个场景可以直接在浏览器测试页完成；T5、T6 用命令行修改 Redis 后再验证。

## 10. Redis 不可用时的降级

当前 Demo 的策略是：Redis 连接失败时记录 Warning，并回到 normal：

```lua
if not ok then
    ngx.log(ngx.WARN, "redis unavailable: ", connect_error or "unknown error")
    decision = "redis-unavailable-fallback-normal"
end
```

这个策略的优点是灰度判断故障不会直接扩大成全站不可用，缺点是 Redis 故障期间灰度请求会落到正常版本。生产系统要根据业务风险选择：

| 策略 | 优点 | 风险 |
| --- | --- | --- |
| 故障转 normal | 可用性优先，影响面较小 | 灰度版本请求可能回到旧版本 |
| 故障转 gray | 保持灰度一致性 | 灰度版本可能承受非预期流量 |
| 拒绝请求 | 不产生错误路由 | 可用性下降，用户直接失败 |

无论选哪种，都需要监控 Redis 连接失败次数、路由结果数量和目标组错误率。

## 11. 常见问题排查

### 页面打不开

先看容器和端口：

```bash
docker compose -f dev/gray-routing/docker-compose.yml ps
lsof -nP -iTCP:8080 -sTCP:LISTEN
```

如果 `8080` 被其他服务占用，可以把 `docker-compose.yml` 的端口改成 `18080:80`，然后访问 `http://localhost:18080/test`。

### 所有请求都进 normal

依次检查：

```bash
docker compose -f dev/gray-routing/docker-compose.yml exec redis \
  redis-cli SMEMBERS gray:whitelist

docker compose -f dev/gray-routing/docker-compose.yml exec redis \
  redis-cli GET gray:token:gray-user-002

docker compose -f dev/gray-routing/docker-compose.yml logs alb-simulator
```

另外确认请求头的拼写是 `X-Gray-Token`。浏览器页面和 `curl` 都应该使用同一个 Header 名称。

### OpenResty 配置报 DNS 错误

容器启动时会解析 Compose 服务名。当前网关配置中有：

```nginx
resolver 127.0.0.11 ipv6=off valid=10s;
```

`127.0.0.11` 是 Docker 内置 DNS。它让 OpenResty 能解析 `redis`、`sf-normal` 和 `sf-gray` 这些 Compose 服务名。

可以直接检查配置：

```bash
docker exec advanced-java-gray-alb \
  /usr/local/openresty/bin/openresty -t
```

### 配置改了但页面还是旧的

配置文件和测试页都是通过 volume 挂载的。修改后重启网关容器：

```bash
docker compose -f dev/gray-routing/docker-compose.yml restart alb-simulator
```

测试页面使用了 `Cache-Control: no-store`，仍然建议浏览器强制刷新一次。

## 12. 停止和清理

停止容器：

```bash
docker compose -f dev/gray-routing/docker-compose.yml down
```

如果只想重启网关，不影响 Redis 中的实验数据：

```bash
docker compose -f dev/gray-routing/docker-compose.yml restart alb-simulator
```

## 13. 面试中可以怎么说

> 我们在接入层通过 Nginx/OpenResty 的 Lua 脚本读取请求 Header 中的灰度 Token，再查询 Redis 中的灰度白名单或路由配置，将请求转发到 normal 或 gray 机器组。后来发现 DMZ 调用 SF 时虽然代码使用同一个域名，但没有完整透传灰度上下文，导致灰度请求在 SF 入口被当成普通流量。后续通过灰度 Header 透传、灰度目标组和入口层统一路由，保证一次请求链路上的灰度身份保持一致。云上可以使用 ALB 的七层监听器和转发规则承载入口，本地则用 OpenResty/Nginx 模拟验证。

如果面试官继续追问，可以补充三个点：

1. **为什么 Redis**：名单和路由规则需要动态变更，不希望每次改配置都重启网关；Redis 的读延迟低，也适合保存集合和带过期时间的键。
2. **Header 是否可信**：外部 Header 不能直接信任，入口层需要校验、重写和清理，内部服务只接受可信网关生成的灰度标记。
3. **故障怎么办**：先明确 Redis 不可用的降级策略，再配合监控、告警和灰度目标组的独立容量，避免路由组件故障扩大成全链路故障。

## 14. 实验边界

这个 Demo 用于学习“路由判断和上下文透传”，没有实现生产系统的全部能力：

- 没有真实云 ALB、证书、WAF 和多可用区容灾。
- Token 是明文示例，不代表生产安全方案。
- Redis 是单实例，没有 Sentinel、Cluster 和持久化策略。
- 后端只是 OpenResty 返回 JSON，不是真实 SF 服务。
- 当前规则是“命中灰度就全部进入 gray”，没有按比例分流和自动回滚。

所以它适合用来验证面试中的核心链路，不能直接当作生产配置复制。
