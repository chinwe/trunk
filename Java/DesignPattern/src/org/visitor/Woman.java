package org.visitor;

/**
 * @author mozixun
 * @description
 * @date 2020/4/2 - 10:21 下午
 */
public class Woman extends BasePerson {
    @Override
    public void accept(BaseAction baseAction) {
        baseAction.getWomanResult(this);
    }
}
