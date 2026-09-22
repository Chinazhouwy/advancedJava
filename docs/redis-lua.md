# Redis 高级用法（Lua 脚本 + Java）

代码位置：`src/main/java/com/advancedjava/redis/` —— 8 个独立 `main`，每个演示一个高频场景。
逐 Demo 详解见包内 [README.md](../src/main/java/com/advancedjava/redis/README.md)。

## 八个 Demo 一览

| main 类 | 主题 | 面试考点 |
| --- | --- | --- |
| `DistributedLockLuaDemo` | 分布式锁 Lua 原子解锁 | GET+DEL 拆两步会误删他人锁 |
| `SlidingWindowRateLimitDemo` | ZSET + Lua 滑动窗口限流 | 固定窗口临界突刺；四步必须原子 |
| `BatchIncrLuaDemo` | 一次 EVAL 原子批量累加 | Pipeline vs Lua：合并往返 ≠ 原子性 |
| `CasUpdateLuaDemo` | Lua CAS 比较并交换 | WATCH/MULTI 乐观事务 vs 一个脚本 |
| `DelayQueueLuaDemo` | ZSET + Lua 延迟队列原子领取 | 取出+删除不原子会重复消费 |
| `StockOversellLuaDemo` | 防超卖 + Redis 7 FUNCTION | EVAL vs FCALL；查-判-扣三步原子 |
| `StreamConsumeDemo` | Stream 消费组消息队列 | Pub/Sub、List、Stream 三者对比；ACK 与 PEL |
| `VectorSearchDemo` | RediSearch 向量检索 + RedisJSON | 语义检索 vs 关键词；DIALECT 2；混合过滤 |

## 核心原理：为什么这些场景离不开 Lua

Redis 命令是单线程执行的，**单条命令天然原子，多条命令的组合不是**。
"读→判断→写" 这类逻辑一旦拆开发送，并发下必然出现竞态：

1. 解锁时先 GET 再 DEL —— 中间锁刚好过期被 B 抢到，A 就删了 B 的锁；
2. 限流时先统计再记录 —— 两个客户端同时数都以为没超限，实际超发；
3. 扣库存时先查余量再 DECRBY —— 余量 1 时两人同时查到通过，超卖。

Lua 脚本把整段逻辑交给服务端一次执行，中途不插入其他客户端的命令，
上面三类问题一次性消失。代价是脚本执行期间阻塞其他命令，所以脚本要短、循环量可控。

## 运行方式

启动本地 Redis（**用 Redis 8**，它的官方镜像已内置 search / ReJSON 模块）：

```bash
docker run -d --name redis-demo -p 6379:6379 redis:8-alpine
# 若还在用 Redis 7.x，Demo 8 需要 redis-stack 镜像（普通 7.x 镜像不带 search/json 模块）
# docker run -d --name redis-demo -p 6379:6379 redis/redis-stack-server:latest
```

依次运行（或 IDE 里直接跑各文件的 main）：

```bash
mvn compile
mvn exec:java -Dexec.mainClass=com.advancedjava.redis.DistributedLockLuaDemo
mvn exec:java -Dexec.mainClass=com.advancedjava.redis.SlidingWindowRateLimitDemo
mvn exec:java -Dexec.mainClass=com.advancedjava.redis.BatchIncrLuaDemo
mvn exec:java -Dexec.mainClass=com.advancedjava.redis.CasUpdateLuaDemo
mvn exec:java -Dexec.mainClass=com.advancedjava.redis.DelayQueueLuaDemo
mvn exec:java -Dexec.mainClass=com.advancedjava.redis.StockOversellLuaDemo
mvn exec:java -Dexec.mainClass=com.advancedjava.redis.StreamConsumeDemo
mvn exec:java -Dexec.mainClass=com.advancedjava.redis.VectorSearchDemo
```

所有 key 统一 `demo:` 前缀，`redis-cli KEYS 'demo:*'` 可随时清掉。

## 实测输出

```text
=== DistributedLockLuaDemo ===
加锁: 成功, token=5c8994b4
错误 token 解锁: 0（0=被拒绝，防误删他人锁）
正确 token 解锁: 1（1=删除成功）
锁是否还存在: false（false=已释放）

=== SlidingWindowRateLimitDemo ===
请求 1~5: 放行（剩余配额 4→0）
请求 6~8: 拒绝（窗口内已满 5 个）
1.1 秒后再次请求: 恢复放行（窗口滑过，旧点已清理）

=== BatchIncrLuaDemo ===
一次 EVAL 累加 [10, 3, 7] -> [10, 3, 7]
再来一轮 [1,1,1]      -> [11, 4, 8]

=== CasUpdateLuaDemo ===
CAS(v0 -> v9): 0（拒绝） → CAS(v1 -> v2): 1（成功） → CAS(v1 -> v3): 0（拒绝）
最终值: v2（只有链式推进生效）

=== DelayQueueLuaDemo ===
立即领取: {}（未到期）
300ms 后领取: [close-order-A(200ms后)]
再过 200ms 领取: [close-order-B(400ms后)]；剩余 1（C 未到期）

=== StockOversellLuaDemo ===
EVAL 抢购: 抢到 5 人, 库存不足被拒 5 人, 最终余量 0（拆成两条命令必超卖）
FCALL stock_decr 扣 1 件 -> 余量 2（函数只注册一次，之后按名调用）

=== StreamConsumeDemo ===
XADD -> 1790051250465-0 {amount=10, orderId=ORD-1}   （共 3 条，ID 服务端自增）
XGROUP CREATE: OK
worker-A 领到 ...465-0 与 ...468-0
worker-B 领到 ...470-0                              （组内分发，不是广播）
XPENDING 汇总: 总数=3, 按消费者={worker-B=1, worker-A=2}
XACK worker-A 的第一条: 1 → ACK 后总数 2

=== VectorSearchDemo ===
① 查询「狗在奔跑」top2（score 是余弦距离，越小越像）
   命中 2 条
   - demo:doc:1 [score=0.003, title=金毛犬在公园奔跑, category=pet]
   - demo:doc:4 [score=0.114, title=宠物猫和狗粮, category=pet]
② category=tech 过滤 + KNN → 命中 1 条（机器学习的向量检索）
③ 关键词 @title:狗 → 命中 0 条（中文默认不分词）
④ 关键词 @title:金毛犬* → 命中 1 条（必须字面命中开头）
```

