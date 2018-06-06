package com.example.nisplab.handwithvoice;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.example.nisplab.handwithvoice.controller.AudioController;
import com.example.nisplab.handwithvoice.function.Call;
import com.example.nisplab.handwithvoice.function.SimulateSwipe;
import com.example.nisplab.handwithvoice.function.SimulateTap;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static com.example.nisplab.handwithvoice.ConstantValue.ANSWER;
import static com.example.nisplab.handwithvoice.ConstantValue.CALL_OFF;
import static com.example.nisplab.handwithvoice.ConstantValue.CALL_ON;
import static com.example.nisplab.handwithvoice.ConstantValue.DIS_NEGA;
import static com.example.nisplab.handwithvoice.ConstantValue.DIS_POSI;
import static com.example.nisplab.handwithvoice.ConstantValue.DOWN;
import static com.example.nisplab.handwithvoice.ConstantValue.IDLE;
import static com.example.nisplab.handwithvoice.ConstantValue.LEFT;
import static com.example.nisplab.handwithvoice.ConstantValue.LR;
import static com.example.nisplab.handwithvoice.ConstantValue.RIGHT;
import static com.example.nisplab.handwithvoice.ConstantValue.RING;
import static com.example.nisplab.handwithvoice.ConstantValue.SWIPE_OFF;
import static com.example.nisplab.handwithvoice.ConstantValue.SWIPE_ON;
import static com.example.nisplab.handwithvoice.ConstantValue.TAP;
import static com.example.nisplab.handwithvoice.ConstantValue.TAP_OFF;
import static com.example.nisplab.handwithvoice.ConstantValue.TAP_ON;
import static com.example.nisplab.handwithvoice.ConstantValue.UD;
import static com.example.nisplab.handwithvoice.ConstantValue.UP;
import static com.example.nisplab.handwithvoice.ConstantValue.WEAK_LIGHT;

/**
 * Created by NIS&PLAB on 2017/6/30.
 * 后台识别手势并处理
 */

public class AudioService extends Service {

    private AudioController audioController;
    public static Handler myHandler;

    private SimulateSwipe simulateSwipe;
    private SimulateTap simulateTap;
    private Call call;

    private IntentFilter intentFilter;

    private SensorManager sensorManager;
    private Sensor sensorLight, sensorProximity;

    private AudioManager manager;

    private Toast toast;

    private int direction, sensitivity;  // 滑动方向，0表示左右滑动，1表示上下滑动
    private boolean callCome = false;   // callCome表示是否有来电
    private boolean home, swipeState, tapState, isAnswer = false;   // isAnswer表示是否正处于接听状态
    private long moveTime, lastMove = 0, pauseTime = 0;
    private int delay = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "onCreate() executed!!");

        manager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // 三种功能实现
        simulateSwipe = new SimulateSwipe();
        simulateTap = new SimulateTap();
        call = new Call();

        // 获取光传感器对象实现模拟点击
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);


        // 距离传感器
