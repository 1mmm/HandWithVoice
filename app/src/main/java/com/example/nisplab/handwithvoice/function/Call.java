package com.example.nisplab.handwithvoice.function;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.nisplab.handwithvoice.AudioService;

import static android.content.ContentValues.TAG;
import static com.example.nisplab.handwithvoice.ConstantValue.ANSWER;
import static com.example.nisplab.handwithvoice.ConstantValue.IDLE;
import static com.example.nisplab.handwithvoice.ConstantValue.RING;

/**
 * Created by NIS&PLAB on 2017/7/26.
 * 来电监听广播
 */

public class Call extends BroadcastReceiver {

    private Message m;

    private Gesture gesture = new Gesture();

    // 一次来电时会有多次响铃，就会多次调用监听器中的响铃事件，定义isFirstRing只在第一次响铃时执行
    private boolean isFirstRing = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(new PhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);
        }
    }



    class PhoneListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String number) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE: {
                    isFirstRing = true;
                    m = new Message();
                    m.what = IDLE;
                    AudioService.myHandler.sendMessage(m);
                    Log.i(TAG, "挂断");
                    break;
                }

                case TelephonyManager.CALL_STATE_OFFHOOK: {
                    m = new Message();
                    m.what = ANSWER;
                    AudioService.myHandler.sendMessage(m);
                    Log.i(TAG, "接听");
                    break;
                }

                case TelephonyManager.CALL_STATE_RINGING: {
                    if (isFirstRing) {
                        m = new Message();
                        m.what = RING;
                        AudioService.myHandler.sendMessage(m);
                        Log.i(TAG, "响铃");
                        isFirstRing = false;
                    }
                    break;
                }
                default: break;
            }
            super.onCallStateChanged(state, number);
        }
    }

    // 是否在桌面接听电话
    public void answer(boolean home) {
        gesture.answer(home);
    }

    public void reject(boolean home) {
        gesture.reject(home);
    }
}
