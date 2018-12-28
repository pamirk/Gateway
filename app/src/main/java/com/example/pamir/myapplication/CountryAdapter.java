package com.example.pamir.myapplication;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class CountryAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Country> mCountries;

    public CountryAdapter(Context context, ArrayList<Country> countries) {
        mContext = context;
        mCountries = countries;
    }

    @Override
    public int getCount() {
        return mCountries.size();
    }

    @Override
    public Object getItem(int position) {
        return mCountries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0; // we aren't going to use this. Tag items for easy reference
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            // brand new
            convertView = LayoutInflater.from(mContext).inflate(R.layout.country_list_item, null);
            holder = new ViewHolder();
            holder.iconImageView = convertView.findViewById(R.id.circleImageView);
            holder.countryLabel = convertView.findViewById(R.id.countryNameLabel);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Country country = mCountries.get(position);

        holder.iconImageView.setImageResource(Integer.parseInt(country.getFlagResId()));
        holder.countryLabel.setText(country.getName());

        if (position == 0) {
            //holder.countryLabel.setText("Pakistann");
        } else {
        }
        holder.countryLabel.setText(country.getName());

        return convertView;
    }

    void remove(Object item) {
        mCountries.remove(item);
    }

    private static class ViewHolder {
        ImageView iconImageView; // public by default
        TextView countryLabel;
    }
}












