package org.observer;

/**
 * @author mozixun
 * @description
 * @date 2020/4/12 - 4:18 下午
 */
public class WeatherWebSite implements IObserver {
    @Override
    public void update(float temperature, float pressure, float humidity) {
        System.out.println("-----------------WeatherWebSite-----------------------");
        System.out.println("Current temperature: " + temperature);
        System.out.println("Current pressure: " + pressure);
        System.out.println("Current humidity: " + humidity);
    }
}

