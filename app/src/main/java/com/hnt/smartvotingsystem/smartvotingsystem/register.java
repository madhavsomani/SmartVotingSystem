package com.hnt.smartvotingsystem.smartvotingsystem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import it.sephiroth.android.library.picasso.Picasso;

public class register extends AppCompatActivity  {

    //Creating views
    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextPhone;
    private EditText edittextname;
    private EditText edittextaddharcard;

    private AppCompatButton buttonRegister;
    private AppCompatButton choosefile;

    //Volley RequestQueue
    private RequestQueue requestQueue;

    //String variables to hold username password and phone
    private String username;
    private String password;
    private String phone;
    private String name;
    private String aadhar;
    private Bitmap bitmap;
    private Uri filePath;
    String usernamekey;
    public ImageView imageView2,fingerimage;
    private int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        //Initializing Views
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextPhone = (EditText) findViewById(R.id.editTextPhone);
        edittextname = (EditText) findViewById(R.id.editTextname);
        edittextaddharcard = (EditText) findViewById(R.id.editTextaadharcard);
        imageView2 = (ImageView)findViewById(R.id.imageView2);
        fingerimage = (ImageView)findViewById(R.id.fingerimage);
        buttonRegister = (AppCompatButton) findViewById(R.id.buttonRegister);
        choosefile = (AppCompatButton) findViewById(R.id.btchoosefile);
        Button btfingerprintscan = (Button)findViewById(R.id.btfingerprintscan);


        SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        usernamekey = saved_values.getString("usernamekey", "null");

        //Initializing the RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        //Adding a listener to button
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                register();
            }
        });

        choosefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }

        });

        btfingerprintscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor=saved_values.edit();
                editor.putString("usernamekey",editTextUsername.getText().toString());
                editor.commit();
                startActivity(new Intent(register.this,fingerscanregister.class));

            }
        });
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Picasso.with(getApplicationContext()).load("http://hntdatabase.16mb.com/fingeruploads/"+usernamekey+".bmp").into(fingerimage);
    }

    //this method will register the user
    private void register() {

        //Displaying a progress dialog
        final ProgressDialog loading = ProgressDialog.show(this, "Registering", "Please wait...", false, false);


        //Getting user data
        username = editTextUsername.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();
        phone = editTextPhone.getText().toString().trim();
        name = edittextname.getText().toString().trim();
        aadhar = edittextaddharcard.getText().toString().trim();


        //Again creating the string request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        try {
                            //Creating the json object from the response
                            JSONObject jsonResponse = new JSONObject(response);

                            //If it is success
                            if(jsonResponse.getString(Config.TAG_RESPONSE).equalsIgnoreCase("Success")){
                                //Asking user to confirm otp
                                Toast.makeText(register.this, "Successfully registered", Toast.LENGTH_LONG).show();

                                String path = Environment.getExternalStorageDirectory()
                                        + "//FingerData";
                                File dir = new File(path);
                                if(dir.exists() && dir.isDirectory()) {
                                    try {
                                        FileUtils.cleanDirectory(dir);
                                    } catch (IOException e) {
                                        Toast.makeText(register.this, "Error Message : Cant Clear FileData",Toast.LENGTH_LONG).show();
                                        e.printStackTrace();
                                    }
                                }
                                finish();
                            }else{
                                //If not successful user may already have registered
                                Toast.makeText(register.this, "Username or Phone number already registered", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Toast.makeText(register.this, "Error Message : "+error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }) {



            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //Adding the parameters to the request

                String uploadImage = getStringImage(bitmap);

                params.put(Config.KEY_NAME, name);
                params.put(Config.KEY_USERNAME, username);
                params.put(Config.KEY_PASSWORD, password);
                params.put(Config.KEY_PHONE, phone);
                params.put(Config.KEY_AADHAR, aadhar);
                params.put(Config.KEY_IMAGE, uploadImage);
                return params;
            }
        };

        //Adding request the the queue
        requestQueue.add(stringRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            filePath = data.getData();
            try {
                imageView2.setImageResource(android.R.color.transparent);
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView2.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 20, baos);

        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

}