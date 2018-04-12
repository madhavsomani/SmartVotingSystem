package com.hnt.smartvotingsystem.smartvotingsystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;


/**
 * Created by Oclemy on 6/5/2016 for ProgrammingWizards Channel and http://www.camposha.com.
 */
public class DataParser extends AsyncTask<Void,Void,Integer> {
    Context c;
    String jsonData;
    ListView lv;
    ProgressDialog pd;
    ArrayList<dataobject> dataobject=new ArrayList<>();
    public DataParser(Context c, String jsonData, ListView lv) {
        this.c = c;
        this.jsonData = jsonData;
        this.lv = lv;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd=new ProgressDialog(c);
        pd.setTitle("Parse");
        pd.setMessage("Parsing...Please wait");
        pd.show();
    }
    @Override
    protected Integer doInBackground(Void... params) {
        return this.parseData();
    }
    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        pd.dismiss();
        if(result==0)
        {
            Toast.makeText(c,"Unable To Parse",Toast.LENGTH_SHORT).show();
        }else {
            //BIND DATA TO LISTVIEW
            CustomAdapter adapter=new CustomAdapter(c,dataobject);

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            Gson gson = new Gson();

            String json = gson.toJson(dataobject);

            editor.putString("arraylist", json);
            editor.commit();

            lv.setAdapter(adapter);

        }
    }
    private int parseData()
    {
        try
        {
            JSONArray ja=new JSONArray(jsonData);
            JSONObject jo=null;
            dataobject.clear();
            dataobject obj;
            for(int i=0;i<ja.length();i++)
            {
                jo=ja.getJSONObject(i);
                int id=jo.getInt("id");
                String name=jo.getString("partyname");
                String imageUrl=jo.getString("partyimagelocation");
                String discription=jo.getString("partydiscription");
                obj=new dataobject();
                obj.setId(id);
                obj.setName(name);
                obj.setImageUrl(imageUrl);
                obj.setDiscription(discription);
                dataobject.add(obj);
            }
            return 1;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
