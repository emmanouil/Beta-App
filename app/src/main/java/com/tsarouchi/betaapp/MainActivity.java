package com.tsarouchi.betaapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;

    private static enum CamType {NONE, NATIVE, INTENT}

    private Uri fileUri;
    private Camera camera;
    private CameraPreview camPreview;
    private CamType camtype;
    private MediaRecorder mr;
    private boolean recording = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        camtype = CamType.NONE;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recording) {
                    Snackbar.make(view, "Stopping recording", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                    stopRecording();
                    //destroyCamera();  //we keep it for further recordings
                    destroyMediaRecorder();
                } else {
                    startRecording();
                    Snackbar.make(view, "Starting recording", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                }
                //destroyCamera();
                //startCameraIntent();


                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                */
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

    public void startCameraIntent() {
        // create Intent to take a video and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO); // create a file to save the video
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the video file name
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set the video quality

        // start the image capture Intent
        startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);

        camtype = CamType.INTENT;
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
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
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
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

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

        if (BuildConfig.DEBUG) {
            Camera.Parameters params = camera.getParameters();
            Camera.CameraInfo infoz = new Camera.CameraInfo();
            Camera.getCameraInfo(0, infoz);
        }

        camtype = CamType.NATIVE;
        camera.setDisplayOrientation(90);
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
        mr.setCamera(camera);
        mr.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mr.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mr.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        UtilsClass.logINFO("Video dir: " + getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
        mr.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
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
        recording = false;

    }

}