//        sensorProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
//        sensorManager.registerListener(new mySensor(), sensorProximity, SensorManager.SENSOR_DELAY_NORMAL);

        // 广播监听过滤，监听来电状况
        intentFilter = new IntentFilter();
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        intentFilter.setPriority(Integer.MAX_VALUE);

        myHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                /*
                * 接收MainActivity传来的开关参数，控制三个功能的开关
                * 接收AudioController传来的距离参数（远离>0 或 靠近<0）
                */
                handle(msg);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensitivity = intent.getIntExtra("sensitivity", 0);
        toastShow("系统灵敏度:" + sensitivity, 500, Gravity.BOTTOM, 0, 200);
        Log.i(TAG, sensitivity+"");
        audioController = new AudioController();
        audioController.init(sensitivity);
        if (requestFocus()) {
            audioController.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        audioController.stop();
        manager.abandonAudioFocus(audioFocusChangeListener);
    }

    private void toastShow(String text, long time, int focus, int xOffset, int yOffset) {
        if (toast == null) {
            toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        } else {
            toast.cancel();
            toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
//            toast.setText(text);
        }
        toast.setGravity(focus, xOffset, yOffset);
        toast.show();
        if (time < 2000 && time!=Toast.LENGTH_SHORT) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, time);
        }
    }

    private void handle(Message m) {
        switch (m.what) {
            case SWIPE_ON: {
                delay = 0;
                Log.i(TAG, "swipe on");
//                toastShow("开启模拟滑动", Toast.LENGTH_SHORT, Gravity.BOTTOM, 0, 300);
                simulateSwipe.setState(true);
                break;
            }
            case SWIPE_OFF: {
                Log.i(TAG, "swipe off");
//                toastShow("关闭模拟滑动", Toast.LENGTH_SHORT, Gravity.BOTTOM, 0, 300);
                simulateSwipe.setState(false);
                break;
            }
            case TAP_ON: {
                Log.i(TAG, "tap on");
                sensorManager.registerListener(simulateTap, sensorLight, SensorManager.SENSOR_DELAY_NORMAL);
//                toastShow("开启模拟点击", Toast.LENGTH_SHORT, Gravity.BOTTOM, 0, 300);
                simulateTap.setState(true);
                break;
            }
            case TAP_OFF: {
                Log.i(TAG, "tap off");
                sensorManager.unregisterListener(simulateTap);
//                toastShow("关闭模拟滑动", Toast.LENGTH_SHORT, Gravity.BOTTOM, 0, 300);
                simulateTap.setState(false);
                break;
            }
            case CALL_ON: {
                Log.i(TAG, "call on");
//                toastShow("开启来电接听", Toast.LENGTH_SHORT, Gravity.BOTTOM, 0, 300);
                registerReceiver(call, intentFilter);
                break;
            }
            case CALL_OFF: {
                Log.i(TAG, "call off");
//                toastShow("关闭来电接听", Toast.LENGTH_SHORT, Gravity.BOTTOM, 0, 300);
                unregisterReceiver(call);
                break;
            }
            case DIS_POSI: {
                disResponse(DIS_POSI);
                break;
            }
            case DIS_NEGA: {
                disResponse(DIS_NEGA);
                break;
            }
            case TAP: {
                toastShow("·", 500, Gravity.BOTTOM, 0, 500);
                break;
            }
            case LR: {
                if (simulateSwipe.getState()) {
                    direction = 0;
                    Log.i(TAG, "左右滑动");
                    toastShow("↔", 500, Gravity.BOTTOM, 0, 500);
                }
                break;
            }
            case UD: {
                if (simulateSwipe.getState()) {
                    direction = 1;
                    Log.i(TAG, "上下滑动");
                    toastShow("↕", 500, Gravity.BOTTOM, 0, 500);
                }
                break;
            }
            case WEAK_LIGHT: {
                if (simulateSwipe.getState()) delay = delay+2;
                break;
            }
            case RING: {
                home = isHome();
                callCome = true;
                // 来电后暂时关闭模拟点击和模拟滑动功能
                swipeState = simulateSwipe.getState();
                tapState = simulateTap.getState();
                simulateSwipe.setState(false);
                simulateTap.setState(false);
                break;
            }
            case IDLE: {
                callCome = false;
                isAnswer = false;
                // 拒听或者通话结束后恢复模拟点击和模拟滑动功能
                simulateSwipe.setState(swipeState);
                simulateTap.setState(tapState);
                break;
            }
            case ANSWER: {
                isAnswer = true;
                break;
            }
            default: break;
        }
    }

    private void disResponse(int DIS) {
        if (!callCome && simulateSwipe.getState()) {
            moveTime = System.currentTimeMillis();
            if ((lastMove == 0 || moveTime - lastMove < 3000) && delay == 0) {
                if (direction == 0 && DIS == DIS_NEGA) {
                    toastShow("←", 500, Gravity.BOTTOM, -80, 500);
                    simulateSwipe.swipe(LEFT);
                } else if (direction == 1 && DIS == DIS_NEGA) {
                    toastShow("↑", 500, Gravity.BOTTOM, 0, 580);
                    simulateSwipe.swipe(UP);
                } else if (direction == 0 && DIS == DIS_POSI) {
                    toastShow("→", 500, Gravity.BOTTOM, 80, 500);
                    simulateSwipe.swipe(RIGHT);
                } else if (direction == 1 && DIS == DIS_POSI) {
                    toastShow("↓", 500, Gravity.BOTTOM, 0, 420);
                    simulateSwipe.swipe(DOWN);
                }
            } else if (moveTime - lastMove >= 3000) {
                if (pauseTime == 0 || moveTime - pauseTime > 8000) {
                    delay = 2;
                } else {
                    delay++;
                }
                pauseTime = moveTime;  //每次暂停记录此次暂停时间
            }
            if (delay > 0) {
                delay--;
                toastShow("暂停:" + delay, 500, Gravity.BOTTOM, 0, 200);
            }
            lastMove = moveTime;
        } else if (!isAnswer && callCome) {
            if (DIS == DIS_NEGA)
                call.reject(home);
            else if (DIS == DIS_POSI)
                call.answer(home);
        }
    }

    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                Log.i(TAG, "暂时失去焦点");
                // Pause playback
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                Log.i(TAG, "重新获得焦点");
                // Resume playback
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                Log.i(TAG, "永久失去焦点");
                // mAm.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
//                mAm.abandonAudioFocus(afChangeListener);
                // Stop playback
            }
        }
    };

   /*
   * 请求音频焦点
   * */
    private boolean requestFocus() {
        int result = manager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    /**
     * 判断当前界面是否是桌面
     */
    private boolean isHome() {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        return getHomes().contains(rti.get(0).topActivity.getPackageName());
    }

    /**
     * 获得属于桌面的应用的应用包名称
     *
     * @return 返回包含所有包名的字符串列表
     */
    private List<String> getHomes() {
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }

    private class mySensor implements SensorEventListener {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float value = event.values[0];
            if (value < 4) {
                swipeState = simulateSwipe.getState();
                simulateSwipe.setState(false);
            } else {
                simulateSwipe.setState(swipeState);
            }
        }
    }
}