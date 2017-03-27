package com.andresvi94.bottroller.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.Set;

public class BluetoothSelector extends BaseBluetoothController {

    private static final int MAC_ADDRESS_LENGTH = 17;

    public ArrayAdapter<String> btArrayAdapter;
    public AdapterView.OnItemClickListener deviceClickListener;
    private final BroadcastReceiver broadcastReceiver;
    private String deviceMacAddress = "";

    public BluetoothSelector(Activity activity, final Context context, Handler handler) {
        super(activity, context, handler);

        btArrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1);

        if (btAdapter == null) {
            // Device does not support Bluetooth
            showToast("Your device does not support Bluetooth! Closing the app");
            activity.finish();
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    btArrayAdapter.notifyDataSetChanged();
                }
            }
        };

        deviceClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String deviceInfo = ((TextView) view).getText().toString();
                deviceMacAddress = deviceInfo.substring(deviceInfo.length() - MAC_ADDRESS_LENGTH);
                connect(deviceMacAddress);
                showToast("Connecting...");
            }
        };
    }

    @Override
    public void disconnect() {
        try {
            btSocket.close();
        } catch (IOException e) {
            showToast("Failed to close Bluetooth socket");
        }
    }

    public String getDeviceMacAddress() {
        return deviceMacAddress;
    }

    public void listPairedDevices() {
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices)
            btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        showToast("Showing paired devices");
    }

    public boolean isBtAdapterOn() {
        return btAdapter.isEnabled();
    }

    public final BroadcastReceiver getBroadcastReceiver() {
        return broadcastReceiver;
    }
}