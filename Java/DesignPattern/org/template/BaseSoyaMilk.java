package org.template;

/**
 * @author mozixun
 * @description
 * @date 2020/3/30 - 11:20 下午
 */
public abstract class BaseSoyaMilk {

    public final void make() {
        select();
        addCondiments();
        soak();
        beat();
    }

    private void  select() {
        System.out.println("Select fresh yellow bean.");
    }

    /**
     * 子类实现
     */
    abstract void addCondiments();

    private void soak() {
        System.out.println("Soak for 3 hours.");
    }

    private void beat() {
        System.out.println("Beat for soya milk.");
    }
}
