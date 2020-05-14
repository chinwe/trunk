package org.state;

public class DispenseOutState extends BaseState {

    RaffleActivity activity;

    public DispenseOutState(RaffleActivity activity) {
        this.activity = activity;
    }

    @Override
    public void deduceMoney() {
        System.out.println("活动结束，请下次再来");
    }

    @Override
    public boolean raffle() {
        System.out.println("活动结束，请下次再来");
        return false;
    }

    @Override
    public void dispensePrize() {
        System.out.println("活动结束，请下次再来");
    }
}
