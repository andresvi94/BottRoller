package com.andresvi94.bottroller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

class JoyStick {
    static final int STICK_NONE = 0;
    static final int STICK_UP = 1;
    static final int STICK_UP_RIGHT = 2;
    static final int STICK_RIGHT = 3;
    static final int STICK_DOWN_RIGHT = 4;
    static final int STICK_DOWN = 5;
    static final int STICK_DOWN_LEFT = 6;
    static final int STICK_LEFT = 7;
    static final int STICK_UP_LEFT = 8;

    private int stickAlpha = 200;
    private int stickLayout = 200;
    private int offset = 0;

    private ViewGroup viewGroup;
    private ViewGroup.LayoutParams params;
    private int stickWidth, stickHeight;

    private int positionX = 0, positionY = 0, minDistance = 0;
    private float distance = 0, angle = 0;

    private DrawCanvas drawCanvas;
    private Paint paint;
    private Bitmap stick;

    private boolean isInBounds = false;

    JoyStick(Context context, ViewGroup layout) {
        stick = BitmapFactory.decodeResource(context.getResources(), R.drawable.image_button);

        stickWidth = stick.getWidth();
        stickHeight = stick.getHeight();

        drawCanvas = new DrawCanvas(context);
        paint = new Paint();
        viewGroup = layout;
        params = viewGroup.getLayoutParams();
    }

    void drawStick(MotionEvent motionEvent) {
        positionX = (int) (motionEvent.getX() - (params.width / 2));
        positionY = (int) (motionEvent.getY() - (params.height / 2));
        distance = (float) Math.sqrt(Math.pow(positionX, 2) + Math.pow(positionY, 2));
        angle = (float) cal_angle(positionX, positionY);

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            if (distance <= (params.width / 2) - offset) {
                drawCanvas.position(motionEvent.getX(), motionEvent.getY());
                draw();
                isInBounds = true;
            }
        } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE && isInBounds) {
            if (distance <= (params.width / 2) - offset) {
                drawCanvas.position(motionEvent.getX(), motionEvent.getY());
                draw();
            } else if (distance > (params.width / 2) - offset) {
                float x = (float) (Math.cos(Math.toRadians(cal_angle(positionX, positionY))) * ((params.width / 2) - offset));
                float y = (float) (Math.sin(Math.toRadians(cal_angle(positionX, positionY))) * ((params.height / 2) - offset));
                x += (params.width / 2);
                y += (params.height / 2);
                drawCanvas.position(x, y);
                draw();
            } else {
                viewGroup.removeView(drawCanvas);
            }
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            viewGroup.removeView(drawCanvas);
            isInBounds = false;
        }
    }

    public int[] getPosition() {
        if (distance > minDistance && isInBounds) {
            return new int[]{positionX, positionY};
        }
        return new int[]{0, 0};
    }

    int getX() {
        if (distance > minDistance && isInBounds) {
            return positionX;
        }
        return 0;
    }

    int getY() {
        if (distance > minDistance && isInBounds) {
            return positionY;
        }
        return 0;
    }

    float getAngle() {
        if (distance > minDistance && isInBounds) {
            return angle;
        }
        return 0;
    }

    float getDistance() {
        if (distance > minDistance && isInBounds) {
            return distance;
        }
        return 0;
    }

    void setMinimumDistance(int minDistance) {
        this.minDistance = minDistance;
    }

    int getMinimumDistance() {
        return minDistance;
    }

    int get8Direction() {
        if (distance > minDistance && isInBounds) {
            if (angle >= 247.5 && angle < 292.5) {
                return STICK_UP;
            } else if (angle >= 292.5 && angle < 337.5) {
                return STICK_UP_RIGHT;
            } else if (angle >= 337.5 || angle < 22.5) {
                return STICK_RIGHT;
            } else if (angle >= 22.5 && angle < 67.5) {
                return STICK_DOWN_RIGHT;
            } else if (angle >= 67.5 && angle < 112.5) {
                return STICK_DOWN;
            } else if (angle >= 112.5 && angle < 157.5) {
                return STICK_DOWN_LEFT;
            } else if (angle >= 157.5 && angle < 202.5) {
                return STICK_LEFT;
            } else if (angle >= 202.5 && angle < 247.5) {
                return STICK_UP_LEFT;
            }
        } else if (distance <= minDistance && isInBounds) {
            return STICK_NONE;
        }
        return 0;
    }

    void setOffset(int offset) {
        this.offset = offset;
    }

    void setStickAlpha(int alpha) {
        stickAlpha = alpha;
        paint.setAlpha(alpha);
    }

    public int getStickAlpha() {
        return stickAlpha;
    }

    void setLayoutAlpha(int alpha) {
        stickLayout = alpha;
        viewGroup.getBackground().setAlpha(alpha);
    }

    public int getLayoutAlpha() {
        return stickLayout;
    }

    void setStickSize(int width, int height) {
        stick = Bitmap.createScaledBitmap(stick, width, height, false);
        stickWidth = stick.getWidth();
        stickHeight = stick.getHeight();
    }

    public void setStickWidth(int width) {
        stick = Bitmap.createScaledBitmap(stick, width, stickHeight, false);
        stickWidth = stick.getWidth();
    }

    public void setStickHeight(int height) {
        stick = Bitmap.createScaledBitmap(stick, stickWidth, height, false);
        stickHeight = stick.getHeight();
    }

    public int getStickWidth() {
        return stickWidth;
    }

    public int getStickHeight() {
        return stickHeight;
    }

    void setLayoutSize(int width, int height) {
        params.width = width;
        params.height = height;
    }

    public int getLayoutWidth() {
        return params.width;
    }

    public int getLayoutHeight() {
        return params.height;
    }

    private double cal_angle(float x, float y) {
        if (x >= 0 && y >= 0)
            return Math.toDegrees(Math.atan(y / x));
        else if (x < 0 && y >= 0)
            return Math.toDegrees(Math.atan(y / x)) + 180;
        else if (x < 0 && y < 0)
            return Math.toDegrees(Math.atan(y / x)) + 180;
        else if (x >= 0 && y < 0)
            return Math.toDegrees(Math.atan(y / x)) + 360;
        return 0;
    }

    private void draw() {
        try {
            viewGroup.removeView(drawCanvas);
        } catch (Exception e) {
        }
        viewGroup.addView(drawCanvas);
    }

    private class DrawCanvas extends View {
        float x, y;

        private DrawCanvas(Context mContext) {
            super(mContext);
        }

        public void onDraw(Canvas canvas) {
            canvas.drawBitmap(stick, x, y, paint);
        }

        private void position(float pos_x, float pos_y) {
            x = pos_x - (stickWidth / 2);
            y = pos_y - (stickHeight / 2);
        }
    }
}
