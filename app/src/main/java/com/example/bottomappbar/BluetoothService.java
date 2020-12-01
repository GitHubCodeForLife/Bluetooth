package com.example.bottomappbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import java.nio.charset.StandardCharsets;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class BluetoothService {
    Activity mainActivity;
    BluetoothAdapter bluetoothAdapter;
    private ServerThread serverThread;
    private ClientThread clientThread;
    CoordinatorLayout coordinatorLayoutWaiting;
    int turnMessage = 1;//1. I send + 2. You send
    TextView txtMsg;
    TextInputEditText inputMsg;
    Button btnSend;
    boolean isSend= false;
    boolean dialog = true;
    boolean firstTime = true;
    String msg;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(!dialog) {
                coordinatorLayoutWaiting.setVisibility(View.INVISIBLE);
                btnSend.setVisibility(View.VISIBLE);
                inputMsg.setVisibility(View.VISIBLE);
            }
            //Receive msg from Thread
            String value = new String((byte[]) msg.obj, StandardCharsets.UTF_8);
            //Token String
            //value: "Start*******" --> "Start"


            // Start
            // Emotion: "Emotion1"
            // Win lose
            // Cancel connect
            // Position
            // State game
            // Turn Game

            //Set text on screen
            if(!value.contains("Start")){
             if(turnMessage==1)
                 txtMsg.append("I: "+value+"\n");
            else if(turnMessage==2)
                txtMsg.append("You: "+value+"\n");
             }
        }
    }; // handler that gets info from Bluetooth service
    private static final String TAG = "MY_APP_DEBUG_TAG";
    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }

    public BluetoothService(Activity mainActivity, BluetoothAdapter bluetoothAdapter){
        this.mainActivity = mainActivity;
        this.bluetoothAdapter  = bluetoothAdapter;
        //Plumbing
        coordinatorLayoutWaiting = mainActivity.findViewById(R.id.cdntWaiting);
        txtMsg = mainActivity.findViewById(R.id.txtMsg);
        inputMsg = mainActivity.findViewById(R.id.inputMsg);
        btnSend = mainActivity.findViewById(R.id.btnSend);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSend = true;
                msg = Objects.requireNonNull(inputMsg.getText()).toString();
                inputMsg.clearFocus();
                inputMsg.setText("");
            }
        });
    }

    public void startServer(){
        if(serverThread!=null){
            serverThread.cancel();
            serverThread=null;
        }
        Toast.makeText(mainActivity, "Start server", Toast.LENGTH_SHORT).show();
        serverThread = new ServerThread();
        serverThread.start();
    }

    public void clientConnect(BluetoothDevice bluetoothDevice){
        if(clientThread!=null){
            clientThread.cancel();
            clientThread=null;
        }
        Toast.makeText(mainActivity, "Client connect", Toast.LENGTH_SHORT).show();
        clientThread = new ClientThread(bluetoothDevice);
        clientThread.start();
        //Show Dialog
    }

    class ServerThread extends Thread {
        private static final String TAG = "Sever";
        private final BluetoothServerSocket mmServerSocket;
        public ServerThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                String NAME = bluetoothAdapter.getName();
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, Bluetooth.MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.

            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's create() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    manageMyConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }


        private void manageMyConnectedSocket(BluetoothSocket socket) {
            dialog=false;
            while (true) {

                ConnectedThread connectedThread = new ConnectedThread(socket);
                connectedThread.start();

                if (isSend) {
                    ConnectedThread connectedThreadWrite = new ConnectedThread(socket);
                    connectedThreadWrite.write(msg.getBytes());
                    isSend = false;
                }
                try{
                    Thread.sleep(100);
                }catch (InterruptedException ignored){
                        break;
                }


            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
//                Toast.makeText(Bluetooth.this, "Could not close the connect socket", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Socket's sever failed", e);
            }
        }
    }

    class ClientThread extends Thread {

        private static final String TAG = "Client";
        private  BluetoothSocket mmSocket;
        private  BluetoothDevice mmDevice;
        public ClientThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(Bluetooth.MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            manageMyConnectedSocket(mmSocket);
        }

        private void manageMyConnectedSocket(BluetoothSocket mmSocket) {
            dialog=false;
//            if(firstTime) {
//                ConnectedThread send = new ConnectedThread(mmSocket);
////                String s = bluetoothAdapter.getName() + " connected with server " + mmDevice.getName();
////                String s="Start";
////                send.write(s.getBytes());
////                firstTime=false;
//            }
               ConnectedThread send = new ConnectedThread(mmSocket);
               String s="Start";
               send.write(s.getBytes());
             while (true) {

                 //Listen
                 ConnectedThread connectedThread = new ConnectedThread(mmSocket);
                 connectedThread.start();

                 //Send
                 if (isSend) {
                     ConnectedThread connectedThreadWrite = new ConnectedThread(mmSocket);
                     connectedThreadWrite.write(msg.getBytes());
                     isSend = false;
                 }
                 try{
                     Thread.sleep(100);
                 }catch (InterruptedException ignored){
                        break;
                 }
             }
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            // mmBuffer store for the stream
            byte[] mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = handler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    turnMessage = 2;
                    readMsg.sendToTarget();

                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                // Share the sent message with the UI activity.
                Message writtenMsg = handler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, bytes);
                turnMessage = 1;
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast", "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

}



