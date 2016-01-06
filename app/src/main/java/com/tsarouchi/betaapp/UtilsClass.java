package com.tsarouchi.betaapp;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

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
    public enum LogLVL{ERROR, INFO, DEBUG}

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

//Startof Other Methods
    public static void logERROR(String msg){
        log(msg, LogLVL.ERROR);
    }
    public static void logDEBUG(String msg){
        log(msg, LogLVL.DEBUG);
    }
    public static void logINFO(String msg){
        log(msg, LogLVL.INFO);
    }
    private static void log(String msg, LogLVL lvl){
        Calendar c = Calendar.getInstance();
        String timestamp = c.get(Calendar.DATE)+"/"+(c.get(Calendar.MONTH)+1)+"  "+c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.MILLISECOND);
        switch (lvl){
            case ERROR:
                Log.e(TAG, msg);
                msg = "ERROR: "+msg;
                break;
            case DEBUG:
                Log.d(TAG, msg);
                msg = "DEBUG: "+msg;
                break;
            case INFO:
                Log.i(TAG, msg);
                break;
            default:
                Log.w(TAG, msg);
                break;
        }
        if(getLogToFile()){
            msg = "\n"+timestamp+" "+msg;
            try
            {
                FileWriter fileWriter = new FileWriter(logFile,true);
                BufferedWriter bufferWritter = new BufferedWriter(fileWriter);
                bufferWritter.write(msg);
                bufferWritter.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                Log.e(TAG, "exception was thrown", e);
            }


        }


    }
//Endof Other Methods


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
            logINFO("Starting new Log - File "+fileList()[0]+" is used for logging");
        }
        return true;

    }

}