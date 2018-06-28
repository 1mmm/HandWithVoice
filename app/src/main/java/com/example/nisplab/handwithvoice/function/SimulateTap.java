package com.example.nisplab.handwithvoice.function;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Message;
import android.util.Log;

import com.example.nisplab.handwithvoice.AudioService;

import static android.content.ContentValues.TAG;
import static com.example.nisplab.handwithvoice.ConstantValue.LR;
import static com.example.nisplab.handwithvoice.ConstantValue.TAP;
import static com.example.nisplab.handwithvoice.ConstantValue.UD;
import static com.example.nisplab.handwithvoice.ConstantValue.WEAK_LIGHT;


/**
 * Created by NIS&PLAB on 2017/7/26.
 */

public class SimulateTap implements SensorEventListener {

    private long startTime ,timeTamp = 0;
    private boolean isChange = false, state = false, isFirst = true;
    private int direction = 0; // 0表示左右滑动， 1表示上下滑动

    private Gesture gesture = new Gesture();

    private float lux = -1f, firstLux;
    private float para1, para2;

    private Message m;

    public void setState(boolean a) {
        state = a;
        isFirst = a;
    }
    public boolean getState() {return state; }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isFirst) {
            firstLux = event.values[0];
            if (firstLux<100) {
                para1 = 0.4f;
                para2 = 0.6f;
            } else if (firstLux < 200) {
                para1 = 0.3f;
                para2 = 0.6f;
            } else {
                para1 = 0.2f;
                para2 = 0.7f;
            }
            Log.i(TAG, firstLux+"");
            isFirst = false;
        }
        startTime = System.currentTimeMillis();
        if (Math.abs(lux - event.values[0]) > (para1*firstLux) && lux >0) {
            if (event.values[0] < (para1*firstLux)) {
                sendMessage(WEAK_LIGHT);
                timeTamp = startTime;
                isChange = true;
            } else if (event.values[0] > (para2*firstLux) && isChange) {
                // 长时间遮挡光传感器，800ms
                if (startTime - timeTamp > 1500) {
                    direction = (direction + 1) % 2;
                    sendMessage(direction == 0 ? LR : UD);
                } else if (startTime - timeTamp < 1400 && state) {
                    gesture.tap();
                    sendMessage(TAP);
                }
                isChange = false;
            }
        }
        lux = event.values[0];
    }

    private void sendMessage(int msg) {
        m = new Message();
        m.what = msg;
        AudioService.myHandler.sendMessage(m);
    }
}