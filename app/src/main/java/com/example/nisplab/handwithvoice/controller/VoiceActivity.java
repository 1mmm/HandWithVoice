package com.example.nisplab.handwithvoice.controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.nisplab.handwithvoice.R;

import static com.example.nisplab.handwithvoice.MainActivity.sd;

public class VoiceActivity extends AppCompatActivity implements GestureDetector.OnGestureListener{
    public GestureDetector detector;
    private Button kz1;
    private TextView kz;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        detector=new GestureDetector(this,this);
        kz1=(Button)findViewById(R.id.kz1);
        kz=(TextView) findViewById(R.id.kz);
        kz1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sd=1-sd;
                if (sd==1) {
                    delay(1000);
                    kz.setText("正在控制音响.");
                }
                else kz.setText("尚未控制音响.");
            }

        });
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
        if (arg0.getX() > arg1.getX() + 50)
        {
            if (sd==0) {

                VoiceActivity.this.finish();
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
    private void delay(int ms){
        try {
            Thread.currentThread();
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
