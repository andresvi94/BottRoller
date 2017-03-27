package com.andresvi94.bottroller;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andresvi94.bottroller.bluetooth.BluetoothCommunicator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;

public class JoyStickActivity extends AppCompatActivity {

    @BindView(R.id.layout_joystick) RelativeLayout layout_joystick;
    TextView textView1, textView2, textView3, textView4, textView5;
    JoyStick joyStick;
    //private int direction;
    private BluetoothCommunicator bluetoothCommunicator;
    private BluetoothCommunicator.ConnectedThread thread;

    @OnTouch(R.id.layout_joystick)
    public boolean onJoystickTouch(MotionEvent motionEvent) {
        joyStick.drawStick(motionEvent);
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN ||
                motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            textView1.setText("X : " + String.valueOf(joyStick.getX()));
            textView2.setText("Y : " + String.valueOf(joyStick.getY()));
            textView3.setText("Angle : " + String.valueOf(joyStick.getAngle()));
            textView4.setText("Distance : " + String.valueOf(joyStick.getDistance()));

            int direction = joyStick.get8Direction();
            thread.write(String.valueOf(direction));

            if (direction == JoyStick.STICK_UP) {
                textView5.setText(R.string.up);
            } else if (direction == JoyStick.STICK_UP_RIGHT) {
                textView5.setText(R.string.up_right);
            } else if (direction == JoyStick.STICK_RIGHT) {
                textView5.setText(R.string.right);
            } else if (direction == JoyStick.STICK_DOWN_RIGHT) {
                textView5.setText(R.string.down_right);
            } else if (direction == JoyStick.STICK_DOWN) {
                textView5.setText(R.string.down);
            } else if (direction == JoyStick.STICK_DOWN_LEFT) {
                textView5.setText(R.string.down_left);
            } else if (direction == JoyStick.STICK_LEFT) {
                textView5.setText(R.string.left);
            } else if (direction == JoyStick.STICK_UP_LEFT) {
                textView5.setText(R.string.up_left);
            } else if (direction == JoyStick.STICK_NONE) {
                textView5.setText(R.string.center);
            }
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            textView1.setText(R.string.x);
            textView2.setText(R.string.y);
            textView3.setText(R.string.angle);
            textView4.setText(R.string.distance);
            textView5.setText(R.string.direction);
        }
        return true;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joy_stick);
        ButterKnife.bind(this);

        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        textView4 = (TextView) findViewById(R.id.textView4);
        textView5 = (TextView) findViewById(R.id.textView5);

        joyStick = new JoyStick(getApplicationContext(), layout_joystick);
        joyStick.setStickSize(150, 150);
        joyStick.setLayoutSize(500, 500);
        joyStick.setLayoutAlpha(150);
        joyStick.setStickAlpha(100);
        joyStick.setOffset(90);
        joyStick.setMinimumDistance(50);

        bluetoothCommunicator = new BluetoothCommunicator(this, getApplicationContext(), null);
        bluetoothCommunicator.turnOn();
        bluetoothCommunicator.connect(getIntent().getStringExtra("MAC_ADDRESS"));
        SystemClock.sleep(1000);
        thread = bluetoothCommunicator.getConnectedThread();
    }

    @Override
    protected void onDestroy() {
        // Toast.makeText(getBaseContext(),"destroy",Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }
}
