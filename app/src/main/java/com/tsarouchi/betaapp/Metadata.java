package com.tsarouchi.betaapp;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.content.Context;

/**
 * Created by Emmanouil on 25-Mar-16.
 *
 * Not used for now
 */
public class Metadata {


    private Location gpsLoc, netLoc;

    private SensorManager sensorManager;
    private Sensor orientationSensor, magnetometer;


    public Metadata(Context context) {
        LocationActivity locationActivity = new LocationActivity(context);
        SensorActivity sensorActivity = new SensorActivity(context);
    }

}