# Seata TCC 最小示例

代码位置：

```text
src/main/java/com/advancedjava/interview/transaction/seata/SeataTccDemo.java
```

这个示例只保留 Seata TCC 最重要的三个 API：

```text
@GlobalTransactional       开启全局事务
@LocalTCC                   声明本地 TCC 参与者
@TwoPhaseBusinessAction     绑定 Try、Confirm、Cancel
```

## 先看调用关系

```text
订单服务（TM）
    @GlobalTransactional
          |
          +--> 库存服务（RM）: tryReserve -> confirm / cancel
          |
          +--> 账户服务（RM）: tryFreeze  -> confirm / cancel
                         |
                   Seata Server（TC）
```

`SeataTccDemo.placeOrder` 是全局事务入口。库存和账户的 Try 都成功时，TC 通知两个参与者 Confirm；任一 Try 失败时，TC 通知已经执行过 Try 的参与者 Cancel。

## 代码怎么看

```java
@GlobalTransactional(name = "place-order", rollbackFor = Exception.class)
public void placeOrder(String orderId, int amount) {
    stock.tryReserve(null, orderId, 2);
    account.tryFreeze(null, orderId, amount);
}
```

这就是 TM 的代码。Try 方法第一个参数是 `BusinessActionContext`，调用方传 `null`，Seata 会在事务过程中填充它。TM 不自己写回滚逻辑，只负责开启全局事务和调用参与者。

```java
@TwoPhaseBusinessAction(
        name = "stock-tcc",
        commitMethod = "confirm",
        rollbackMethod = "cancel",
        useTCCFence = true)
boolean tryReserve(BusinessActionContext context, String orderId, int quantity);
```

这段注解告诉 Seata：`tryReserve` 是 Try，成功后调用 `confirm`，需要回滚时调用 `cancel`。`BusinessActionContext` 可以拿到本次全局事务的 `xid`；业务代码通常用 `xid + orderId` 找到自己的预留记录。

`@BusinessActionContextParameter` 会把业务参数带到 Confirm / Cancel 的上下文中。`useTCCFence = true` 表示启用 Seata TCC Fence，帮助处理空回滚、悬挂和重复调用场景。

## 为什么这次比原来的代码短

原来的 Java 示例自己模拟了状态、余额、库存和 Confirm 重试，适合讲协议，但不是 Seata 代码。现在把协调工作交给 Seata，只留下业务服务必须实现的 Try、Confirm、Cancel。

参与者仍然必须自己保证：

1. Try 预留资源，并记录 `xid + 业务主键`。
2. Confirm 幂等，重复调用不能重复扣款。
3. Cancel 幂等，重复调用不能重复释放。
4. Try、Confirm、Cancel 使用本地事务写自己的数据库。

## 运行边界

先编译，确认 Seata API 可用：

```bash
mvn -q -DskipTests compile
```

这份代码是学习用的 Seata API 骨架，不是开箱即跑的三服务工程。要真正运行，需要：

```text
1. 启动 Seata Server（TC）。
2. 在 Spring Boot 配置 Seata 服务地址和事务组。
3. 给订单、库存、账户配置各自的数据源和本地事务。
4. 需要体现服务边界时，把库存、账户拆成独立进程，通过 Feign / Dubbo 等 RPC 调用。
```

当前示例把三个参与者放在一个 Maven 工程里，是为了让核心注解一眼可见；加上 `@GlobalTransactional` 并不会自动把单 JVM 代码变成分布式系统。

官方 API 说明可参考 [Seata TCC 模式](https://seata.apache.org/docs/v2.0/user/mode/tcc/) 和 [Seata API](https://seata.apache.org/docs/v2.0/user/api/)。
