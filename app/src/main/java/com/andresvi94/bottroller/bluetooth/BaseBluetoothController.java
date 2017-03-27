package com.andresvi94.bottroller.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

abstract class BaseBluetoothController {
    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    private static final int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private static final int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status
    static final int MESSAGE_READ = 2; // used in bluetooth handler to identify message update

    private Activity activity;
    private Context context;

    BluetoothAdapter btAdapter;
    BluetoothSocket btSocket = null; // bi-directional client-to-client data path
    Handler handler;

    BaseBluetoothController(Activity activity, Context context, Handler handler) {
        this.activity = activity;
        this.context = context;
        this.handler = handler;

        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void connect(final String deviceMacAddress) {
        Thread backgroundThread = new Thread() {
            public void run() {
                btAdapter.cancelDiscovery();    //Ensure Adapter Discovery is Off (Documentation recommended)
                BluetoothDevice device = btAdapter.getRemoteDevice(deviceMacAddress);

                try {
                    btSocket = device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
                } catch (IOException e) {
                    showToast("Socket creation failed");
                }
                // Establish the Bluetooth socket connection.
                try {
                    btSocket.connect();
                } catch (IOException e) {
                    try {
                        btSocket.close();
                        //connectedThread.cancel();
                        if (handler != null)
                            handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                    } catch (IOException e2) {
                        // TODO: Insert code to deal with this
                        showToast("Socket creation failed");
                    }
                }

                if (btSocket.isConnected()) {
                    onBtConnected();
                }
            }
        };

        backgroundThread.start();
    }

    void onBtConnected() {
        if (handler != null)
            handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
    }

    public void turnOn() {
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public abstract void disconnect();

    void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public BluetoothAdapter getBtAdapter() {
        return btAdapter;
    }
}
