package com.tsarouchi.betaapp;

import java.io.File;

/**
 * Created by Emmanouil on 05-Apr-16.
 */
public class Options {

    public enum coordLVL {GPS, NET, BOTH}


    public coordLVL getCoordsType() {
        return coordsType;
    }

    public void setCoordsType(coordLVL coordsType) {
        this.coordsType = coordsType;
    }

    private coordLVL coordsType = coordLVL.BOTH;


    public static File getAppDir() {
        return appDir;
    }

    public static void setAppDir(File appDir) {
        Options.appDir = appDir;
    }

    private static File appDir;


}
