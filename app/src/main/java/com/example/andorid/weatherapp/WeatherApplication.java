package com.example.andorid.weatherapp;

import android.app.Application;
import android.util.Log;

import java.io.BufferedInputStream;

/**
 * Created by S410P on 4/20/2016.
 */
public class WeatherApplication extends Application {
    private String cityName = "Manila";

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public WeatherApplication(){

    }
    public void saveWeatherData(String weatherData, String filename){
        if(FileIO.isStorageReady()){
            Log.i("INFO","saving weather");
            FileIO.write(filename,weatherData.getBytes());
            Log.i("INFO", "saving complete");
        }
    }
    public String getWeatherData(String filename){
        try{
            if(FileIO.isStorageReady()){
                Log.i("INFO","Reading file...");
                BufferedInputStream is = new BufferedInputStream(FileIO.getFileInputStream(filename));
                String dataStr = "";
                int cInp = 0;
                while(is.available()>0){
                    cInp = is.read();
                    dataStr += (char)(cInp);
                }
                is.close();
                Log.i("INFO", "Reading done: " + dataStr);
                return dataStr;
            }
        }catch(Exception e){
            Log.e("ERROR","Exception occurred"+e.getMessage());
        }
        return"";
    }
}
