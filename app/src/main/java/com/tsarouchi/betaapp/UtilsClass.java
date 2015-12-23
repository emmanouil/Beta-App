package com.tsarouchi.betaapp;

import android.app.Application;
import android.content.Context;

/**
 * Created by Emmanouil on 17-Dec-15.
 */
public class UtilsClass extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        UtilsClass.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return UtilsClass.context;
    }
}