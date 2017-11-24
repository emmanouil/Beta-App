package com.tsarouchi.betaapp;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public void startCamera(View view) {
        if (recording) {
            Snackbar.make(view, "Stopping recording", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            stopRecording();
            //destroyCamera();  //we keep it for further recordings
            destroyMediaRecorder();
        } else {
            startRecording();
            Snackbar.make(view, "Starting recording", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        }
    }

    private enum CamType {NONE, NATIVE}

    private Camera camera;
    private CameraPreview camPreview;
    private MediaRecorder mr;
    public static boolean recording = false;
    public static String last_timestamp;
    SensorActivitySingleThread sensorActivity;
    LocationActivity locationActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (UtilsClass.getLogToFile())
            UtilsClass.logINFO("Logging to file: " + UtilsClass.getLogFile());


        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2VideoFragment.newInstance())
                    .commit();
        }

/*
        initCamera();
        */
        sensorActivity = new SensorActivitySingleThread(this.getBaseContext());
        locationActivity = new LocationActivity(this.getBaseContext());

    }


    //TODO implement onPause/Resume for logging as well
    @Override
    protected void onPause(){
        super.onPause();
        if(recording){
            stopRecording();
            //destroyCamera();  //we keep it for further recordings
            destroyMediaRecorder();
        }
        sensorActivity.onPause();
        UtilsClass.refreshFileList(getApplicationContext());
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorActivity.onResume();
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

    public void onWindowFocusChanged(boolean hasFocus){
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        //OR conteview extents behind status bar
        //int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        /* We do not use an actionbar for now
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.hide();
        }
        */
    }

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    /**
     * Create a File for saving an image or video
     * WARNING: it creates a file EACH time it's being called
     */
    private static File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                UtilsClass.logERROR("MyCameraApp failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        last_timestamp = timeStamp;
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "VID_" + timeStamp + ".mp4");
        UtilsClass.logINFO("Created file: " + mediaFile.toString());
        UtilsClass.pushFileToList(mediaFile.toString());

        UtilsClass.createVideoLocationFile();

        return mediaFile;
    }


    private void initCamera() {
        try {
            //this is for the back-facing camera
            camera = Camera.open();
        } catch (Exception e) {
            UtilsClass.logERROR("Couldn't open camera: " + e);
            return;
        }
/*
        if (BuildConfig.DEBUG) {
            Camera.Parameters params = camera.getParameters();
            UtilsClass.logDEBUG(params.flatten());
            Camera.CameraInfo infoz = new Camera.CameraInfo();
            Camera.getCameraInfo(0, infoz);
        }
        */

//        camera.setDisplayOrientation(90);
        camPreview = new CameraPreview(this, camera);
        FrameLayout framePreview = (FrameLayout) findViewById(R.id.camera_preview);
        framePreview.addView(camPreview);
    }

    private void destroyCamera() {
        //if (recording) destroyMediaRecorder();
        camera.stopPreview();
        camera.release();
    }

    private void destroyMediaRecorder() {
        mr.stop();
        mr.reset();
        mr.release();
        //camera.lock();
    }

    private void startRecording() {

        recording = true;
        mr = new MediaRecorder();
        camera.unlock();
        if (Build.MODEL.toString().contains("Nexus 5X")) {
            mr.setOrientationHint(180);
        }
        mr.setCamera(camera);
        mr.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mr.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//        mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mr.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//        mr.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        mr.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        mr.setOutputFile(getOutputMediaFile().toString());
        mr.setPreviewDisplay(camPreview.getHolder().getSurface());
        try {
            mr.prepare();
            UtilsClass.logINFO("MediaRecorder Ready");
        } catch (IOException e) {
            UtilsClass.logERROR("MediaRecorder Error: " + e);
            destroyCamera();
            recording = false;
            return;
        }

        mr.start();

//TODO #1 add file management for output file
    }

    private void stopRecording() {
        UtilsClass.logINFO("Stopping Recording at: " + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        recording = false;

    }

}
