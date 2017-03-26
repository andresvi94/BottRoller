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

import java.util.Set;

public class BluetoothSelector extends BaseBluetoothController {

    public ArrayAdapter<String> btArrayAdapter;
    public AdapterView.OnItemClickListener deviceClickListener;
    private final BroadcastReceiver broadcastReceiver;

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
                connect(view);
                showToast("Connecting...");
            }
        };
    }

    @Override
    public void turnOff() {
        if (btAdapter.isEnabled()) {
            btAdapter.disable();
            showToast("Bluetooth turned off");
        }
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