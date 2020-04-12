package org.observer;

/**
 * @author mozixun
 * @description
 * @date 2020/4/3 - 9:04 下午
 */
public class Client {
    public static void main(String[] args) {
        WeatherData weatherData = new WeatherData();

        WeatherStation weatherStation = new WeatherStation();
        weatherData.registerObserver(weatherStation);

        WeatherWebSite weatherWebSite = new WeatherWebSite();
        weatherData.registerObserver(weatherWebSite);

        weatherData.setData(25, 150, 40);

        weatherData.setData(30, 120, 30);

    }
}
