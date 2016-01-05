package com.tsarouchi.betaapp;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by Emmanouil on 17-Dec-15.
 */
public class UtilsClass extends Application {

    private static Context context;
    private static boolean logToFile = true;
    private static File logFile;

    /*
     * Initialize
     */
    public void onCreate() {
        super.onCreate();
        UtilsClass.context = getApplicationContext();
        UtilsClass.logToFile = isExternalStorageWritable();
        if(UtilsClass.logToFile){
            UtilsClass.logToFile = createLogFile();
        }
    }

// Startof Setters
    public static Context getAppContext() {
        return UtilsClass.context;
    }
    public static boolean getLogToFile() {
        return UtilsClass.logToFile;
    }
//Endof Setters

    /*
     * Check if writing to ext is possible
     */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)&&UtilsClass.logToFile) {
            return true;
        }
        return false;
    }

    /*
     * create file in ext for logging
     */
    private boolean createLogFile(){
        UtilsClass.logFile = new File("sdcard/betaApp.log");
        if (!UtilsClass.logFile.exists())
        {
            try
            {
                UtilsClass.logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if(!UtilsClass.logFile.exists()){
            return false;
        }
        return true;

    }

}