package com.hnt.smartvotingsystem.smartvotingsystem;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import it.sephiroth.android.library.picasso.Picasso;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Created by madhav on 2/9/2017.
 */

public class profile extends AppCompatActivity {



    public static final String DATA_URL = "http://hntdatabase.16mb.com/getuserdata.php?username=";
    public static final String KEY_PROFILENAME = "name";
    public static final String KEY_PROFILEPHONE = "phone";
    public static final String KEY_PROFILEAADHAR= "aadharcard";

    public static final String JSON_ARRAY = "result";

    String usernamekey;


    TextView textViewname,textViewusername,textViewphone,textViewaadharcard;
    ImageView image,image2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

         image = (ImageView)findViewById(R.id.dispalyimageView1);
         textViewname = (TextView)findViewById(R.id.displayname);
         textViewusername = (TextView)findViewById(R.id.displayTextUsername);
         textViewphone = (TextView)findViewById(R.id.displayphoneno);
         textViewaadharcard = (TextView)findViewById(R.id.displayaadhar);

        SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        usernamekey = saved_values.getString("usernamekey", "null");

        //Fetch Image
        Picasso.with(getApplicationContext()).load("http://hntdatabase.16mb.com/PhotoUpload/"+usernamekey+".png").into(image);

        //fetch userinfo
        getData();
    }


    private void getData() {


        if (usernamekey.equals("")) {
            Toast.makeText(this, "Please enter an username", Toast.LENGTH_LONG).show();
            return;
        }
         final ProgressDialog loading = ProgressDialog.show(this,"Please wait...","Fetching...",false,false);

        String url = DATA_URL+usernamekey;

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loading.dismiss();
                showJSON(response);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(profile.this, error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void showJSON(String response){
        String name="";
        String phone="";
        String aadharcard = "";
        String imagepath = "";
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray(JSON_ARRAY);
            JSONObject collegeData = result.getJSONObject(0);
            name = collegeData.getString(KEY_PROFILENAME);
            phone = collegeData.getString(KEY_PROFILEPHONE);
            aadharcard = collegeData.getString(KEY_PROFILEAADHAR);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        textViewname.setText("Name : "+name);
        textViewusername.setText("Username : "+usernamekey);
        textViewphone.setText("Phone no : "+phone);
        textViewaadharcard.setText("Aadhar Card No : "+aadharcard);

    }

}