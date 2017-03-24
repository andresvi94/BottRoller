package com.andresvi94.bottroller;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    // GUI Components
    @BindView(R.id.cmd) EditText textCmd;
    @BindView(R.id.PairedBtn) Button listPairedDevicesBtn;

    private String dataSentBack; // TEST FOR DIRECTION FROM JOYSTICK
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;
    private AlertDialog dialog;

    private Handler mHandler; // Our main handler that will receive callback notifications
    //private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier


    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    BluetoothCtrl btCtrl = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ButterKnife.bind(this);

        mHandler = new Handler(){
            public void handleMessage(android.os.Message msg) {
                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1) {
                        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                        //joystickStart();
                    }
                    joystickStart();
                    /*else{
                        Toast.makeText(mainContext, "Failed to Connect", Toast.LENGTH_SHORT).show();
                    }*/
                }
            }
        };
        btCtrl = new BluetoothCtrl(this, getApplicationContext(), mHandler);
        btCtrl.on();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUp();
    }

    private void joystickStart()
    {
        Intent getJoyStickIntent = new Intent(this, JoyStickActivity.class);
        final int result = 42; // result from 2nd activity
        //getJoyStickIntent.putExtra("callingActivity","MainActivity");
        //mainContext.startActivityForResult(getJoyStickIntent, result);
        getJoyStickIntent.putExtra("test",btCtrl);
        //mainContext.startActivity(getJoyStickIntent); //works
        startActivityForResult(getJoyStickIntent, result);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data){
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                checkBluetoothStatus(resultCode);
                break;
            case 42:
                dataSentBack = Data.getStringExtra("Test");
                //btCtrl.testString = dataSentBack;
                textCmd.setText(dataSentBack);
                break;
        }
    }

    private void checkBluetoothStatus(int resultCode){
        // Make sure the request was successful
        if (resultCode == RESULT_OK) {
            // The user picked a contact.
            // The Intent's data Uri identifies which contact was selected.
            Toast.makeText(getBaseContext(),"Bluetooth turned on",Toast.LENGTH_SHORT).show();
        }
        else {
            //int pid = android.os.Process.myPid();
            //android.os.Process.killProcess(pid);
            Toast.makeText(getBaseContext(), "Bluetooth not turned on", Toast.LENGTH_SHORT).show();
        }
    }

    private void setUp()
    {

        listPairedDevicesBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                dialogBuilder();
            }
        });

    }

    private void dialogBuilder (){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_device, null);
        Button pairedBtn = (Button) dialogView.findViewById(R.id.pairedBtn);
        Button discoverBtn = (Button) dialogView.findViewById(R.id.discoverBtn);
        final ListView devicesListView = (ListView) dialogView.findViewById(R.id.devicesListView);

        pairedBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                btCtrl.btArrayAdapter.clear();
                btCtrl.listPairedDevices();
                devicesListView.setAdapter(btCtrl.btArrayAdapter);
            }
        });

        discoverBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (btCtrl.btAdapter.isDiscovering()) {
                    btCtrl.btAdapter.cancelDiscovery();
                }
                //Get Location Permission
                int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

                btCtrl.btArrayAdapter.clear();
                btCtrl.btAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(),"Discovery started...",Toast.LENGTH_SHORT).show();
                registerReceiver(btCtrl.blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
                devicesListView.setAdapter(btCtrl.btArrayAdapter);
            }
        });

        devicesListView.setOnItemClickListener(btCtrl.deviceClickListener);

        builder.setView(dialogView);
        dialog = builder.create();
        dialog.show();
    }

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        btCtrl = new BluetoothCtrl(this, getApplicationContext());
        mBTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

        mDevicesListView = (ListView) findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        //turns off keyboard with EditText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    readBuffer.setText(readMessage);
                }

                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1)
                        bluetoothStatus.setText("Connected to Device: " + (String) (msg.obj));
                    else
                        bluetoothStatus.setText("Connection Failed");
                }
            }
        };

        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            bluetoothStatus.setText("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(), "Bluetooth device not found!", Toast.LENGTH_SHORT).show();
        } else {

            if (mBTAdapter.isEnabled()) {
                toggleButton.setChecked(true);
            }

            checkBoxLED.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mConnectedThread != null)    //First check to make sure thread created
                    {
                        String cmd = textCmd.getText().toString();
                        mConnectedThread.write(cmd);
                    }

                }
            });

            textCmd.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //textCmd.setText(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        mmInStream.read(buffer);
                    }
                    // Send the obtained bytes to the UI activity

                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        *//* Call this from the main activity to send data to the remote device *//*
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        *//* Call this from the main activity to shutdown the connection *//*
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }*/
}