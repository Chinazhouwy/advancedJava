# Java 并发学习总图（高级）

本清单按“能力模块”组织，标注了当前仓库是否已有示例。

## 1. 线程与任务编排
- [x] `ThreadPoolExecutor` 参数调优、拒绝策略（`ThreadPoolTuningDemo`）
- [x] `CompletableFuture` 并行编排、超时兜底（`CompletableFutureOrchestrationDemo`）
- [ ] `ForkJoinPool` 任务拆分与工作窃取（可在 `concurrency/forkjoin` 再补）
- [ ] 虚拟线程（JDK 21）与平台线程对比

## 2. 同步器与协作
- [x] `Semaphore` 限流/舱壁（`SemaphoreBulkheadDemo`）
- [x] `CountDownLatch` 批处理收敛（`CountDownLatchBatchDemo`）
- [x] `CyclicBarrier` 与 `Phaser` 多阶段协作（`CyclicBarrierPhaserDemo`）
- [x] `LockSupport` 协议化协作（`LockSupportHandshakeDemo`）
- [x] AQS 自定义同步器（`AqsCustomMutexDemo`）

## 3. 并发容器与数据结构
- [x] `ConcurrentHashMap#compute` 原子更新（`ConcurrentHashMapComputeDemo`）
- [x] `ConcurrentSkipListMap` 跳表排序/TopN（`ConcurrentSkipListMapRankingDemo`）
- [x] `CopyOnWriteArrayList` 读多写少（`CopyOnWriteArrayListReadMostlyDemo`）
- [ ] `BlockingQueue` 系列边界与背压

## 4. 锁与读写优化
- [x] `StampedLock` 乐观读（`StampedLockOptimisticReadDemo`）
- [ ] `ReentrantReadWriteLock` 对比 `StampedLock`
- [ ] 锁公平性、饥饿、优先级反转分析

## 5. 原子类与性能
- [x] `AtomicLong` vs `LongAdder`（`LongAdderVsAtomicLongDemo`）
- [ ] ABA 问题与 `AtomicStampedReference`
- [ ] 伪共享与缓存行优化（`@Contended`）

## 6. 正确性与排障
- [ ] `jcstress` 并发正确性测试
- [ ] `jstack/jcmd/JFR` 线上问题定位
- [ ] 死锁、活锁、线程池内依赖死锁复现与修复

## 推荐学习顺序
1. 先跑 `ConcurrencyFeatureShowcaseApp`，对并发行为建立直觉。
2. 对照每个 demo 读注释，理解“为什么这么做”。
3. 再补“未覆盖”模块，尤其是 `jcstress` 与排障工具链。
