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

    private final static Location coordNorthPole = new Location("manual");
    static{
        coordNorthPole.setLatitude(90d);
        coordNorthPole.setLongitude(0d);
        coordNorthPole.setAltitude(0d);
    }

    private LocationManager locationManager;
    private coordLVL coordsType = coordLVL.BOTH;
    private Location gpsLoc, netLoc;

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor, orientationSensor, magnetometer;
    private SensorActivity sensorActivity;


    public enum coordLVL {GPS, NET, BOTH}

    public Metadata(Context context) {
        initiateLocationServices(context);
        sensorActivity = new SensorActivity(context);
    }


    private void initiateLocationServices(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                UtilsClass.logDEBUG("location listener called");
                recordLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

// Register the listener with the Location Manager to receive location updates
        try {

            switch (coordsType) {
                case GPS:
                    UtilsClass.logINFO("Getting coordinates from GPS");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    break;
                case NET:
                    UtilsClass.logINFO("Getting coordinates from Network");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                    break;
                case BOTH:
                    UtilsClass.logINFO("Getting coordinates from Network and GPS");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                    break;
                default:
                    UtilsClass.logERROR("No Coordinate source specified");
                    break;
            }
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException e) {
            UtilsClass.logERROR("We do not have permission to access Location Services " + e);
            e.printStackTrace();
        }

    }

    private void recordLocation(Location location) {
        UtilsClass.logINFO("Bearing: "+location.bearingTo(coordNorthPole));
        try {
            //TODO 1. Do we really need JSON convertion, since we re-stringify?
            //TODO 2. Error-handling
            UtilsClass.writeDataToFile(UtilsClass.locationToJSON(location).toString());
        } catch (JSONException e) {
            e.printStackTrace();
            UtilsClass.logDEBUG("ERROR @ JSON lvl2");
        }
        //UtilsClass.writeDataToFile(location.toString());
    }


}

/**
 * Adds (and register) a sensor listener
 * we use it for measuring magnetic field (for now)
 */
class SensorActivity implements SensorEventListener {
    private final SensorManager sensorManager;
    private final Sensor magnetometer;
    private final Sensor accelerometer;
    private final Sensor gyrometer;
    private float[] lastAcc;
    private float[] lastMagn;
    private float rotation[] = new float[9];
    private float identity[] = new float[9];
    private boolean newAcc = false, newMagn = false;

    public SensorActivity(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyrometer = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        //TODO handle register and unregister listener
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyrometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onResume() {
        UtilsClass.logINFO("resumed");
        //super.onResume();
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
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
                lastAcc = event.values;
                UtilsClass.logINFO("acc length: "+lastAcc.length);
                newAcc = true;
                UtilsClass.writeDataToFile(UtilsClass.SensorDataToString(event));
                break;
            case Sensor.TYPE_GYROSCOPE:
                UtilsClass.logINFO("GYRO WHAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                UtilsClass.writeDataToFile(UtilsClass.SensorDataToString(event));
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                lastMagn = event.values;
                newMagn = true;
                UtilsClass.logINFO("magn length: "+lastMagn.length);
                UtilsClass.writeDataToFile(UtilsClass.SensorDataToString(event));
                break;
        }

        if(newAcc || newMagn){
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
        }



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        UtilsClass.logINFO(sensor.getName() + "  CURR ACC:" + accuracy);
    }
}

