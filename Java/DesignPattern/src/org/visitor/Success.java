package org.visitor;

/**
 * @author mozixun
 * @description
 * @date 2020/4/2 - 10:22 下午
 */
public class Success extends BaseAction{

    @Override
    public void getManResult(Man man) {
        System.out.println("man success.");
    }

    @Override
    public void getWomanResult(Woman woman) {
        System.out.println("woman success.");
    }
}
