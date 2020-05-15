package org.template;

/**
 * @author mozixun
 * @description
 * @date 2020/3/30 - 11:19 下午
 */
public class Client {
    public static void main(String[] args) {

        BaseSoyaMilk peanutSoyaMilk = new PeanutSoyaMilk();
        peanutSoyaMilk.make();

        BaseSoyaMilk redBeanSoyaMilk = new RedBeanSoyaMilk();
        redBeanSoyaMilk.make();
    }
}
