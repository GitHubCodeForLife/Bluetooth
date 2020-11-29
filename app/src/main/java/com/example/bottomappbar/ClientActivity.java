package com.example.bottomappbar;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ClientActivity extends AppCompatActivity {
    BluetoothDevice bluetoothDevice;
    BluetoothAdapter bluetoothAdapter;
    TextView txtWaiting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_message);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        txtWaiting = findViewById(R.id.txtWaiting);

        Intent intent = getIntent();
        bluetoothDevice = intent.getParcelableExtra("bluetoothDevice");

        //Connect with server
        BluetoothService bluetoothService = new BluetoothService(this,bluetoothAdapter);
        bluetoothService.clientConnect(bluetoothDevice);

        //Set text
        txtWaiting.setText("Connecting with "+ bluetoothDevice.getName());


    }
}