package com.tsarouchi.betaapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.json.JSONException;

import java.util.List;

/**
 * Created by Emmanouil on 30-Jun-16.
 */

public class SensorActivitySingleThread implements SensorEventListener {

    private final SensorManager sensorManager;
    private final Sensor magnetometer;
    private final Sensor accelerometer;
    private final Sensor rot;
    private float[] lastAcc;
    private float[] lastMagn;
    private float[] lastRot;
    private float[] orientation;
    private float rotationMx[] = new float[9];
    @SuppressWarnings("CanBeFinal")
    private float rotationMxRaw[] = new float[9];
    private float identity[] = new float[9];
    private boolean newRot = false;


    public SensorActivitySingleThread(Context context) {


        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        //listing available sensors
        List<Sensor> mList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (int i = 1; i < mList.size(); i++) {
            UtilsClass.logDEBUG(mList.get(i).getName());
        }

        rot = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        registerListeners();

//        time_diff = SystemClock.uptimeMillis();
//        time_base = System.currentTimeMillis();
    }

    private void registerListeners(){
        if(rot!=null){
            UtilsClass.logINFO("Using Rotation Vector composite sensor for device orientation");
            sensorManager.registerListener(this, rot, SensorManager.SENSOR_DELAY_UI);
        }else if(magnetometer!= null && accelerometer != null) {
            UtilsClass.logINFO("Using Acceleration & Magnetic Field sensors for device orientation");
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }else{
            UtilsClass.logERROR("No usable sensors found for device orientation");
        }
    }

    protected void onResume() {
        UtilsClass.logINFO("resumed");
        //super.onResume();
        registerListeners();
    }

    protected void onPause() {
        //super.onPause();
        UtilsClass.logINFO("paused");
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {

            case Sensor.TYPE_ACCELEROMETER:
                long nano_time = System.nanoTime();
                long event_time = System.currentTimeMillis();
                lastAcc = event.values;
                boolean gotOrient = calculateRotationMx();
                recordSensor(event, Sensor.TYPE_ACCELEROMETER, event_time, nano_time);
                if (!gotOrient) return;
                calculateOrientation();
                recordOrientation(orientation, event_time);
                // UtilsClass.writeDataToFile(UtilsClass.SensorDataToString(event));
                break;
            case Sensor.TYPE_GYROSCOPE:
                //UtilsClass.writeDataToFile(UtilsClass.SensorDataToString(event));
                UtilsClass.logERROR("Received Not handled Gyroscope Event");
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                nano_time = System.nanoTime();
                event_time = System.currentTimeMillis();
                lastMagn = event.values;
                recordSensor(event, Sensor.TYPE_MAGNETIC_FIELD, event_time, nano_time);
                // UtilsClass.writeDataToFile(UtilsClass.SensorDataToString(event));
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                UtilsClass.logERROR("Received Not handled Rotation Vector Event");
                lastRot = event.values;
                newRot = true;
                break;
        }

        if (newRot) {
            newRot = false;
            UtilsClass.logINFO("ROTA:   " + lastRot[0] + " ,  " + lastRot[1] + " ,  " + lastRot[2]);
        }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // UtilsClass.logINFO(sensor.getName() + "  CURR ACC:" + accuracy);
    }

    private void recordSensor(SensorEvent event, int sensorType, long event_time, long nano_time) {
        //UtilsClass.logINFO("Bearing: "+location.bearingTo(NORTH_POLE));
        try {
            //TODO 1. Do we really need JSON convertion, since we re-stringify?
            //TODO 2. Error-handling
            //TODO 3. NOTE: check time
            UtilsClass.writeDataToFile(UtilsClass.sensorToJSON(event, sensorType, event_time, nano_time).toString());
        } catch (JSONException e) {
            e.printStackTrace();
            UtilsClass.logDEBUG("ERROR @ JSON lvl2");
        }
        //UtilsClass.writeDataToFile(location.toString());
    }

    private void recordOrientation(float[] orientation, long event_time) {
        try {
            UtilsClass.writeDataToFile(UtilsClass.orientationToJSON(orientation, event_time).toString());
        } catch (JSONException e) {
            e.printStackTrace();
            UtilsClass.logDEBUG("ERROR @ JSON lvl2");
        }
    }

    private boolean calculateRotationMx() {
        boolean gotRotation;

        try {
            gotRotation = SensorManager.getRotationMatrix(rotationMxRaw, null, lastAcc, lastMagn);
        } catch (Exception e) {
            gotRotation = false;
            UtilsClass.logERROR("Error getting rotation matrix" + e.getMessage());
        }

        if (gotRotation) {
            rotationMx = rotationMxRaw.clone();
        }

/*
        if (gotRotation) {
            //NOTE: the rotation considered is according to device natural orientation
            //and it might NOT be the same as the screen orientation
            switch (displayDev.getRotation()) {
                case 1: //Surface.ROTATION_90
                    rotationMx = rotationMxRaw.clone();
                    break;
                case 2: //Surface.ROTATION_180
                    SensorManager.remapCoordinateSystem(rotationMxRaw, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, rotationMx);
                    break;
                case 3: //Surface.ROTATION_270
                    SensorManager.remapCoordinateSystem(rotationMxRaw, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, rotationMx);
                    break;
                case 0: //no rotation (= Surface.ROTATION_0)
                    SensorManager.remapCoordinateSystem(rotationMxRaw, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, rotationMx);
                    break;
            }
        }
*/
        return gotRotation;
    }

    private void calculateOrientation() {
        float[] tmpOrient = new float[3];
        SensorManager.getOrientation(rotationMx, tmpOrient);
        orientation = tmpOrient.clone();
//        UtilsClass.logINFO("Received Orientation - Yaw: " + orientation[0] + " , Pitch: " + orientation[1] + " , Roll: " + orientation[2]);
    }


}
