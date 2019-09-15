package com.example.asl_ml;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends AppCompatActivity {
    private Camera camera;
    private CameraPreview preview;
    private Button button;
    public ArrayList<String> files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        releaseCameraAndPreview();
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);

        final short camera_id = 0;
        boolean camera_opened = safeCameraOpen(camera_id);
        if(camera_opened){
            CameraPreview preview = new CameraPreview(getApplicationContext(), camera);
            FrameLayout frameLayout = findViewById(R.id.camera_frame);
            frameLayout.addView(preview);

            files = new ArrayList<String>();
            int image_cache = 60;
            for(int x = 0; x < image_cache; x++){
                files.add(x+".JPG");
            }
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
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    camera.takePicture(null, null, mPicture);
                }
            });
        }

        /*int failed_attempts = 0;
        while (!camera_opened){
            failed_attempts++;
            camera_id++;
            Log.e("ASL-ML",
                    "Attempting to reopen camera \n Failed Attempts :: " + failed_attempts);
            camera_opened = safeCameraOpen(camera_id);
        }*/
        // TODO:: Get Camera Preview

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
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + ".mp4");
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
