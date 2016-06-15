package com.tsarouchi.betaapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Context;
import android.os.Bundle;

import org.json.JSONException;

/**
 * Created by Emmanouil on 25-Mar-16.
 */
public class Metadata {


    private Location gpsLoc, netLoc;

    private SensorManager sensorManager;
    private Sensor orientationSensor, magnetometer;
    private SensorActivity sensorActivity;
    private LocationActivity locationActivity;



    public Metadata(Context context) {
        locationActivity = new LocationActivity(context);
        sensorActivity = new SensorActivity(context);
    }

}