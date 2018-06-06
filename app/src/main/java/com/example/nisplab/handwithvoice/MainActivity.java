package com.example.nisplab.handwithvoice;


import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.nisplab.handwithvoice.controller.VoiceActivity;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import static android.content.ContentValues.TAG;


public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener{
    static public int sd=0;
    public GestureDetector detector;
    private Switch aSwitch, fun1, fun2, fun3;
    private Message m;
    private DiscreteSeekBar seekBar;



//    public static Handler myHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        detector=new GestureDetector(this,this);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Log.i(TAG, "heigth : " + dm.heightPixels);
        Log.i(TAG, "width : " + dm.widthPixels);


        aSwitch = (Switch) findViewById(R.id.identify);

        fun1 = (Switch) findViewById(R.id.fun1);
        fun2 = (Switch) findViewById(R.id.fun2);
        fun3 = (Switch) findViewById(R.id.fun3);

        aSwitch.setOnCheckedChangeListener(new Listener());

        fun1.setOnCheckedChangeListener(new Listener());
        fun2.setOnCheckedChangeListener(new Listener());
        fun3.setOnCheckedChangeListener(new Listener());

        seekBar = (DiscreteSeekBar) findViewById(R.id.sensitivity);


        /*seekBar = (SeekBar)findViewById(R.id.distance);
        myHandle = new Handler() {
            @Override
            public void handleMessage(Message m) {
                seekBar.setProgress(m.what);
            }
        };*/
    }


    /*@Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float x = event.getRawX();
        float y = event.getRawY();
        Log.i(TAG, "x : " + x + "     y : " + y);
        return true;
    }*/

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.identify: {
                    fun1.setEnabled(isChecked);
                    fun2.setEnabled(isChecked);
                    fun3.setEnabled(isChecked);

                    if (isChecked) {
                        Intent startIntent = new Intent(MainActivity.this, AudioService.class);
                        startIntent.putExtra("sensitivity", seekBar.getProgress());
                        startService(startIntent);
                        seekBar.setEnabled(false);
                    } else {
                        fun1.setChecked(isChecked);
                        fun2.setChecked(isChecked);
                        fun3.setChecked(isChecked);
                        Intent stopIntent = new Intent(MainActivity.this, AudioService.class);
                        stopService(stopIntent);
                        seekBar.setEnabled(true);
                    }
                    break;
                }

                //模拟滑动——开关
                case R.id.fun1: {
                    functionSwitch(1, isChecked);
                    break;
                }

                //模拟点击——开关
                case R.id.fun2: {
                    functionSwitch(2, isChecked);
                    break;
                }

                //来电接听——开关
                case R.id.fun3: {
                    functionSwitch(3, isChecked);
                    break;
                }

                default: break;
            }
        }
    }

    /*
    * x = 1（功能1）， 2（功能2）， 3（功能3）
    * y = switch开关的状态
    * SWIPE_ON = 2;
    * SWIPE_OFF = -2;
    * TAP_ON = 3;
    * TAP_OFF = -3;
    * CALL_ON = 4;
    * CALL_OFF = -4;
    * x + 1 表示开，-x - 1 表示关
    */
    private void functionSwitch(int x, boolean y) {
        m = new Message();
        m.what = y ? (x+1) : (-x-1);
        AudioService.myHandler.sendMessage(m);
    }
    public	boolean	onTouchEvent(MotionEvent	event)	{
//	TODO	Auto-generated	method	stub
//将该Activity上触碰事件交给GestureDetector处理
        return	detector.onTouchEvent(event);
    }
    @Override
    public	boolean	onDown(MotionEvent	arg0)	{
//	TODO	Auto-generated	method	stub
        return	false;
    }
    @Override
    public	boolean	onFling(MotionEvent arg0, MotionEvent	arg1, float	arg2,
                                 float	arg3) {

//	TODO	Auto-generated	method	stub
//当是Fragment0的时候
        if (arg1.getX() > arg0.getX() + 50)
        {
            if (sd==0) {
                Intent intent = new Intent(MainActivity.this, VoiceActivity.class);
                startActivity(intent);
            }
        }

        if (arg0.getX() > arg1.getX() + 50)
        {
            if (sd==0) {
                Intent intent = new Intent(MainActivity.this, LightActivity.class);
                startActivity(intent);
            }
        }
        return  false;
    }
    @Override
    public	void	onLongPress(MotionEvent arg0)	{
//	TODO	Auto-generated	method	stub

    }

    @Override
    public	boolean	onScroll(MotionEvent arg0, MotionEvent	arg1, float	arg2,
                                  float	arg3)	{
//	TODO	Auto-generated	method	stub
        return	false;
    }

    @Override
    public	void	onShowPress(MotionEvent arg0)	{
//	TODO	Auto-generated	method	stub

    }

    @Override
    public	boolean	onSingleTapUp(MotionEvent	arg0)	{
//	TODO	Auto-generated	method	stub
        return	false;
    }
}
