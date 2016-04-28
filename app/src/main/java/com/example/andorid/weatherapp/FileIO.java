package com.example.andorid.weatherapp;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by S410P on 4/20/2016.
 */
public class FileIO {
    public static boolean isStorageReady(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
    public static String getStorage(){
        return Environment.getExternalStorageDirectory().toString()+"/weatherApp/weatherData/";
    }
    public static void write(String filename, byte[] data){
        File fOutputDir = new File(getStorage());
        File fOutput =new File(getStorage(),filename+".txt");

        FileOutputStream fileOut = null;

        try{
            //make directory and file if it does not exist yet
            if(!fOutputDir.exists()){
                fOutputDir.mkdirs();
            }
            if(!fOutputDir.exists()){
                fOutputDir.createNewFile();
            }
            fileOut = new FileOutputStream(fOutput, false);//false is for appending
            fileOut.write(data);
            fileOut.close();
        }catch(FileNotFoundException e){
            Log.e("ERROR", "File not found:" + fOutput.toString());
        }catch (Exception e){
            Log.e("ERROR","Exception occurred"+ e.getMessage());
        }
        return;
    }
    public static FileInputStream getFileInputStream(String filename){
        File fInput = new File(getStorage(),filename+".txt");
        /*Check if file exists*/
        if(!fInput.exists()){
            return null;
        }
        Log.i("INFO", "Accessing file:"+ fInput.toString());
        Log.i("INFO","         Exists"+fInput.exists());
        FileInputStream fileIn = null;
        try{
            fileIn = new FileInputStream(fInput);
        }catch (Exception e){
            Log.e("ERROR","Exception occurred"+e.getMessage());
        }
        return fileIn;
    }
}
