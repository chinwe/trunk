package org.visitor;

/**
 * @author mozixun
 * @description
 * @date 2020/4/2 - 10:21 下午
 */
public class Man extends BasePerson{

    @Override
    public void accept(BaseAction baseAction) {
        baseAction.getManResult(this);
    }
}