## 踩坑记录（真实调试付出，都写在代码注释里）

1. **Jedis 不是线程安全的**：防超卖 Demo 里 10 个抢购线程共享一个连接，RESP 协议响应
   立刻错乱（`Unknown reply` / broken connection）。每个线程必须自己 `new Jedis(...)`。
2. **FUNCTION API 有版本差异**：Redis 7.0 注册函数用 `redis.register_function(name, fn)`；
   `Lua.registerFunction` 是错的（全局变量不存在），带 flags 的新签名要 7.4+。
3. **Stream 消费 ID 必须传 `">"`**：用 `StreamEntryID.XREADGROUP_UNDELIVERED_ENTRY`；
   传 `new StreamEntryID()` 会序列化成空串，服务端当成已投递位置 → 一条都读不到**且不报错**。
4. **KNN 需要 DIALECT 2**：Jedis 里 `Query.dialect(2)` 不能省，否则报
   `Syntax error at offset 1 near >[`，错误信息完全不提 dialect，很难猜。
5. **JSON 索引字段类型要对**：给字符串字段声明 NUMERIC，文档会静默索引失败
   （`FT.CREATE` 不报错、`num_docs=0`），要靠 `FT.INFO` 的 `last indexing error` 定位。
6. **用 `jsonSetWithEscape`**：新版 stack 对 `JSON.SET` 的 path 校验更严，
   `jsonSet(key, Path2.ROOT_PATH, obj)` 报 `key must be a string`。

## 单机 / 主从 / Cluster：这套代码在各部署形态下的表现

**先纠正一个常见误区：Cluster ≠ 没有主从。** Redis Cluster 的每个分片本身就是一个主从组
（1 主 + N 副本），所以"是集群就不用考虑主从"不成立——两种问题是**正交**的：

| 问题域 | 根源 | 影响谁 | 解法 |
|---|---|---|---|
| CROSSSLOT | 路由：一条命令/EVAL 的多个 key 落在不同 slot | 只发生在 **Cluster** | hash tag `{...}` 归槽，或按槽分组多次 EVAL |
| 锁丢失 / 旧值读 | 复制异步：写落 master，replica 有延迟；failover 会丢未同步数据 | **任何带主从的部署**（含 Cluster 每个分片） | RedLock / fencing token；强一致读写一律走 master |

各 Demo 对照（详细版写在每个类的 javadoc【部署形态提示】里）：

- **锁 / CAS / 限流 / 防超卖**：单 KEYS，Cluster 下零改动。但锁要防主从 failover 丢锁
  （RedLock 或 fencing token）；CAS 和扣减严禁读写分离——拿 replica 旧值判断再写回，
  比拆命令更隐蔽的超卖来源。
- **批量累加 / 延迟队列领取**：多 KEYS，Cluster 下默认命名必报 `CROSSSLOT`。
  用 hash tag 归槽（`{demo:cnt}:pv`、`{demo:delayq}:orders` + `{demo:delayq}:processing`）。
  注意 Cluster 禁止脚本触碰未声明的 key，所以带前缀的 key 名必须全部作为 KEYS 传入。
- **FUNCTION 库**：`FUNCTION LOAD` 不跨节点传播，Cluster 下要在**每个 master 各加载一次**，
  FCALL 路由到哪个节点就要求那个节点注册过。
- **Stream**：所有命令只碰一个 key，Cluster 下零改动；消费组元数据存在流内部，
  failover 后随数据复制到新主，消费者重连继续即可。
- **向量库**：`FT.*` 以索引名做路由，RediSearch 集群版自行维护分片内索引；
  索引定义随 RDB/AOF 复制，replica 提升后 FT.SEARCH 可直接用。

## 依赖说明

新增 `redis.clients:jedis`（轻量直连客户端，适合独立 main 演示；
工程原有的 spring-data-redis 面向 Spring 应用，两者定位不同）。
Demo 8 的向量检索走 Redis 服务端的 RediSearch + RedisJSON 模块。**Redis 8 起官方镜像
已内置并默认加载**（实测 `redis:8-alpine` 的 `MODULE LIST` 有 `search / ReJSON / vectorset /
timeseries / bf`），不需要额外的 Java 依赖；生产里 embedding 由 spring-ai-openai 等调用。

Redis 8 还新增了**原生 Vector Set 数据类型**（`VADD` / `VSIM` 等 13 个命令），适合纯相似度检索：

```redis
VADD vs:pets FP32 <二进制float32> dog-run
VSIM vs:pets VALUES 4 0.85 0.15 0.7 0.05 COUNT 2 WITHSCORES
```

比"建索引 + KNN"轻得多（不用声明 schema、算法、维度），但没有 TEXT/TAG/NUMERIC 那种
元数据混检能力。⚠️ 注意 score 语义相反：`FT.SEARCH KNN` 返回**距离**（越小越像），
`VSIM` 返回**相似度**（越大越像）。
