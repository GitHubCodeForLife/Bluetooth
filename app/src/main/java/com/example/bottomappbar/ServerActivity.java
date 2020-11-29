package com.example.bottomappbar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ServerActivity extends Activity {
    BluetoothAdapter bluetoothAdapter;
    TextView txtWaiting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_message);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        txtWaiting = findViewById(R.id.txtWaiting);

        //Start Service Bluetooth server
        BluetoothService bluetoothService = new BluetoothService(this,bluetoothAdapter);
        bluetoothService.startServer();

        txtWaiting.setText(bluetoothAdapter.getName()+" Server is waiting connect.");

    }
}
