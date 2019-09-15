package com.example.asl_ml;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends AppCompatActivity {
    private Camera camera;
    private Button button;
    public ArrayList<String> files;

    int IMAGE_CACHE_SIZE = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideActivityBar();
        releaseCameraAndPreview();
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);

        final short camera_id = 0;
        // Open Camera
        boolean camera_opened = safeCameraOpen(camera_id);
        if(camera_opened){
            setCameraDisplayOrientation(this, camera_id, camera);
            // Set previews
            CameraPreview preview = new CameraPreview(getApplicationContext(), camera);
            FrameLayout frameLayout = findViewById(R.id.camera_frame);
            frameLayout.addView(preview);

            define_file_cache(IMAGE_CACHE_SIZE);

            defineImageCapture();

        }
    }

    public void defineRecordActivity(final Camera.PictureCallback mPicture){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setClickable(false);
                final Timer timer = new Timer();
                int begin = 0;
                int timeInterval = 333;
                timer.schedule(new TimerTask() {
                    int counter = 0;
                    @Override
                    public void run() {
                        counter++;
                        camera.takePicture(null, null, mPicture);
                        if (counter >= IMAGE_CACHE_SIZE){
                            button.setClickable(true);
                            timer.cancel();

                        }
                    }
                }, begin, timeInterval);
            }
        });
    }

    public void defineImageCapture(){
        // Every time an image is retrieved, we need to pull it from the front and push the name to the back.
        final Camera.PictureCallback mPicture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null){
                    Log.d("camera", "Error creating media file, check storage permissions");
                    return;
                }
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d("camera", "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d("camera", "Error accessing file: " + e.getMessage());
                }
            }
        };
        defineRecordActivity(mPicture);
    }

    public void hideActivityBar(){
        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e){}
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId,
                                                   android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }



    private void define_file_cache(int size){
        // Define file names for file cache
        files = new ArrayList<>();
        for(int x = 0; x < IMAGE_CACHE_SIZE; x++){
            files.add(x+".JPG");
        }
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.


        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + files.get(0));
            Log.e(getString(R.string.app_name), "Output file::" + mediaStorageDir.getPath() + File.separator + files.get(0));
            files.add(files.get(0));
            files.remove(0);
        } else if(type == MEDIA_TYPE_VIDEO) {
            // TODO:: This is not implemented yet, do not use
            // mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + ".mp4");
            return null;
        } else {
            return null;
        }

        return mediaFile;
    }

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;
        try {
            releaseCameraAndPreview();
            camera = Camera.open(id);
            qOpened = (camera != null);
            Log.e(getString(R.string.app_name), "Camera Opened Successfully");

        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }
        return qOpened;
    }
    private void releaseCameraAndPreview() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}
