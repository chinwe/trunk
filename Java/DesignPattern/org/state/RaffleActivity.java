package org.state;

public class RaffleActivity {

    BaseState currentState;
    int count = 0;

    BaseState noRaffleState = new NoRaffleState(this);
    BaseState canRaffleState = new CanRaffleState(this);
    BaseState dispensePrizeState = new DispensePrizeState(this);
    BaseState dispenseOutState = new DispenseOutState(this);

    public RaffleActivity(int count) {
        this.currentState = getNoRaffleState();
        this.count = count;
    }

    // 扣除积分
    public void deduceMoney()
    {
        currentState.deduceMoney();
    }

    // 抽奖
    public void raffle()
    {
        if (currentState.raffle()) {
            currentState.dispensePrize();
        }
    }

    public BaseState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(BaseState currentState) {
        this.currentState = currentState;
    }

    public int getCount() {
        return count--;
    }

    public BaseState getNoRaffleState() {
        return noRaffleState;
    }

    public BaseState getCanRaffleState() {
        return canRaffleState;
    }

    public BaseState getDispensePrizeState() {
        return dispensePrizeState;
    }

    public BaseState getDispenseOutState() {
        return dispenseOutState;
    }
}
