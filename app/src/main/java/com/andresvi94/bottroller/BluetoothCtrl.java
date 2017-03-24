package com.andresvi94.bottroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothCtrl implements Parcelable {

    private Context mainContext;
    private Activity activity;

    private Set<BluetoothDevice> pairedDevices;
    public ArrayAdapter<String> btArrayAdapter;
    private ListView devicesListView;
    public BluetoothAdapter btAdapter;
    private Handler handler;
    public ConnectedThread connectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    //public BluetoothCtrl(Context context, ViewGroup layout, int stick_res_id)
    public BluetoothCtrl(Activity act, Context context, Handler mHandler)
    {
        activity = act;
        mainContext = context;
        handler = mHandler;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btArrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1);

        if (btAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(mainContext, "Bluetooth device not found! Cannot use app!", Toast.LENGTH_SHORT).show();
            //end activity/close app?
        }

//        handler = new Handler(){
//            public void handleMessage(android.os.Message msg) {
//                if (msg.what == CONNECTING_STATUS) {
//                    if (msg.arg1 == 1) {
//                        Toast.makeText(mainContext, "Connected", Toast.LENGTH_SHORT).show();
//                        connectedThread = new ConnectedThread(mBTSocket);
//                        connectedThread.start();
//                        //joystickStart();
//                    }
//                    joystickStart();
//                    /*else{
//                        Toast.makeText(mainContext, "Failed to Connect", Toast.LENGTH_SHORT).show();
//                    }*/
//                }
//            }
//        };
    }

    public BluetoothCtrl(Parcel in)
    {
        //BluetoothAdapter adapter = btAdapter;
        //BluetoothSocket socket = mBTSocket;
        ConnectedThread thread = connectedThread;
    }

    // 99.9% of the time you can just ignore this
    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeValue(connectedThread);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<BluetoothCtrl> CREATOR = new Parcelable.Creator<BluetoothCtrl>() {
        public BluetoothCtrl createFromParcel(Parcel in) {
            return new BluetoothCtrl(in);
        }

        public BluetoothCtrl[] newArray(int size) {
            return new BluetoothCtrl[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    /*private MyParcelable(Parcel in) {
        mData = in.readInt();
    }*/


    private void joystickStart()
    {
        Intent getJoyStickIntent = new Intent(activity, JoyStickActivity.class);
        final int result = 42; // result from 2nd activity
        //getJoyStickIntent.putExtra("callingActivity","MainActivity");
        //mainContext.startActivityForResult(getJoyStickIntent, result);

        //mainContext.startActivity(getJoyStickIntent); //works
        activity.startActivityForResult(getJoyStickIntent, result);
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                btArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void on(){
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void off(){
        if (btAdapter.isEnabled()){
            if (connectedThread != null)
                connectedThread.cancel();
            btAdapter.disable();
            Toast.makeText(mainContext,"Bluetooth turned off",Toast.LENGTH_SHORT).show();
        }
    }

    public void listPairedDevices(){
        pairedDevices = btAdapter.getBondedDevices();
        // put it's one to the adapter
        for (BluetoothDevice device : pairedDevices)
            btArrayAdapter.add(device.getName() + "\n" + device.getAddress());

        Toast.makeText(mainContext, "Show Paired Devices", Toast.LENGTH_SHORT).show();
        //return btArrayAdapter;
    }

    public AdapterView.OnItemClickListener deviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            connect(view);
            Toast.makeText(mainContext,"Connecting...",Toast.LENGTH_SHORT).show();
        }
    };

    public void connect(View v) {

        //bluetoothStatus.setText("Connecting...");
        // Get the device MAC address, which is the last 17 chars in the View
        String info = ((TextView) v).getText().toString();
        final String address = info.substring(info.length() - 17);
        final String name = info.substring(0, info.length() - 17);

        // Spawn a new thread to avoid blocking the GUI one
        new Thread() {
            public void run() {
                boolean fail = false;

                BluetoothDevice device = btAdapter.getRemoteDevice(address);

                try {
                    mBTSocket = createBluetoothSocket(device);
                } catch (IOException e) {
                    fail = true;
                    Toast.makeText(mainContext, "Socket creation failed", Toast.LENGTH_SHORT).show();
                }
                // Establish the Bluetooth socket connection.
                try {
                    mBTSocket.connect();
                } catch (IOException e) {
                    try {
                        fail = true;
                        mBTSocket.close();
                        //connectedThread.cancel();
                        handler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                .sendToTarget();
                    } catch (IOException e2) {
                        //insert code to deal with this
                        Toast.makeText(mainContext, "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                }
                if (fail == false) {
                    //connectedThread = new MainActivity.ConnectedThread(mBTSocket);
                    connectedThread = new ConnectedThread(mBTSocket);
                    connectedThread.start();

                    handler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                            .sendToTarget();
                }
            }
        }.start();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    public class ConnectedThread extends Thread {
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
            } catch (IOException e) { }

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
                    if(bytes != 0) {
                        SystemClock.sleep(100);
                        mmInStream.read(buffer);
                    }
                    // Send the obtained bytes to the UI activity

                    handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}