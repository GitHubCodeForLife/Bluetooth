package com.example.bottomappbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.appbar.MaterialToolbar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Bluetooth extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 101;
    private static final Object REQUEST_LOCATION =102 ;
    public static final UUID MY_UUID = new UUID(1,0);
    private static final String TAG ="Tag" ;
    private MaterialToolbar topAppBar;
    private Button discoverBluetooth;
    protected BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> btDeviceAvailableList = new ArrayList<>();
    private List<BluetoothDevice> btDevicePairedList = new ArrayList<>();
    private ListView listPairedDevices;
    private ListView listAvailableDevices;

    private TextView nameDevice;
    private CheckBox visible;

    private Button btnCreateServer;
    private  final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                Toast.makeText(context, "Start discover", Toast.LENGTH_SHORT).show();
            }

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                //Check already in
                if(!checkDuplicate(device, btDeviceAvailableList)){
                    btDeviceAvailableList.add(device);
                    setListView(listAvailableDevices, btDeviceAvailableList);
                }


            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
            Toast.makeText(context, "Device discovery ended", Toast.LENGTH_SHORT).show();
        }
    }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //Enable bluetooth
        enableBluetooth();

        //Plumbing
        listPairedDevices =findViewById(R.id.listPairedDevices);
        listAvailableDevices = findViewById(R.id.listAvailableDevices);
        topAppBar = findViewById(R.id.topAppBar);
        discoverBluetooth = findViewById(R.id.discoverBluetooth);
        nameDevice = findViewById(R.id.nameDevice);
        visible = findViewById(R.id.visible);
        btnCreateServer = findViewById(R.id.btnCreateServer);

        //Register broadcastReceiver to receive bluetooth_Found signal
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //Toast.makeText(this, "Discovering ...", Toast.LENGTH_SHORT).show();
        registerReceiver(receiver, filter);
        setListView(listAvailableDevices, btDeviceAvailableList);
        getPairedDevices();
        setListView(listPairedDevices,btDevicePairedList);

         //Take name my device
        nameDevice.setText("My device: "+bluetoothAdapter.getName());
        //Btn server click
        btnCreateServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Change activity
                Intent intent = new Intent(Bluetooth.this,ServerActivity.class );
                startActivity(intent);
            }
        });

        //Visible check box on click
        visible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    setVisibleBluetooth();
                }
            }
        });

        //Btn click
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        discoverBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPairedDevices();
                setListView(listPairedDevices,btDevicePairedList);
                bluetoothAdapter.startDiscovery();

            }
        });
        //Paired devices click

        listPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(btDevicePairedList.size()>0){
                    Toast.makeText(Bluetooth.this, "paired click"+position, Toast.LENGTH_SHORT).show();
                    showDialog(btDevicePairedList.get(position));
                }
            }
        });
        //Available devices click
        listAvailableDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(btDeviceAvailableList.size()>0) {
                    Toast.makeText(Bluetooth.this, "available click" + position, Toast.LENGTH_SHORT).show();
                    showDialog(btDeviceAvailableList.get(position));
                }
            }
        });

    }

    private void showDialog(final BluetoothDevice bluetoothDevice){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Quiz");
        builder.setMessage("Do you want connect with this device.");
        builder.setPositiveButton("OK!!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(Bluetooth.this, "Ok Clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Bluetooth.this,ClientActivity.class );
                //Send intent with Bundle: This Bluetooth device + Target device
                intent.putExtra("bluetoothDevice",bluetoothDevice);
                startActivity(intent);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(Bluetooth.this, "Cancel  clicked", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();

    }

    private void enableBluetooth() {
        if(bluetoothAdapter==null){
            Toast.makeText(this, "This device don't support bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!bluetoothAdapter.isEnabled()) {
             //bluetoothAdapter.enable();
             Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
             startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            //Enable Location

        }
    }

    private void getPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        btDevicePairedList.clear();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                btDevicePairedList.add(device);
            }
        }
    }

    private boolean checkDuplicate(BluetoothDevice b,List<BluetoothDevice> list){
        int size = list.size();
        boolean check=false;
        for(int i=0;i<size;i++){
            if(b.getAddress().equals(list.get(i).getAddress())){
                check=true;
            }
        }
        return check;
    }

    private void setVisibleBluetooth(){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,120);
        startActivity(discoverableIntent);
    }

    private void setListView(ListView lv, List<BluetoothDevice> list) {

        int size = list.size();
//        Toast.makeText(this, "size: "+size, Toast.LENGTH_SHORT).show();
        String[] nameDevice = new  String[size];
        String[] addressDevice = new String[size];
        int[] imageDevice  = new int[size];
        for(int i=0;i<size;i++){
            nameDevice[i]= list.get(i).getName();
            addressDevice[i] = list.get(i).getAddress();
            imageDevice[i] = R.drawable.ic_baseline_phone_iphone_24;
        }
        if(size==0){
            size = 1;
            nameDevice = new  String[size];
            addressDevice = new String[size];
            imageDevice  = new int[size];
            nameDevice[0]="No devices found";
            addressDevice[0]="XX-XX-XX-XX";
            imageDevice[0]=R.drawable.ic_baseline_error_24;
        }

//        Toast.makeText(this, "Name devices: "+ nameDevice[size-1], Toast.LENGTH_SHORT).show();

        BTAdapter btAdapter = new BTAdapter(Bluetooth.this,R.layout.bluetooth_custom_view, nameDevice, addressDevice, imageDevice);
        lv.setAdapter(btAdapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

}