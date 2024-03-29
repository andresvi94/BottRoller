package com.andresvi94.bottroller.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.SystemClock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothCommunicator extends BaseBluetoothController
{
    private ConnectedThread connectedThread;

    public BluetoothCommunicator(Activity activity, Context context)
    {
        super(activity, context, null);
    }

    @Override
    void onBtConnected()
    {
        super.onBtConnected();
        connectedThread = new ConnectedThread(btSocket);
        connectedThread.start();
    }

    public ConnectedThread getConnectedThread()
    {
        return connectedThread;
    }

    @Override
    public void disconnect()
    {
        if (btAdapter.isEnabled() && connectedThread != null)
            connectedThread.cancel();
    }

    public class ConnectedThread extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        ConnectedThread(BluetoothSocket socket)
        {
            this.bluetoothSocket = socket;
            InputStream tempInputStream = null;
            OutputStream tempOutputStream = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try
            {
                tempInputStream = socket.getInputStream();
                tempOutputStream = socket.getOutputStream();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            inputStream = tempInputStream;
            outputStream = tempOutputStream;
        }

        public void run()
        {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true)
            {
                try
                {
                    // Read from the InputStream
                    bytes = inputStream.read(buffer);
                    if (bytes != 0)
                    {
                        SystemClock.sleep(100);
                        //noinspection ResultOfMethodCallIgnored
                        inputStream.read(buffer);
                    }
                    // Send the obtained bytes to the UI activity
                    handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                }
                catch (IOException e)
                {
                    break;
                }
            }
        }

        // Call this from the activity to send data to the remote device
        public void write(String input)
        {
            byte[] bytes = input.getBytes();
            try
            {
                outputStream.write(bytes);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                showToast("Failed writing to Bluetooth device");
            }
        }

        // Call this from the activity to close the connection
        public void cancel()
        {
            try
            {
                bluetoothSocket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                showToast("Failed to close the Bluetooth connection");
            }
        }
    }
}
