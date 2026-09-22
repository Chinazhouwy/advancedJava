package com.advancedjava.interview.transaction.seata;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;

/**
 * Seata TCC 最小示例：订单服务调用库存服务和账户服务。
 *
 * <pre>
 * TM：@GlobalTransactional，开启全局事务
 * RM：@TwoPhaseBusinessAction，声明 Try / Confirm / Cancel
 * TC：Seata Server，协调各个分支事务
 * </pre>
 */
@Service
public class SeataTccDemo {

    private final StockTcc stock;
    private final AccountTcc account;

    public SeataTccDemo(StockTcc stock, AccountTcc account) {
        this.stock = stock;
        this.account = account;
    }

    /** 订单服务：这里只负责发起全局事务和调用两个参与者。 */
    @GlobalTransactional(name = "place-order", rollbackFor = Exception.class)
    public void placeOrder(String orderId, int amount) {
        // Try 方法的第一个参数由 Seata 在执行过程中填充，调用方传 null。
        stock.tryReserve(null, orderId, 2);
        account.tryFreeze(null, orderId, amount);
    }
}

/** 库存服务的 TCC 接口。实际项目中可以放在独立的库存服务里。 */
@LocalTCC
interface StockTcc {

    @TwoPhaseBusinessAction(
            name = "stock-tcc",
            commitMethod = "confirm",
            rollbackMethod = "cancel",
            useTCCFence = true)
    boolean tryReserve(BusinessActionContext context,
                       @BusinessActionContextParameter(paramName = "orderId") String orderId,
                       @BusinessActionContextParameter(paramName = "quantity") int quantity);

    boolean confirm(BusinessActionContext context);

    boolean cancel(BusinessActionContext context);
}

@Service
class StockTccService implements StockTcc {

    @Override
    public boolean tryReserve(BusinessActionContext context, String orderId, int quantity) {
        // 预扣库存，并记录 orderId + xid，方便 Confirm / Cancel 找回这次预留。
        return true;
    }

    @Override
    public boolean confirm(BusinessActionContext context) {
        // 把预留库存正式扣除；必须幂等。
        return true;
    }

    @Override
    public boolean cancel(BusinessActionContext context) {
        // 释放预留库存；必须幂等，并处理空回滚。
        return true;
    }
}

/** 账户服务的 TCC 接口。 */
@LocalTCC
interface AccountTcc {

    @TwoPhaseBusinessAction(
            name = "account-tcc",
            commitMethod = "confirm",
            rollbackMethod = "cancel",
            useTCCFence = true)
    boolean tryFreeze(BusinessActionContext context,
                      @BusinessActionContextParameter(paramName = "orderId") String orderId,
                      @BusinessActionContextParameter(paramName = "amount") int amount);

    boolean confirm(BusinessActionContext context);

    boolean cancel(BusinessActionContext context);
}

@Service
class AccountTccService implements AccountTcc {

    @Override
    public boolean tryFreeze(BusinessActionContext context, String orderId, int amount) {
        // 冻结余额，并记录 orderId + xid。
        return true;
    }

    @Override
    public boolean confirm(BusinessActionContext context) {
        // 确认扣款；必须幂等。
        return true;
    }

    @Override
    public boolean cancel(BusinessActionContext context) {
        // 解冻余额；必须幂等，并处理空回滚。
        return true;
    }
}
