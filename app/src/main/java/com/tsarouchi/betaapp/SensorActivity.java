package com.tsarouchi.betaapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.tsarouchi.betaapp.UtilsClass;

class SensorActivity implements SensorEventListener {

//Startof Options

//Endof Options

    private final SensorManager sensorManager;
    private final Sensor magnetometer;
    private final Sensor accelerometer;
    private final Sensor rot;
    //private final Sensor gyrometer;
    private float[] lastAcc;
    private float[] lastMagn;
    private float[] lastRot;
    private float rotation[] = new float[9];
    private float identity[] = new float[9];
    private boolean newAcc = false, newMagn = false, newRot = false;
    private static long event_time = 0;

    public SensorActivity(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        rot = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        //gyrometer = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        //TODO handle register and unregister listener
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, rot, SensorManager.SENSOR_DELAY_UI);
        //sensorManager.registerListener(this, gyrometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onResume() {
        UtilsClass.logINFO("resumed");
        //super.onResume();
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, rot, SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause() {
        //super.onPause();
        UtilsClass.logINFO("paused");
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        switch(event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                event_time = System.currentTimeMillis();
                lastAcc = event.values;
                newAcc = true;
                recordSensor(event, Sensor.TYPE_ACCELEROMETER, event_time);
                // UtilsClass.writeDataToFile(UtilsClass.SensorDataToString(event));
                break;
            case Sensor.TYPE_GYROSCOPE:
                //UtilsClass.writeDataToFile(UtilsClass.SensorDataToString(event));
                UtilsClass.logERROR("Received Not handled Gyroscope Event");
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                event_time = System.currentTimeMillis();
                lastMagn = event.values;
                newMagn = true;
                recordSensor(event, Sensor.TYPE_MAGNETIC_FIELD, event_time);
                // UtilsClass.writeDataToFile(UtilsClass.SensorDataToString(event));
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                UtilsClass.logERROR("Received Not handled Rotation Vector Event");
                lastRot = event.values;
                newRot = true;
                break;
        }

        if(newRot){
            newRot = false;
            UtilsClass.logINFO("ROTA:   "+lastRot[0]+" ,  "+lastRot[1]+" ,  "+lastRot[2]);
            return;
        }

        //if(newAcc || newMagn){
        newAcc = newMagn = false;
        boolean gotRotation = false;
        try {
            gotRotation = SensorManager.getRotationMatrix(rotation, identity, lastAcc, lastMagn);
        } catch (Exception e) {
            gotRotation = false;
            UtilsClass.logERROR("one of the two is null  (if it's once, it's ok)"+ e.getMessage());
        }
        if (gotRotation) {
            //Orientation Vector
            float orientation[] = new float[3];
            SensorManager.getOrientation(rotation, orientation);
            UtilsClass.logINFO("Orientation:   "+orientation[0]+" ,  "+orientation[1]+" ,  "+orientation[2]);
        }
        //}



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // UtilsClass.logINFO(sensor.getName() + "  CURR ACC:" + accuracy);
    }


    private void recordSensor(SensorEvent event, int sensorType){
        _recordSensor(event, sensorType, -1);   //TODO: merge timed/untimed the methods
    }

    private void recordSensor(SensorEvent event, int sensorType, long event_time) {
        _recordSensor(event, sensorType, event_time);
    }

    private void _recordSensor(SensorEvent event, int sensorType, long event_time) {
        //UtilsClass.logINFO("Bearing: "+location.bearingTo(NORTH_POLE));
        try {
            //TODO 1. Do we really need JSON convertion, since we re-stringify?
            //TODO 2. Error-handling
            //TODO 3. NOTE: check time
            UtilsClass.writeDataToFile(UtilsClass.sensorToJSON(event, sensorType, event_time).toString());
        } catch (JSONException e) {
            e.printStackTrace();
            UtilsClass.logDEBUG("ERROR @ JSON lvl2");
        }
        //UtilsClass.writeDataToFile(location.toString());
    }



}