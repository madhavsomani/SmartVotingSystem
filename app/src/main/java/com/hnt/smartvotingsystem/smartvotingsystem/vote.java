package com.hnt.smartvotingsystem.smartvotingsystem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by madhav on 2/9/2017.
 */

public class vote  extends AppCompatActivity {


    final static String urlAddress="http://hntdatabase.16mb.com/partydiscription.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vote);

        final ListView lv= (ListView) findViewById(R.id.votelsitview);

        String path = Environment.getExternalStorageDirectory()
                + "//FingerData";
        File dir = new File(path);
        if(dir.exists() && dir.isDirectory()) {
            try {
                FileUtils.cleanDirectory(dir);
            } catch (IOException e) {
                Toast.makeText(vote.this, "Error Message : Cant Clear FileData",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        new Downloader(vote.this,urlAddress,lv).execute();


       lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


               Intent i = new Intent(vote.this, partydiscription.class);
               i.putExtra("position", position);
               startActivity(i);

           }
       });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.votemenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.miProfile) {

            startActivity(new Intent(vote.this,profile.class));
            return true;
        }

        if (id == R.id.miCompose) {

            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
