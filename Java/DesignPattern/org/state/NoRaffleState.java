package org.state;

public class NoRaffleState extends BaseState {

    RaffleActivity activity;

    public NoRaffleState(RaffleActivity raffleActivity) {
        this.activity = raffleActivity;
    }

    @Override
    public void deduceMoney() {
        System.out.println("积分扣除成功，可以抽奖了");
        activity.setCurrentState(activity.getCanRaffleState());
    }

    @Override
    public boolean raffle() {
        System.out.println("需要先扣除积分");
        return false;
    }

    @Override
    public void dispensePrize() {
        System.out.println("不能发送奖品");
    }
}
