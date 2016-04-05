package com.tsarouchi.betaapp;

import java.io.File;

/**
 * Created by Emmanouil on 05-Apr-16.
 */
public class Options {


    public static File getAppDir() {
        return appDir;
    }

    public static void setAppDir(File appDir) {
        Options.appDir = appDir;
    }

    private static File appDir;


}
