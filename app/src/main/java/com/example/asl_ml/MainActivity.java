package com.example.asl_ml;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dispatchTakePictureIntent();


        setContentView(R.layout.activity_main);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            System.out.println("WIDTH" + imageBitmap.getWidth());
            System.out.println("HEIGHT" + imageBitmap.getHeight());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Bitmap.createScaledBitmap(imageBitmap, 120, 120, false).compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            System.out.println("LENGTH " + byteArray.length);

            System.out.println("Byte Array");
            for (byte b : byteArray) {
                System.out.print(b);
            }

            sendRequest(byteArray);

            Bitmap decodedByte = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

            imageView = (ImageView) findViewById(R.id.cameraDisplay);
            imageView.setImageBitmap(decodedByte);
        }
    }

    private void sendRequest(final byte[] byteArray) {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://aslml-252919.appspot.com"; // Change URL to match our server

        System.out.println("HERE");

        JSONObject jsonObject = new JSONObject();

        StringBuilder sb = new StringBuilder();

        for(byte b : byteArray) {
            sb.append(b);
        }

        try{
            jsonObject.put("byteArray", sb);
        } catch (Exception e){
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("RESPONSE1: " + response.toString().substring(0,response.toString().length()/2));
                        System.out.println("RESPONSE2: " + response.toString().substring(response.toString().length()/2, response.toString().length()));

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        error.printStackTrace();
                    }
                });

        queue.add(jsonObjectRequest);

    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


}
