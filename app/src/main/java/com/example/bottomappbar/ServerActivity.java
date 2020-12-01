package com.example.bottomappbar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import org.w3c.dom.Text;

public class ServerActivity extends Activity {
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

        //Start Service Bluetooth server
        BluetoothService bluetoothService = new BluetoothService(this,bluetoothAdapter);
        bluetoothService.startServer();

        txtWaiting.setText(bluetoothAdapter.getName()+" Server is waiting connect.");

    }
}
