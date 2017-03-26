package com.andresvi94.bottroller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.andresvi94.bottroller.bluetooth.BluetoothSelector;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    // GUI Components
    @BindView(R.id.cmd) EditText textCmd;
    @BindView(R.id.PairedBtn) Button listPairedDevicesBtn;

    private String dataSentBack; // TEST FOR DIRECTION FROM JOYSTICK
    private AlertDialog dialog;

    private static Handler mHandler; // Our main handler that will receive callback notifications
    //private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    BluetoothSelector btCtrl = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ButterKnife.bind(this);

        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1) {
                        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                        // TODO: Un-comment when device is available
                        //joystickStart();
                    }
                    joystickStart();
                    /*else{
                        Toast.makeText(mainContext, "Failed to Connect", Toast.LENGTH_SHORT).show();
                    }*/
                }
            }
        };
        btCtrl = new BluetoothSelector(this, getApplicationContext(), mHandler);
        btCtrl.turnOn();
    }

    private void joystickStart() {
        Intent getJoyStickIntent = new Intent(MainActivity.this, JoyStickActivity.class);
        final int result = 42; // result from 2nd activity
        //getJoyStickIntent.putExtra("callingActivity","MainActivity");
        //mainContext.startActivityForResult(getJoyStickIntent, result);
        startActivity(getJoyStickIntent);
        //mainContext.startActivity(getJoyStickIntent); //works
//        startActivityForResult(getJoyStickIntent, result);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                checkBluetoothStatus(resultCode);
                break;
//            case 42:
//                dataSentBack = Data.getStringExtra("Test");
//                //btCtrl.testString = dataSentBack;
//                textCmd.setText(dataSentBack);
//                break;
        }
    }

    private void checkBluetoothStatus(int resultCode) {
        // Make sure the request was successful
        if (resultCode == RESULT_OK) {
            // The user picked a contact.
            // The Intent's data Uri identifies which contact was selected.
            Toast.makeText(getBaseContext(), "Bluetooth turned turnOn", Toast.LENGTH_SHORT).show();
        } else {
            //int pid = android.os.Process.myPid();
            //android.os.Process.killProcess(pid);
            Toast.makeText(getBaseContext(), "Bluetooth not turned turnOn", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.PairedBtn)
    public void onPairedDevicesBtnClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_device, null);
        Button pairedBtn = (Button) dialogView.findViewById(R.id.pairedBtn);
        Button discoverBtn = (Button) dialogView.findViewById(R.id.discoverBtn);
        final ListView devicesListView = (ListView) dialogView.findViewById(R.id.devicesListView);

        pairedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btCtrl.btArrayAdapter.clear();
                btCtrl.listPairedDevices();
                devicesListView.setAdapter(btCtrl.btArrayAdapter);
            }
        });

        discoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btCtrl.getBtAdapter().isDiscovering()) {
                    btCtrl.getBtAdapter().cancelDiscovery();
                }
                //Get Location Permission
                int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

                btCtrl.btArrayAdapter.clear();
                btCtrl.getBtAdapter().startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started...", Toast.LENGTH_SHORT).show();
                registerReceiver(btCtrl.getBroadcastReceiver(), new IntentFilter(BluetoothDevice.ACTION_FOUND));
                devicesListView.setAdapter(btCtrl.btArrayAdapter);
            }
        });

        devicesListView.setOnItemClickListener(btCtrl.deviceClickListener);

        builder.setView(dialogView);
        dialog = builder.create();
        dialog.show();
    }
}