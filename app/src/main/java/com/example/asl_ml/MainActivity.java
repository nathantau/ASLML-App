package com.example.asl_ml;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.api.services.translate.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.*;
import com.google.cloud.translate.Translate;


import org.json.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static ImageView imageView;
    static String responseString = "";
    String language;

    private String inputToTranslate;
    private TextView initialTv;
    private TextView translatedTv;
    private String originalText;
    private String translatedText = "";
    private Spinner textSpinner;
    private boolean connected;

    Translate translate;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dispatchTakePictureIntent();


        setContentView(R.layout.activity_main);

        initialTv = findViewById(R.id.initialTv);
        inputToTranslate = responseString;
        translatedTv = findViewById(R.id.translatedTv);
        textSpinner = findViewById(R.id.textSpinner);
        Button translateButton = findViewById(R.id.translateButton);

        initialTv.setText(responseString);
        translatedTv.setText(responseString);

        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkInternetConnection()) {

                    //If there is internet connection, get translate service and start translation:
                    getTranslateService();
                    translate();

                } else {

                    //If not, display "no connection" warning:
                    translatedTv.setText(getResources().getString(R.string.no_connection));
                }

            }
        });

    }


    public void getTranslateService() {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try (InputStream is = getResources().openRawResource(R.raw.credentials)) {

            //Get credentials:
            final GoogleCredentials myCredentials = GoogleCredentials.fromStream(is);

            //Set credentials and get translate service:
            TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(myCredentials).build();

            translate = translateOptions.getService();

        } catch (IOException ioe) {
            ioe.printStackTrace();

        }
    }

    public void translate() {

        String translated = textSpinner.getSelectedItem().toString();

        if(translated.equals("English")) {
            language = "en";
        } else if (translated.equals("French")) {
            language = "fr";
        } else if (translated.equals("Spanish")) {
            language = "es";
        } else if (translated.equals("Chinese")) {
            language = "zh";
        } else if (translated.equals("German"))
            language = "de";

        //Get input text to be translated:
        originalText = inputToTranslate;
        Translation translation = translate.translate(
                originalText,
                Translate.TranslateOption.targetLanguage(language),
                Translate.TranslateOption.model("base"));
        translatedText = translation.getTranslatedText();

        //Translated text and original text are set to TextViews:

        translatedTv.setText(translatedText);

    }

    public boolean checkInternetConnection() {

        //Check internet connection:
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //Means that we are connected to a network (mobile or wi-fi)
        connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

        return connected;
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

//                        try {
                            //responseString = response.getString("text")
                            responseString = "hello";
                            initialTv.setText(responseString);


//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }

//                        System.out.println("RESPONSE1: " + response.toString().substring(0,response.toString().length()/2));
//                        System.out.println("RESPONSE2: " + response.toString().substring(response.toString().length()/2, response.toString().length()));

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
