package com.hnt.smartvotingsystem.smartvotingsystem;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.hnt.smartvotingsystem.smartvotingsystem.R.id.partdiscription;


/**
 * Created by madhav on 2/19/2017.
 */


public class partydiscription extends AppCompatActivity {


    ArrayList<dataobject> object;
    dataobject spacecraft;
    private RequestQueue requestQueue;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.partydiscription);


        int position= getIntent().getIntExtra("position",1);

        //Initializing the RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = sharedPrefs.getString("arraylist", null);
        Type type = new TypeToken<ArrayList<dataobject>>() {}.getType();
        object = gson.fromJson(json, type);

        TextView partname= (TextView) findViewById(R.id.partname);
        final TextView partdiscriptionr= (TextView) findViewById(partdiscription);
        ImageView img= (ImageView) findViewById(R.id.partyimage);
        final Button vote = (Button)findViewById(R.id.voteButton);

         spacecraft=object.get(position);

        partname.setText(spacecraft.getName());
        partdiscriptionr.setText(spacecraft.getDiscription());

        PicassoClient.downloadImage(getApplicationContext(),spacecraft.getImageUrl(),img);



        vote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(partydiscription.this)
                        .setTitle("Confirmation Box")
                        .setMessage("Do you really want to Vote?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                postvote();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();

            }
        });
    }

    //this method will register the user
    private void postvote() {

        //Displaying a progress dialog
        final ProgressDialog loading = ProgressDialog.show(this, "Voting", "Please wait...", false, false);

        //Again creating the string request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.VOTING_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        try {
                            //Creating the json object from the response
                            JSONObject jsonResponse = new JSONObject(response);

                            //If it is success
                            if(jsonResponse.getString(Config.TAG_RESPONSE).equalsIgnoreCase("voted"))
                            {
                                //Asking user to confirm otp
                                Toast.makeText(partydiscription.this, "Error : User Already Voted!", Toast.LENGTH_LONG).show();
                                finish();

                            }
                            if(jsonResponse.getString(Config.TAG_RESPONSE).equalsIgnoreCase("Success"))
                            {
                                //Asking user to confirm otp
                                Toast.makeText(partydiscription.this, "Successfully VOTED", Toast.LENGTH_LONG).show();
                                finish();

                            }if(jsonResponse.getString(Config.TAG_RESPONSE).equalsIgnoreCase("Failure"))
                            {
                                //If not successful user may already have registered
                                Toast.makeText(partydiscription.this, "Error in Voting", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(partydiscription.this, "Error Message : "+error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }) {



            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //Adding the parameters to the request

                SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String usernamekey = saved_values.getString("usernamekey", "null");


                params.put("username", usernamekey);
                params.put("votepartyname", spacecraft.getName());
                return params;
            }
        };

        //Adding request the the queue
        requestQueue.add(stringRequest);
    }
}
