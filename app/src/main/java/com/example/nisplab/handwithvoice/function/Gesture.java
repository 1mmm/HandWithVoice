package com.example.nisplab.handwithvoice.function;


import android.util.Log;

import java.io.DataOutputStream;
import java.io.OutputStream;

import static android.content.ContentValues.TAG;
import static com.example.nisplab.handwithvoice.ConstantValue.DOWN;
import static com.example.nisplab.handwithvoice.ConstantValue.LEFT;
import static com.example.nisplab.handwithvoice.ConstantValue.RIGHT;
import static com.example.nisplab.handwithvoice.ConstantValue.UP;

/**
 * Created by NIS&PLAB on 2017/6/29.
 * 使用adb命令来模拟用户手势
 */

public class Gesture {

    private OutputStream os;
    private Process process;

    /**
     * 执行shell指令
     *
     * @param cmd
     *            指令
     */
    public void exec(String cmd) {
        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            process = Runtime.getRuntime().exec("su");
            // 获取输出流
            os = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(os);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            os.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * 后台模拟全局按键
     *
     * @param keyCode
     *            键值
     */
    public void simulateKey(int keyCode) {
        exec("input keyevent " + keyCode + "\n");
        Log.i(TAG, "........keyCode......" + keyCode);
    }

    /**
     * 后台模拟滑动
     *
     * @param direction
     * 1：上滑
     * 2：右滑
     * 3：下滑
     * 4：左滑
     */
    public void swipe(int direction) {
        switch (direction) {
            case UP : {
                exec("input swipe 500 1500 500 800");
                Log.i(TAG, "........UP......");
                break;
            }

            case RIGHT : {
                exec("input swipe 150 1500 850 1500");
                Log.i(TAG, "........RIGHT......");
                break;
            }

            case DOWN : {
                exec("input swipe 500 800 500 1500");
                Log.i(TAG, "........DOWN......");
                break;
            }

            case LEFT : {
                exec("input swipe 850 1500 150 1500");
                Log.i(TAG, "........LEFT......");
                break;
            }
        }
    }

    /**
     * 后台模拟点击
     */
    public void tap() {
        exec("input tap 900 620");
        Log.i(TAG, "........TAP......");
    }

    public void answer(boolean home) {
        if (home) {
            exec("input swipe 202 1600 540 1600");
            Log.i(TAG, "........answer call on home......");
        } else {
            exec("input tap 252 237");
            Log.i(TAG, "........answer call not on home......");
        }
    }

    public void reject(boolean home) {
        if (home) {
            exec("input swipe 878 1600 540 1600");
            Log.i(TAG, "........reject call on home......");
        } else {
            exec("input tap 828 237");
            Log.i(TAG, "........reject call not on home......");
        }
    }
}
