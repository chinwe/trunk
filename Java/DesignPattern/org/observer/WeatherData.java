package org.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mozixun
 * @description
 * @date 2020/4/12 - 3:48 下午
 */
public class WeatherData implements ISubject {
    /**
     * 温度
     */
    private float temperature;
    /**
     * 气压
     */
    private float pressure;
    /**
     * 湿度
     */
    private float humidity;

    /**
     * 观察者列表
     */
    private List<IObserver> observers = new ArrayList<>();

    @Override
    public void registerObserver(IObserver observer) {
        observers.add(observer);
    }

    @Override
    public void remove(IObserver observer) {
        if (observers.contains(observer)) {
            observers.remove(observer);
        }
    }

    @Override
    public void notifyObservers() {
        for (IObserver observer : observers) {
            observer.update(this.temperature, this.pressure, this.humidity);
        }
    }

    void setData(float temperature, float pressure, float humidity)
    {
        this.temperature = temperature;
        this.pressure = pressure;
        this.humidity = humidity;

        notifyObservers();
    }

}
