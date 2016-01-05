package com.tsarouchi.betaapp;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

/**
 * Created by Emmanouil on 17-Dec-15.
 */
public class UtilsClass extends Application {

    private static Context context;
    private static boolean logToFile = true;

    public void onCreate() {
        super.onCreate();
        UtilsClass.context = getApplicationContext();
        UtilsClass.logToFile = isExternalStorageWritable();
    }

    public static Context getAppContext() {
        return UtilsClass.context;
    }
    public static boolean getLogToFile() {
        return UtilsClass.logToFile;
    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)&&UtilsClass.logToFile) {
            return true;
        }
        return false;
    }

}