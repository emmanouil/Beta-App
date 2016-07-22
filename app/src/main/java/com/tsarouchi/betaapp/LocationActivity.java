package com.tsarouchi.betaapp;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Created by Emmanouil on 13-Jun-16.
 * <p>
 * Location data handling
 */
public class LocationActivity {

    //Startof Options
    private static final coordLVL LOCATION_SERVICE = coordLVL.BOTH;
//Endof Options

    public enum coordLVL {GPS, NET, BOTH}

    private final static Location NORTH_POLE = new Location("manual");

    static {
        NORTH_POLE.setLatitude(90d);
        NORTH_POLE.setLongitude(0d);
        NORTH_POLE.setAltitude(0d);
    }

    private static long event_time = 0, nano_time = 0;


    public LocationActivity(Context context) {

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                nano_time = System.nanoTime();
                event_time = System.currentTimeMillis();
                UtilsClass.logDEBUG("location listener called");
                recordLocation(location, event_time, nano_time);
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

            switch (LOCATION_SERVICE) {
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


    private void recordLocation(Location location, long event_time, long nano_time) {
        //UtilsClass.logINFO("Bearing: "+location.bearingTo(NORTH_POLE));
        //TODO 1. Do we really need JSON convertion, since we re-stringify?
        //TODO 2. Error-handling
        //TODO 3. NOTE: time (timestamp) is in ms
        UtilsClass.writeDataToFile(UtilsClass.locationToJSON(location, event_time, nano_time).toString());
    }


}
