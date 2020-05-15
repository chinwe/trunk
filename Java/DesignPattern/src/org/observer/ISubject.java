package org.observer;

/**
 * @author mozixun
 * @description
 * @date 2020/4/12 - 4:04 下午
 */
public interface ISubject {

    void registerObserver(IObserver observer);

    void remove(IObserver observer);

    void notifyObservers();
}
