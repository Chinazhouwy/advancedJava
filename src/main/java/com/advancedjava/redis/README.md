# Redis 高级用法演示包（Lua 脚本 + Java）

本包是 6 个**独立可运行的 main**，每个演示一个 Redis + Lua 的高频进阶场景。
核心主线只有一条：

> **Redis 单线程执行，单条命令天然原子；"读 → 判断 → 写" 这类多步逻辑一旦拆开发送，
> 并发下必然出竞态。Lua 脚本把整段逻辑交给服务端一次执行，中途不插入其他客户端的命令，
> 竞态一次性消失。**

---

## 文件清单与运行方式

```text
DistributedLockLuaDemo.java      # 1) 分布式锁：SET NX EX + Lua 原子解锁
SlidingWindowRateLimitDemo.java  # 2) ZSET + Lua 滑动窗口限流
BatchIncrLuaDemo.java            # 3) 一次 EVAL 原子批量累加 N 个计数器
CasUpdateLuaDemo.java            # 4) Lua CAS 比较并交换
DelayQueueLuaDemo.java           # 5) ZSET + Lua 延迟队列原子领取
StockOversellLuaDemo.java        # 6) 防超卖 EVAL + Redis 7 FUNCTION 按名调用
StreamConsumeDemo.java           # 7) Stream 消费组：XADD/XREADGROUP/XACK/XPENDING
VectorSearchDemo.java            # 8) 向量库：RediSearch KNN + RedisJSON + 混合检索
```

```bash
docker run -d --name redis-demo -p 6379:6379 redis:8-alpine   # 本地 Redis（推荐 8）
mvn compile
mvn exec:java -Dexec.mainClass=com.advancedjava.redis.DistributedLockLuaDemo
# ...其余同理；IDE 里也可直接跑各文件的 main。所有 key 带 demo: 前缀，可随时清掉
```

> Demo 8 依赖 RediSearch + RedisJSON 模块。**Redis 8 官方镜像已内置并默认加载**，所以
> `redis:8-alpine` 直接能跑；若还在用 Redis 7.x，换成 redis-stack 镜像即可：
> `docker rm -f redis-demo && docker run -d --name redis-demo -p 6379:6379 redis/redis-stack-server:latest`

---

## 八个 Demo 详解

### 1. DistributedLockLuaDemo —— 为什么解锁必须用 Lua

- **问题**：解锁若拆成 `GET 判断持有者` + `DEL` 两条命令，中间锁可能刚好过期被 B 抢到，
  A 再去 DEL 就误删了 B 的锁。
- **做法**：加锁用 `SET key token NX EX 30`——一条命令完成"占位+设过期"
  （绝不能 SETNX + EXPIRE 两步，中间宕机会留下永不过期的死锁）；
  解锁用脚本让"读+判+删"原子执行：

  ```lua
  if redis.call('get', KEYS[1]) == ARGV[1] then
      return redis.call('del', KEYS[1])
  else
      return 0
  end
  ```

- **实测**：错误 token 解锁返回 0（拒绝），正确 token 返回 1（删除）。
- **追问三连**：业务没跑完锁先过期？→ 看门狗续期（生产直接用 Redisson RLock）。
  要可重入？→ Hash 结构，field=`客户端:线程id`，value=重入次数。

### 2. SlidingWindowRateLimitDemo —— ZSET 滑动窗口

- **问题**：固定窗口计数器在切换瞬间放行 2 倍流量（第 59 秒打满 + 新窗口第 1 秒再打满）。
- **做法**：ZSET 里每个请求一个点（score=时间戳毫秒），脚本内"清理窗口外旧点 → ZCARD 计数
  → 判断 → ZADD 记录 + PEXPIRE 兜底"四步原子完成；返回剩余配额，-1 表示拒绝。
- **算法家族对比**：固定窗口（简单但临界突刺）→ 滑动窗口 ZSET（精确，每请求占一个 member）
  → 令牌桶/漏桶（允许突发 / 需要匀速时选）。
- **实测**：1 秒窗口限 5 个，连发 8 个 = 5 放行 + 3 拒绝；1.1 秒后恢复放行。

### 3. BatchIncrLuaDemo —— 一次 EVAL 批量累加

- **问题**：N 个计数器逐条 INCRBY = N 次网络往返。
- **三种批量写法对比**（面试标准题）：
  1. 循环单条：N 次 RTT；
  2. Pipeline：合并网络包，仍是 N 条独立命令、**非原子**；
  3. Lua：1 次 RTT，且整批在单线程内原子生效（同一时刻快照）。
