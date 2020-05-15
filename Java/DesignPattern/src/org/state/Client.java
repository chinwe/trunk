package org.state;

public class Client {

    public static void main(String[] args) {

        RaffleActivity activity = new RaffleActivity(1);

        for (int i = 0; i < 20; i++) {
            System.out.println("------第" + (i + 1) + "次抽奖------");

            activity.deduceMoney();

            activity.raffle();
        }
    }
}
