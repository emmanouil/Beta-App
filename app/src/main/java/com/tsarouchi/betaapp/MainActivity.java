package com.tsarouchi.betaapp;

import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    private Camera camera;
    private CameraPreview preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //CoordinatorLayout coord = (CoordinatorLayout) findViewById(R.id.coord);
        /*
        if(checkCameraHardware(UtilsClass.getAppContext())){
            Snackbar.make(coord, "We have Context", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }*/
        if (UtilsClass.getLogToFile())
            UtilsClass.logINFO("Logging to file: " + UtilsClass.getLogFile());

        initCamera();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Check if this device has a camera
     * It should, since we declare it on manifest, but just to be on the safe side
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private void initCamera(){
        try{
            //this is for the back-facing camera
            camera = Camera.open();
        }catch (Exception e){
            UtilsClass.logERROR("Couldn't open camera: "+e);
            return;
        }

        if(BuildConfig.DEBUG) {
            Camera.Parameters params = camera.getParameters();
            Camera.CameraInfo infoz = new Camera.CameraInfo();
            Camera.getCameraInfo(0, infoz);
        }

        preview = new CameraPreview(this, camera);
        FrameLayout framePreview = (FrameLayout) findViewById(R.id.camera_preview);
        framePreview.addView(preview);
    }


}
