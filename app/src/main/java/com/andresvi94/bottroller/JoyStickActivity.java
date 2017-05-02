package com.andresvi94.bottroller;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andresvi94.bottroller.bluetooth.BluetoothCommunicator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

public class JoyStickActivity extends AppCompatActivity
{
    private static final int SLEEP_DURATION = 850;
    private static final String INCREASE = "I";
    private static final String DECREASE = "D";

    private JoyStick joyStick;
    private boolean fast = false;
    private boolean tap = false;
    private BluetoothCommunicator.ConnectedThread thread;

    @BindView(R.id.layout_joystick) RelativeLayout layoutJoystick;
    @BindView(R.id.button_fast) Button buttonFast;
    @BindView(R.id.textView5) TextView textView5;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joy_stick);
        ButterKnife.bind(this);

        configureJoyStick();
        setUpBtConnection(getIntent().getStringExtra("MAC_ADDRESS"));
    }

    private void configureJoyStick()
    {
        joyStick = new JoyStick(getApplicationContext(), layoutJoystick);
        joyStick.setStickSize(150, 150);
        joyStick.setLayoutSize(500, 500);
        joyStick.setLayoutAlpha(150);
        joyStick.setStickAlpha(100);
        joyStick.setOffset(90);
        joyStick.setMinimumDistance(50);
    }

    private void setUpBtConnection(String macAddress)
    {
        BluetoothCommunicator bluetoothCommunicator = new BluetoothCommunicator(this, getApplicationContext());
        bluetoothCommunicator.turnOn();
        bluetoothCommunicator.connect(macAddress);
        SystemClock.sleep(SLEEP_DURATION);
        thread = bluetoothCommunicator.getConnectedThread();
        thread.write(String.valueOf(JoyStick.STICK_NONE));
    }

    @OnClick(R.id.button_fast)
    public void onFastButtonClick()
    {
        if(!fast) {
            buttonFast.setText(R.string.fast);
            thread.write(INCREASE);
            fast = true;
        }
        else{
            buttonFast.setText(R.string.normal);
            thread.write(DECREASE);
            fast = false;
        }
    }


    @OnTouch(R.id.layout_joystick)
    public boolean onJoystickTouch(MotionEvent motionEvent)
    {

        if ((motionEvent.getAction() == MotionEvent.ACTION_DOWN ||
                motionEvent.getAction() == MotionEvent.ACTION_MOVE && !tap))
        {
            joyStick.drawStick(motionEvent);
            tap = true;

            int direction = joyStick.get8Direction();
            thread.write(String.valueOf(direction));

            if (direction == JoyStick.STICK_UP)
                textView5.setText(R.string.up);
            else if (direction == JoyStick.STICK_UP_RIGHT)
                textView5.setText(R.string.up_right);
            else if (direction == JoyStick.STICK_RIGHT)
                textView5.setText(R.string.right);
            else if (direction == JoyStick.STICK_DOWN_RIGHT)
                textView5.setText(R.string.down_right);
            else if (direction == JoyStick.STICK_DOWN)
                textView5.setText(R.string.down);
            else if (direction == JoyStick.STICK_DOWN_LEFT)
                textView5.setText(R.string.down_left);
            else if (direction == JoyStick.STICK_LEFT)
                textView5.setText(R.string.left);
            else if (direction == JoyStick.STICK_UP_LEFT)
                textView5.setText(R.string.up_left);
            else if (direction == JoyStick.STICK_NONE)
            else if (direction == JoyStick.STICK_NONE)
                textView5.setText(R.string.center);
        }
        else if (motionEvent.getAction() == MotionEvent.ACTION_UP && tap)
        {
            tap = false;
            //textView5.setText(R.string.direction);
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        thread.cancel();
        finish();
        super.onDestroy();
    }
}
