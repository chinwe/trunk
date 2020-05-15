package org.state;

import java.util.Random;

public class CanRaffleState extends BaseState {

    RaffleActivity activity;

    public CanRaffleState(RaffleActivity activity) {
        this.activity = activity;
    }

    @Override
    public void deduceMoney() {
        System.out.println("积分已扣除");
    }

    @Override
    public boolean raffle() {
        System.out.println("正在抽奖，请稍等");
        Random random = new Random();
        int num = random.nextInt(10);
        if (2 == num) {
            activity.setCurrentState(activity.getDispensePrizeState());
            return true;
        } else {
            activity.setCurrentState(activity.getNoRaffleState());
            System.out.println("很遗憾，没有中奖");
            return false;
        }
    }

    @Override
    public void dispensePrize() {
        System.out.println("不能发放奖品");
    }
}