- **代价**：脚本执行期间阻塞其他命令，所以脚本要短、循环量可控。
- **实测**：`[10,3,7]` 一轮 `[1,1,1]` 一轮，2 次往返完成 6 次累加。

### 4. CasUpdateLuaDemo —— 比较并交换

- **问题**：Redis 没有原生 CAS。传统解法是 WATCH/MULTI/EXEC 乐观事务：命令多、要写重试循环。
- **做法**：`if redis.call('get',KEYS[1]) == ARGV[1] then SET return 1 else return 0`——
  读+比较+写一个脚本原子完成，无竞态。
- **场景**：带版本号的配置发布，只允许 v1→v2 链式推进；拿旧版本号提交一律被拒，
  天然防止并发回退覆盖。

### 5. DelayQueueLuaDemo —— ZSET 延迟队列

- **场景**：下单 30 分钟未支付自动关单、重试退避、定时对账。
- **结构**：任务以 score=到期时间戳放进 ZSET；消费时"查到期 → 取出 → 删除"必须原子，
  否则两个消费者领到同一任务重复执行。领取脚本把任务原子迁移进"处理中"队列
  （score=领取时间），消费者宕机可按领取超时重投——这就是崩溃恢复的标准做法。
- **实测**：投 200/400/600ms 三个任务，立即领取为空，300ms 后只有 A，再 200ms 后只有 B。

### 6. StockOversellLuaDemo —— 防超卖 + FUNCTION 库

- **问题**："查余量 → 判断 → DECRBY" 拆开发送，余量 1 时两个线程同时读到 1、各自通过
  判断、各扣一次 → 必超卖。
- **EVAL 版**：三步收进一个脚本，10 线程抢 5 件实测恰好 5 成功 + 5 拒绝、余量归零。
- **FUNCTION 版**（Redis 7）：`FUNCTION LOAD` 把函数注册进服务端命名空间，之后
  `FCALL stock_decr 1 <key> <数量>` 按名调用——省流量、可版本化、多客户端共享一份逻辑。
- **易错点**：带 `#!lua name=` shebang 的库源码只对 FUNCTION 子系统可见，不能走 EVAL/EVALSHA；
  Redis 7.0 注册 API 是 `redis.register_function(name, fn)`（`Lua.registerFunction` 不存在，
  带 flags 的新签名要 7.4+）。

### 7. StreamConsumeDemo —— 像 Kafka 一样的消息队列

- **三者对比**（面试标准题）：
  - Pub/Sub：发完即走，订阅者不在线就永久丢消息，无积压能力；
  - List + BRPOP：消费即删除，处理失败消息就没了，也无法多消费者分组；
  - **Stream**：消息持久堆积、自动递增 ID、消费组分发、ACK 确认、失败重投。
- **核心命令链**：`XADD` 生产 → `XGROUP CREATE` 建组 → `XREADGROUP` 消费 →
  `XACK` 确认 → `XPENDING` 看待确认 → `XAUTOCLAIM` 接管超时消息。
- **实测**：3 条消息被 worker-A（2 条）和 worker-B（1 条）分走——同一条只投给组内
  一个消费者，不是广播；XPENDING 显示 `{worker-A=2, worker-B=1}`；ACK 一条后总数 3→2。
- **易错点**：消费新消息的 ID 必须是 `">"`，即 `StreamEntryID.XREADGROUP_UNDELIVERED_ENTRY`；
  传 `new StreamEntryID()` 会序列化成空串，服务端当成"已投递位置"，结果一条都读不到。

### 8. VectorSearchDemo —— Redis 集成的向量库（RediSearch + RedisJSON）

- **基本用法三步**：`JSON.SET` 写入带向量的文档 → `FT.CREATE` 建 HNSW 索引 →
  `FT.SEARCH` 用 KNN 语句检索；VECTOR 字段支持 HNSW / FLAT 与 COSINE / L2 / IP 距离。
- **KNN 一句话**：文本经 embedding 变成向量存进字段，查询时把问题也变成向量，
  按距离返回 topK——匹配"意思"而非关键词。
- **实测**（4 维玩具向量，依次代表 动物性/食物性/运动感/科技感）：
  - 查「狗在奔跑」→ 命中 `金毛犬在公园奔跑` 与 `宠物猫和狗粮`，美食类被自然排除；
  - 混合检索 `@category:{tech}=>[KNN 2 ...]` → 先按标签过滤再算近邻，只命中 tech 文档。
- **两个真坑**：
  1. KNN 的 `=>` 语法属于 **DIALECT 2**，Jedis 里必须显式 `.dialect(2)`，
     否则报 `Syntax error at offset 1 near >[`；
  2. 查询向量要按 **FLOAT32 小端字节序**传，而 JSON 里存的是普通数字数组。
