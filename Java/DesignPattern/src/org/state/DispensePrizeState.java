package org.state;

public class DispensePrizeState extends BaseState {

    RaffleActivity activity;

    public DispensePrizeState(RaffleActivity activity) {
        this.activity = activity;
    }

    @Override
    public void deduceMoney() {
        System.out.println("不能扣除积分");
    }

    @Override
    public boolean raffle() {
        System.out.println("不能抽奖");
        return  false;
    }

    @Override
    public void dispensePrize() {
        if (activity.getCount() > 0) {
            activity.setCurrentState(activity.getNoRaffleState());
            System.out.println("恭喜你中奖了，发放奖品");
        } else {
            activity.setCurrentState(activity.getDispenseOutState());
            System.out.println("奖品发放完毕");
            // System.exit(0);
        }
    }
}
