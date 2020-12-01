package com.example.bottomappbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ButtonBarLayout;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ClientActivity extends AppCompatActivity {
    BluetoothDevice bluetoothDevice;
    BluetoothAdapter bluetoothAdapter;
    TextView txtWaiting;
    TextInputEditText inputMsg;
    Button btnSend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_message);

        //Plumbing
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        txtWaiting = findViewById(R.id.txtWaiting);
        inputMsg = findViewById(R.id.inputMsg);
        btnSend = findViewById(R.id.btnSend);

        //Hide button and input field when you don't connect
        inputMsg.setVisibility(View.INVISIBLE);
        btnSend.setVisibility(View.INVISIBLE);


        Intent intent = getIntent();
        bluetoothDevice = intent.getParcelableExtra("bluetoothDevice");

        //Connect with server
        BluetoothService bluetoothService = new BluetoothService(this,bluetoothAdapter);
        bluetoothService.clientConnect(bluetoothDevice);

        //Set text
        txtWaiting.setText("Connecting with "+ bluetoothDevice.getName());

        //Top App Bar event Click


    }
}