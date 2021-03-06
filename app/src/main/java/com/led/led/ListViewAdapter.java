package com.led.led;

/**
 * Created by Maggi on 02.06.2016.
 * 2 Column ListView Adapter for BlueTooth Devices
 */

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class ListViewAdapter extends BaseAdapter{
    private final ArrayList<ArrayList<Object>> list;
    private final Activity activity;
    private TextView txtFirst;
    private TextView txtSecond;
    public ListViewAdapter(Activity activity,ArrayList<ArrayList<Object>> list){
        super();
        this.activity=activity;
        this.list=list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        LayoutInflater inflater=activity.getLayoutInflater();

        if(convertView == null){

            convertView=inflater.inflate(R.layout.rowlayout, null);

            txtFirst=(TextView) convertView.findViewById(R.id.Description);
            txtSecond=(TextView) convertView.findViewById(R.id.BTDevice);
        }

        ArrayList<Object> row=list.get(position);
        txtFirst.setText(row.get(0).toString());
        txtSecond.setText(row.get(1).toString());

        return convertView;
    }

}