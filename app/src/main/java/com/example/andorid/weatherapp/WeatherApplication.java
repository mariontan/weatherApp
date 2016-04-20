package com.example.andorid.weatherapp;

import android.app.Application;
import android.util.Log;

/**
 * Created by S410P on 4/20/2016.
 */
public class WeatherApplication extends Application {
    public WeatherApplication(){

    }
    public void saveWeatherData(String weatherData){
        if(FileIO.isStorageReady()){
            Log.i("INFO","saving weather");
            FileIO.write("Weather",weatherData.getBytes());
            Log.i("INFO", "saveing complete");
        }
    }
}
