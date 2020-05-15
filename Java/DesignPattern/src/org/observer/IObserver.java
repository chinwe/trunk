package org.observer;

/**
 * @author mozixun
 * @description
 * @date 2020/4/12 - 4:04 下午
 */
public interface IObserver {
    void update(float temperature, float pressure, float humidity);
}
