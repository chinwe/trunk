package org.visitor;

/**
 * @author mozixun
 * @description
 * @date 2020/4/2 - 10:22 下午
 */
public class Fail extends BaseAction {

    @Override
    public void getManResult(Man man) {
        System.out.println("man fail.");
    }

    @Override
    public void getWomanResult(Woman woman) {
        System.out.println("woman fail.");
    }
}
