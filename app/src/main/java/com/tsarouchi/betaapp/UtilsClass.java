package com.tsarouchi.betaapp;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by Emmanouil on 17-Dec-15.
 */
public class UtilsClass extends Application {

    private static final String TAG = "BetaAppLOG";
    private static Context context;
    private static boolean logToFile = true;
    private static File logFile;
    private static File sdCardDir;
    private static File mediaDir;

    /*
     * Initialize
     */
    public void onCreate() {
        super.onCreate();
        UtilsClass.context = getApplicationContext();
        UtilsClass.logToFile = isExternalStorageWritable();
        if(UtilsClass.logToFile){
            UtilsClass.logToFile = createLogFile();
        }else{
            Log.i(TAG, "User-disabled log-to-file; using Android Log for logging");
        }
    }

// Startof Getters
    public static Context getAppContext() {
        return UtilsClass.context;
    }
    public static boolean getLogToFile() {
        return UtilsClass.logToFile;
    }

    public static String getLogFile() {
        return logFile.getPath();
    }

//Endof Getters

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
    //TODO checkthis
    private boolean createLogFile(){
      //  sdCardDir = new File(ContextCompat.getExternalCacheDirs(context)[0].getAbsolutePath()+"/BetAppOut");
      //  sdCardDir = getFilesDir();
        sdCardDir = new File(ContextCompat.getExternalFilesDirs(context, null)[0].getAbsolutePath());
        if(!sdCardDir.exists()){
            logToFile = sdCardDir.mkdirs();
        }
        UtilsClass.logFile = new File(sdCardDir+"/betaApp.log");
        if (!UtilsClass.logFile.exists())
        {
            try
            {
                UtilsClass.logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                Log.e(TAG, "exception was thrown", e);
            }
        }
        if(!UtilsClass.logFile.exists()){
            Log.e(TAG, "File Not Created - using Android Log for logging");
            return false;
        }else{
            Log.i(TAG, "File "+fileList()[0]+" is used for logging");
        }
        return true;

    }

}