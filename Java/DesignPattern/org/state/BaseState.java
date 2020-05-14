package org.state;

public abstract class BaseState {

    // 扣除积分
    public abstract void deduceMoney();

    // 是否抽奖
    public abstract boolean raffle();

    // 发放奖品
    public abstract void dispensePrize();
}
