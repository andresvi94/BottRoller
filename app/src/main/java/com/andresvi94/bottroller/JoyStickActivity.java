package com.andresvi94.bottroller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class JoyStickActivity extends AppCompatActivity {

    RelativeLayout layout_joystick;
    ImageView image_joystick, image_border;
    TextView textView1, textView2, textView3, textView4, textView5;
//    @BindView(R.id.stopBtn) Button stoBtn;

    JoyStick js;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joy_stick);

        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        textView4 = (TextView) findViewById(R.id.textView4);
        textView5 = (TextView) findViewById(R.id.textView5);

        layout_joystick = (RelativeLayout) findViewById(R.id.layout_joystick);

        js = new JoyStick(getApplicationContext()
                , layout_joystick, R.drawable.image_button);
        js.setStickSize(150, 150);
        js.setLayoutSize(500, 500);
        js.setLayoutAlpha(150);
        js.setStickAlpha(100);
        js.setOffset(90);
        js.setMinimumDistance(50);

//        Intent intent = getIntent();
//        BluetoothSelector ctrlTest = intent.getParcelableExtra("test");

        layout_joystick.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js.drawStick(arg1);
                if (arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    textView1.setText("X : " + String.valueOf(js.getX()));
                    //textView1.setText(getResources().getString(R.string.x,js.getX()));
                    textView2.setText("Y : " + String.valueOf(js.getY()));
                    textView3.setText("Angle : " + String.valueOf(js.getAngle()));
                    textView4.setText("Distance : " + String.valueOf(js.getDistance()));

                    int direction = js.get8Direction();
                    //New Code
                    Intent intent = getIntent();
//                    BluetoothSelector ctrlTest = intent.getParcelableExtra("test");
                    //ctrlTest.connectedThread.write(Integer.toString(direction));
                    Intent goingBack = new Intent();
                    goingBack.putExtra("Test", Integer.toString(direction));
                    setResult(RESULT_OK, goingBack);
                    //End
                    if (direction == JoyStick.STICK_UP) {
                        textView5.setText(R.string.up);
                    } else if (direction == JoyStick.STICK_UPRIGHT) {
                        textView5.setText(R.string.up_right);
                    } else if (direction == JoyStick.STICK_RIGHT) {
                        textView5.setText(R.string.right);
                    } else if (direction == JoyStick.STICK_DOWNRIGHT) {
                        textView5.setText(R.string.down_right);
                    } else if (direction == JoyStick.STICK_DOWN) {
                        textView5.setText(R.string.down);
                    } else if (direction == JoyStick.STICK_DOWNLEFT) {
                        textView5.setText(R.string.down_left);
                    } else if (direction == JoyStick.STICK_LEFT) {
                        textView5.setText(R.string.left);
                    } else if (direction == JoyStick.STICK_UPLEFT) {
                        textView5.setText(R.string.up_left);
                    } else if (direction == JoyStick.STICK_NONE) {
                        textView5.setText(R.string.center);
                    }
                } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    textView1.setText(R.string.x);
                    textView2.setText(R.string.y);
                    textView3.setText(R.string.angle);
                    textView4.setText(R.string.distance);
                    textView5.setText(R.string.direction);
                }
                return true;
            }
        });

        /*stoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });*/
    }

    @Override
    protected void onDestroy() {
        //Toast.makeText(getBaseContext(),"destroy",Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }
}
