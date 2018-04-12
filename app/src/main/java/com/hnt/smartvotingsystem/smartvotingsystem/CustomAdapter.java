package com.hnt.smartvotingsystem.smartvotingsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
/**
 * Created by Oclemy on 6/5/2016 for ProgrammingWizards Channel and http://www.camposha.com.
 */
public class CustomAdapter extends BaseAdapter {
    Context c;
    ArrayList<dataobject> dataobject;
    LayoutInflater inflater;
    public CustomAdapter(Context c, ArrayList<dataobject> dataobject) {
        this.c = c;
        this.dataobject = dataobject;
        inflater= (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return dataobject.size();
    }
    @Override
    public Object getItem(int position) {
        return dataobject.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null)
        {
            convertView=inflater.inflate(R.layout.listview,parent,false);
        }
        TextView nametxt= (TextView) convertView.findViewById(R.id.nameTxt);
        ImageView img= (ImageView) convertView.findViewById(R.id.movieImage);
        //BIND DATA
        dataobject spacecraft=dataobject.get(position);
        nametxt.setText(spacecraft.getName());
        //IMG
        PicassoClient.downloadImage(c,spacecraft.getImageUrl(),img);
        return convertView;
    }
}