package com.example.bottomappbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BTAdapter extends ArrayAdapter<String> {
    Context context;
    String[] nameArray;
    String[] addressArray;
    int[] imageArray;

    public BTAdapter(@NonNull Context context,int layoutToBeInflated, String[] nameArray, String[] addressArray, int[] imageArray) {
        super(context, R.layout.bluetooth_custom_view,nameArray);
        this.context = context;
        this.imageArray = imageArray;
        this.nameArray = nameArray;
        this.addressArray = addressArray;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        View row = inflater.inflate(R.layout.bluetooth_custom_view, null);

        //Get the reference to the view objects
        ImageView myImage = row.findViewById(R.id.ImgView);
        TextView myNameDevice = row.findViewById(R.id.nameDevice);
        TextView myAddressDevice = row.findViewById(R.id.addressDevice);

        //providing the element of an array by specifying its position
        myImage.setImageResource(imageArray[position]);
        myNameDevice.setText(nameArray[position]);
        myAddressDevice.setText(addressArray[position]);
        return  row;
    }
}