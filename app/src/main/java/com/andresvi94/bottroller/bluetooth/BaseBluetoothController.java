package com.andresvi94.bottroller.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

abstract class BaseBluetoothController {
    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    private static final int MAC_ADDRESS_LENGTH = 17;
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

    void connect(View view) {
        // Get the device MAC address, which is the last 17 chars in the View
        String info = ((TextView) view).getText().toString();
        final String address = info.substring(info.length() - MAC_ADDRESS_LENGTH);
        final String name = info.substring(0, info.length() - MAC_ADDRESS_LENGTH);

        Thread backgroundThread = new Thread() {
            public void run() {
                BluetoothDevice device = btAdapter.getRemoteDevice(address);

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
                        handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                    } catch (IOException e2) {
                        // TODO: Insert code to deal with this
                        showToast("Socket creation failed");
                    }
                }

                if (btSocket.isConnected()) {
                    onBtConnected(name);
                }
            }
        };

        backgroundThread.start();
    }

    void onBtConnected(String name) {
        handler.obtainMessage(CONNECTING_STATUS, 1, -1, name).sendToTarget();
    }

    public void turnOn() {
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public abstract void turnOff();

    void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public BluetoothAdapter getBtAdapter() {
        return btAdapter;
    }
}
