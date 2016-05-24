package com.tsarouchi.betaapp;

import android.app.Application;
import android.content.Context;
import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;

/**
 * Created by Emmanouil on 17-Dec-15.
 */
public class UtilsClass extends Application {

    private static final String TAG = "BetaAppLOG";
    private static final String logFileName = "betaApp.log";
    private static final String locFileName = "coordinates.txt";
    private static Context context;
    private static boolean logToFile = true;
    private static File logFile;    //logs location
    private static File currLocFile;    //current coordinates file
    private static File defLocFile;    //default coordinates file
    private static File sdCardDir;
    private static File mediaDir;

    public enum LogLVL {ERROR, INFO, DEBUG}

    /*
     * Initialize
     */
    public void onCreate() {
        super.onCreate();
        UtilsClass.context = getApplicationContext();
        UtilsClass.logToFile = isExternalStorageWritable();
        if (UtilsClass.logToFile) {
            UtilsClass.logToFile = createLogFile();
        } else {
            Log.i(TAG, "User-disabled log-to-file; using Android Log for logging");
        }

        createLocationFile(locFileName);
        if(currLocFile!=null && defLocFile==null){
            defLocFile = currLocFile;
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

    //Startof Other Logging Methods
    public static void logERROR(String msg) {
        log(msg, LogLVL.ERROR);
    }

    public static void logDEBUG(String msg) {
        log(msg, LogLVL.DEBUG);
    }

    public static void logINFO(String msg) {
        log(msg, LogLVL.INFO);
    }

    private static void log(String msg, LogLVL lvl) {
        Calendar c = Calendar.getInstance();
        String timestamp = c.get(Calendar.DATE) + "/" + (c.get(Calendar.MONTH) + 1) + "  " + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.MILLISECOND);
        switch (lvl) {
            case ERROR:
                Log.e(TAG, msg);
                msg = "ERROR: " + msg;
                break;
            case DEBUG:
                Log.d(TAG, msg);
                msg = "DEBUG: " + msg;
                break;
            case INFO:
                Log.i(TAG, msg);
                break;
            default:
                Log.w(TAG, msg);
                break;
        }
        if (getLogToFile()) {
            msg = "\n" + timestamp + " " + msg;
            try {
                FileWriter fileWriter = new FileWriter(logFile, true);
                BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
                bufferWriter.write(msg);
                bufferWriter.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "exception was thrown", e);
            }


        }

    }
    //Endof Other Methods


    //Startof Other Utility Methods

    public static void createVideoLocationFile(){
        createLocationFile(MainActivity.last_timestamp+".txt");
    }

    //Create File for coordinate logging
    private static void createLocationFile(String locationFileName) {
        if(locationFileName==null || (locationFileName.length() <1)){
            logERROR("calling create file with no filename");
        }

        if (!sdCardDir.exists()) {
            logERROR("Couldn't find folder for saving locations file");
            return;
        }

        UtilsClass.currLocFile = new File(sdCardDir + "/" + locationFileName);
        if (!UtilsClass.currLocFile.exists()) {
            try {
                UtilsClass.currLocFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "exception was thrown COULDN'T CREATE LOCATIONS FILE", e);
            }
        }
        if (!UtilsClass.currLocFile.exists()) {
            Log.e(TAG, "Locations File Not Created");
        } else {
            logINFO("Starting new Log - File " + locationFileName + " is used for location logging");
        }
    }

    //Append
    public static void writeDataToFile(String msg) {
        try {
            FileWriter fileWriter;
            if(MainActivity.recording) {
                fileWriter = new FileWriter(currLocFile, true);
            }else{
                fileWriter = new FileWriter(defLocFile, true);
            }
            BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
            bufferWriter.write(msg + "\n");
            bufferWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logERROR("couldn't write location " + e);
            Log.e(TAG, "exception was thrown", e);
        }
    }

    public static JSONObject locationToJSON(Location location) throws JSONException {
        String json = "{\n"
                + " \"Provider\" : \""+location.getProvider()+"\", "
                + " \"Latitude\" : "+location.getLatitude()+", "
                + " \"Longitude\" : "+location.getLongitude()+", "
                + " \"Time\" : "+location.getTime()+", "
                + " \"Accuracy\" : "+location.getAccuracy()+", "
                + " \"Velocity\" : "+location.getSpeed()+"\n "
                +"}";
        //TODO prettify try-catch and handle return
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            //e.printStackTrace();
            UtilsClass.logDEBUG("ERROR @ JSON lvl1 "+e.getMessage());
        }
        return new JSONObject("ERROR");
    }

    public static String SensorDataToString(SensorEvent event){
        String sensorDataString="{\n"
                + " \"Sensor\" : \""+event.sensor.getName()+"\", "
                + " \"Values\" : "+event.values+", "
                + " \"Time (Local)\" : "+event.timestamp+", "
                + " \"Accuracy\" : "+event.accuracy+"\n "
                +"}";
        return sensorDataString;
    }

    /*
     * Check if writing to ext is possible
     */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) && UtilsClass.logToFile;
    }

    /*
     * create file in ext for logging
     */
    //TODO checkthis
    private boolean createLogFile() {
        //  sdCardDir = new File(ContextCompat.getExternalCacheDirs(context)[0].getAbsolutePath()+"/BetAppOut");
        //  sdCardDir = getFilesDir();
        sdCardDir = new File(ContextCompat.getExternalFilesDirs(context, null)[0].getAbsolutePath());
        if (!sdCardDir.exists()) {
            logToFile = sdCardDir.mkdirs();
        }
        UtilsClass.logFile = new File(sdCardDir + "/" + logFileName);
        if (!UtilsClass.logFile.exists()) {
            try {
                UtilsClass.logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "exception was thrown", e);
            }
        }
        if (!UtilsClass.logFile.exists()) {
            Log.e(TAG, "File Not Created - using Android Log for logging");
            return false;
        } else {
            logINFO("Starting new Log - File " + logFileName + " is used for logging");
        }
        return true;

    }

}