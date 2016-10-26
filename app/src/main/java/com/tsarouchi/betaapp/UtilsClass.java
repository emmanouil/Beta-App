package com.tsarouchi.betaapp;

import android.app.Application;
import android.content.Context;
import android.hardware.SensorEvent;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Emmanouil on 17-Dec-15.
 * <p>
 * Holds helper functions
 */
public class UtilsClass extends Application {

    //Startof Options
    private static final String TAG = "BetaAppLOG";
    private static final String logFileName = "betaApp.log";
    private static final String locFileName = "coordinates.txt";
    private static boolean logToFile = true;    //try to log to file

//Endof Options

    private static File logFile;    //logs location
    private static File currLocFile;    //current coordinates file
    private static File defLocFile;    //default coordinates file
    private static File sdCardDir;

    public enum LogLVL {ERROR, INFO, DEBUG};

    private static List<String> mediaScannerList = new ArrayList<String>();

    /*
     * Initialize
     */
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        UtilsClass.logToFile = isExternalStorageWritable();
//      sdCardDir = new File(ContextCompat.getExternalFilesDirs(context, null)[0].getAbsolutePath());
        sdCardDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "MyCameraApp");

        if (UtilsClass.logToFile) {
            UtilsClass.logToFile = createLogFile();
        } else {
            Log.i(TAG, "User-disabled log-to-file; using Android Log for logging");
        }

        createLocationFile(locFileName);
        if (currLocFile != null && defLocFile == null) {
            defLocFile = currLocFile;
        }

    }

    public static void stopLogging(){

    }


    // Startof Getters
