package com.andresvi94.bottroller;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.andresvi94.bottroller.bluetooth.BluetoothSelector;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private final static int BT_CONNECTION_SUCCESSFUL = 1;
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    private BluetoothSelector bluetoothSelector;

    // GUI Components
    @BindView(R.id.button_start) Button buttonStart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ButterKnife.bind(this);

        Handler handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == BT_CONNECTION_SUCCESSFUL) {
                        showToast("Connected");
                        // TODO: Un-comment when device is available
                        startJoystickActivity();
                    }
                    else
                        showToast("Failed to connect");
//                    startJoystickActivity();
                }
            }
        };

        bluetoothSelector = new BluetoothSelector(this, getApplicationContext(), handler);
        bluetoothSelector.turnOn();
    }

    private void startJoystickActivity() {
        SystemClock.sleep(500);
        bluetoothSelector.disconnect();
        Intent getJoyStickIntent = new Intent(MainActivity.this, JoyStickActivity.class);
        getJoyStickIntent.putExtra("MAC_ADDRESS", bluetoothSelector.getDeviceMacAddress());
        startActivity(getJoyStickIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        if (requestCode == REQUEST_ENABLE_BT)
            checkBluetoothStatus(resultCode);
    }

    private void checkBluetoothStatus(int resultCode) {
        // Make sure the request was successful
        if (resultCode == RESULT_OK) {
            // The user picked a contact.
            // The Intent's data Uri identifies which contact was selected.
            showToast("Bluetooth turned on");
        } else {
            showToast("Bluetooth failed to turn on");
        }
    }

    @OnClick(R.id.button_start)
    public void onStartButtonClick() {
        if (!bluetoothSelector.isBtAdapterOn())
            showToast("Enable Bluetooth to continue");
        else
            showBtSelectionDialog();
    }

    private void showBtSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_device, null);

        Button buttonShowPaired = (Button) dialogView.findViewById(R.id.button_show_paired);
        Button buttonDiscover = (Button) dialogView.findViewById(R.id.button_discover);
        final ListView listViewBtDevices = (ListView) dialogView.findViewById(R.id.list_devices);

        buttonShowPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothSelector.btArrayAdapter.clear();
                bluetoothSelector.listPairedDevices();
                listViewBtDevices.setAdapter(bluetoothSelector.btArrayAdapter);
            }
        });

        buttonDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothSelector.getBtAdapter().isDiscovering()) {
                    bluetoothSelector.getBtAdapter().cancelDiscovery();
                }
                // Get Location permission
                int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

                bluetoothSelector.btArrayAdapter.clear();
                bluetoothSelector.getBtAdapter().startDiscovery();
                showToast("Discovery started...");
                registerReceiver(bluetoothSelector.getBroadcastReceiver(), new IntentFilter(BluetoothDevice.ACTION_FOUND));
                listViewBtDevices.setAdapter(bluetoothSelector.btArrayAdapter);
            }
        });

        listViewBtDevices.setOnItemClickListener(bluetoothSelector.deviceClickListener);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}