- **Redis 8 的另一种选择**：原生 **Vector Set** 数据类型（`VADD` / `VSIM`）不用建索引，
  适合纯相似度检索；但没有 TEXT/TAG 混检能力，带元数据过滤的 RAG 仍走本 Demo 这条路。
  ⚠️ score 语义相反：KNN 给**距离**（越小越像），VSIM 给**相似度**（越大越像）。

---

## EVAL 参数约定（Demo 1–6 通用）

```java
jedis.eval(脚本, List.of(key...), List.of(arg...));
//    KEYS[1..n] ↑                ↑ ARGV[1..n]
```

- **KEYS**：参与路由的 key 名必须走这里声明（Cluster 靠它算 slot）；
- **ARGV**：纯业务数据，**不要塞 key 名**；
- 脚本不用包函数，就是裸 Lua 程序；`return` 的数字/字符串/table 原样转成 RESP 类型返回。

---

## 单机 / 主从 / Cluster 部署对照

**先纠正误区：Cluster ≠ 没有主从。** Cluster 的每个分片本身就是 1 主 + N 副本，
两类问题正交：

| 问题域 | 根源 | 发生在哪 | 解法 |
|---|---|---|---|
| CROSSSLOT | 一条命令的多个 key 落在不同 slot | 只在 Cluster | hash tag `{...}` 归槽 / 按槽分组 |
| 锁丢失、旧值读 | 复制异步 + failover 丢未同步数据 | **一切带主从的部署**（含 Cluster 分片） | RedLock、fencing token、强一致读写走 master |

| Demo | Cluster | 主从 |
|---|---|---|
| 1 锁 | 单 key 零改动 | failover 丢锁 → RedLock / fencing token |
| 2 限流 | 单 key 零改动 | 判定永远在 master，replica 只做观测 |
| 3 批量累加 | 多 key 默认命名必报 CROSSSLOT → `{demo:cnt}:pv` 归槽 | — |
| 4 CAS | 单 key 零改动 | 严禁读写分离（拿 replica 旧值回退覆盖最危险） |
| 5 延迟队列 | 2 个 KEYS 需同槽 `{demo:delayq}:orders/processing` | 领取写操作必须 master |
| 6 防超卖 | 单 key 零改动；**FUNCTION 要在每个 master 各 LOAD 一次** | 扣减走 master，详情页可读 replica |
| 7 Stream | 单 key 零改动；消费组元数据随流复制到新主 | 消费者重连新主继续 XREADGROUP |
| 8 向量库 | `FT.*` 以索引名路由，RediSearch 集群版自管分片索引 | 索引定义随 RDB/AOF 复制，提升后可直接搜 |

注意：Cluster 禁止脚本触碰未声明的 key，多 key 场景所有 key 名必须完整走 KEYS 传入。

---

## 踩坑记录（真实调试付出）

1. **Jedis 不是线程安全的**：抢购 Demo 里 10 个线程共享一个连接，RESP 响应立刻错乱
   （`Unknown reply` / broken connection）。每线程自己 `new Jedis(...)`。
2. **FUNCTION API 版本差异**：`Lua.registerFunction` 直接报全局变量不存在；
   7.0 用 `redis.register_function(name, fn)`。
3. **Jedis 5.x 签名**：`del` 收 `String...` 不收 `List`；`fcall` 是
   `fcall(fnName, List keys, List args)` 或 `fcall(fnName, int keyCount, String... args)`。
4. **Stream 消费 ID**：新消息必须传 `">"`（`XREADGROUP_UNDELIVERED_ENTRY`），
   空 ID 会导致一条都读不到且不报错——最难查的那种坑。
5. **KNN 需要 DIALECT 2**：Jedis 的 `Query.dialect(2)` 不能省，否则报
   `Syntax error at offset 1 near >[`，错误信息完全不提 dialect。
6. **JSON 索引字段类型**：给字符串字段声明 NUMERIC 会让文档索引失败进
   `hash_indexing_failures`，`num_docs` 为 0 但 FT.CREATE 不报错——用 `FT.INFO` 看
   `last indexing error` 才定位得到。
7. **`jsonSet` vs `jsonSetWithEscape`**：新版 stack 对 `JSON.SET` 的 path 校验更严，
   `jsonSet(key, Path2.ROOT_PATH, obj)` 报 `key must be a string`，换用
   `jsonSetWithEscape` 正常。

延伸阅读：仓库根目录 [docs/redis-lua.md](../../../../../../docs/redis-lua.md)（含全部实测输出）、
[README.md](../../../../../../README.md)（项目总览与本包的概要条目）。