/*
    public static Context getAppContext() {
        return UtilsClass.context;
    }
*/
    public static boolean getLogToFile() {
        return UtilsClass.logToFile;
    }

    public static String getLogFile() {
        return logFile.getPath();
    }

    public static void pushFileToList(String filepath){
        UtilsClass.mediaScannerList.add(filepath);
    }

    public static void refreshFileList(Context app_c){
            MediaScannerConnection.scanFile(app_c, UtilsClass.mediaScannerList.toArray(new String[mediaScannerList.size()]), null, null);
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
            msg = "\n" + timestamp + " " + lvl.toString() + "  " + msg;
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

    public static void createVideoLocationFile() {
        createLocationFile(MainActivity.last_timestamp + ".txt");
    }

    //Create File for coordinate logging
    private static void createLocationFile(String locationFileName) {
        if (locationFileName == null || (locationFileName.length() < 1)) {
            logERROR("calling create file with no filename");
        }

        if (!sdCardDir.exists()) {
            logERROR("Couldn't find folder for saving locations file");
            return;
        }

        UtilsClass.currLocFile = new File(sdCardDir + "/" + locationFileName);
        boolean newfile = true;
        if (!UtilsClass.currLocFile.exists()) {
            try {
                newfile = UtilsClass.currLocFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "exception was thrown COULDN'T CREATE LOCATIONS FILE", e);
            }
        }
        if (!UtilsClass.currLocFile.exists() || !newfile) {
            Log.e(TAG, "Locations File Not Created");
        } else {
            logINFO("Starting new Log - File " + locationFileName + " is used for location logging");
            UtilsClass.pushFileToList(currLocFile.toString());
        }
    }

    //Append
    public static void writeDataToFile(String msg) {
        try {
            FileWriter fileWriter;
            if (MainActivity.recording) {
                fileWriter = new FileWriter(currLocFile, true);
            } else {
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

    public static JSONObject locationToJSON(Location location, long event_time, long nano_time) {
        JSONObject locJSON = new JSONObject();
        try {
            locJSON.put("Provider", location.getProvider());
            locJSON.put("Latitude", location.getLatitude());
            locJSON.put("Longitude", location.getLongitude());
            locJSON.put("Time", location.getTime());
            locJSON.put("LocalTimestamp", event_time);
            locJSON.put("LocalNanostamp", nano_time);
            if (location.hasAccuracy())
                locJSON.put("Accuracy", location.getAccuracy());  //in meters
            if (location.hasAltitude())
                locJSON.put("Altitude", location.getAltitude()); //in meters above the WGS 84 reference ellipsoid
            if (location.hasBearing())
                locJSON.put("Bearing", location.getBearing()); //[0, 360] travel bearing (NOT related to device orientation)
            if (location.hasSpeed()) locJSON.put("Velocity", location.getSpeed());    //in m/s
        } catch (JSONException e) {
            //e.printStackTrace();
            UtilsClass.logDEBUG("ERROR @ JSON lvl1 " + e.getMessage());
        }
        return locJSON;
    }

    public static JSONObject sensorToJSON(SensorEvent event, int sensorType, long event_time, long nano_time) throws JSONException {
        String json;
        if (sensorType == 1) {    //ACC
            json = "{\n"
                    + " \"Type\" : \"ACCELERATION\", "
                    + " \"X\" : " + event.values[0] + ", "
                    + " \"Y\" : " + event.values[1] + ", "
                    + " \"Z\" : " + event.values[2] + ", "
                    + " \"Time\" : " + event.timestamp + ", "
                    + " \"LocalTimestamp\" : " + event_time + ", "
                    + " \"LocalNanostamp\" : " + nano_time + ", "
                    + " \"Accuracy\" : " + event.accuracy + "\n "
                    + "}";
        } else if (sensorType == 2) {    //MAGNETIC FIELD
            json = "{\n"
                    + " \"Type\" : \"MAGNETIC FIELD\", "
                    + " \"X\" : " + event.values[0] + ", "
                    + " \"Y\" : " + event.values[1] + ", "
                    + " \"Z\" : " + event.values[2] + ", "
                    + " \"Time\" : " + event.timestamp + ", "
                    + " \"LocalTimestamp\" : " + event_time + ", "
                    + " \"LocalNanostamp\" : " + nano_time + ", "
                    + " \"Accuracy\" : " + event.accuracy + "\n "
                    + "}";
        } else if (sensorType == 11) {    //ROTATION VECTOR (COMPOSITE)
            json = "{\n"
                    + " \"Type\" : \"ROTATION VECTOR\", "
                    + " \"X\" : " + event.values[0] + ", "
                    + " \"Y\" : " + event.values[1] + ", "
                    + " \"Z\" : " + event.values[2] + ", "
                    + " \"Time\" : " + event.timestamp + ", "
                    + " \"LocalTimestamp\" : " + event_time + ", "
                    + " \"LocalNanostamp\" : " + nano_time + ", "
                    + " \"Accuracy\" : " + event.accuracy + "\n "
                    + "}";
        }else{  //UNHANDLED
            return new JSONObject("ERROR [Unknown sensor type in sensorToJSON");
        }
        //TODO prettify try-catch and handle return
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            //e.printStackTrace();
            UtilsClass.logDEBUG("ERROR @ JSON lvl1 " + e.getMessage());
        }
        return new JSONObject("ERROR");
    }

    public static JSONObject orientationToJSON(float[] orientation, long event_time) throws JSONException {
        String json;
        json = "{\n"
                + " \"Type\" : \"ORIENTATION\", "
                + " \"X\" : " + orientation[0] + ", "
                + " \"Y\" : " + orientation[1] + ", "
                + " \"Z\" : " + orientation[2] + ", "
                + " \"LocalTimestamp\" : " + event_time + "\n"
                + "}";
        return new JSONObject(json);
    }

    /*
        //TODO we put it in single-line for testing (format it after that)
        public static String SensorDataToString(SensorEvent event) {
            String sensorDataString = "{"
                    + " \"Sensor\" : \"" + event.sensor.getName() + "\", "
                    + " \"Values\" : " + event.values + ", "
                    + " \"Time (Local)\" : " + event.timestamp + ", "
                    + " \"Accuracy\" : " + event.accuracy
                    + "}";
            return sensorDataString;
        }
    */
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
        if (!sdCardDir.exists()) {
            logToFile = sdCardDir.mkdirs();
        }
        UtilsClass.logFile = new File(sdCardDir + "/" + logFileName);
        boolean newfile = true;
        if (!UtilsClass.logFile.exists()) {
            try {
                newfile = UtilsClass.logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "exception was thrown", e);
            }
        }
        if (!UtilsClass.logFile.exists() || !newfile) {
            Log.e(TAG, "File Not Created - using Android Log for logging");
            return false;
        } else {
            logINFO("Starting new Log - File " + logFileName + " is used for logging");
            UtilsClass.pushFileToList(logFile.toString());
        }
        return true;

    }

}