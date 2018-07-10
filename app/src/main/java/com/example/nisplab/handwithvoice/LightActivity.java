package com.example.nisplab.handwithvoice;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.nisplab.handwithvoice.MainActivity.sd;
import static com.example.nisplab.handwithvoice.MainActivity.zt;
public class LightActivity extends AppCompatActivity implements GestureDetector.OnGestureListener{
    public GestureDetector detector;
    private Button kz1;

    private TextView kz;
    private AlertDialog alert = null;
    private AlertDialog.Builder builder = null;
    private Toast toast;
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
    final Handler handed = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            kz1.setText("正在控制灯光！");
            zt=1;
            alert = null;
            builder = new AlertDialog.Builder(LightActivity.this);
            alert = builder
                    .setTitle("提示：")
                    .setMessage("申请成功！").create();             //创建AlertDialog对象
            alert.show();
            Thread thread = new Thread()
            {
                public void run()
                {
                    try
                    {
                        sleep(1000);
                    } catch (InterruptedException e)
                    {
                        // TODO 自动生成的 catch 块
                        e.printStackTrace();
                    }
                    // cancel和dismiss方法本质都是一样的，都是从屏幕中删除Dialog,唯一的区别是
                    // 调用cancel方法会回调DialogInterface.OnCancelListener如果注册的话,dismiss方法不会回掉
                    alert.cancel();

//                        dialog.dismiss();
                }
            };
            thread.start();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);
        detector=new GestureDetector(this,this);
        kz1=(Button)findViewById(R.id.kz1);
        kz=(TextView) findViewById(R.id.kz);
        kz1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sd=1-sd;
                if (sd==1)
                {
                    final ProgressDialog dialog = new ProgressDialog(LightActivity.this);
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条的形式为圆形转动的进度条
                    dialog.setCancelable(true);// 设置是否可以通过点击Back键取消
                    dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
                    // 设置提示的title的图标，默认是没有的，如果没有设置title的话只设置Icon是不会显示图标的
                    dialog.setTitle("提示");
                    // dismiss监听
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            // TODO Auto-generated method stub

                        }
                    });
                    // 监听Key事件被传递给dialog
                    dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode,
                                             KeyEvent event) {
                            // TODO Auto-generated method stub
                            return false;
                        }
                    });
                    // 监听cancel事件
                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            // TODO Auto-generated method stub

                        }
                    });


                    dialog.setMessage("正在申请控制灯光！");
                    dialog.show();
                    Thread thread = new Thread()
                    {
                        public void run()
                        {
                            try
                            {
                                sleep(2000);
                            } catch (InterruptedException e)
                            {
                                // TODO 自动生成的 catch 块
                                e.printStackTrace();
                            }
                            // cancel和dismiss方法本质都是一样的，都是从屏幕中删除Dialog,唯一的区别是
                            // 调用cancel方法会回调DialogInterface.OnCancelListener如果注册的话,dismiss方法不会回掉
                            handed.sendEmptyMessage(5);
                            dialog.cancel();

//                        dialog.dismiss();
                        }
                    };
                    thread.start();

                }
                else {
                    kz1.setText("点击申请控制灯光");
                    zt=3;
                   /* alert = null;
                    builder = new AlertDialog.Builder(LightActivity.this);
                    alert = builder
                            .setTitle("提示：")
                            .setMessage("灯光已被占用！")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alert.cancel();
                                }
                            }).create();             //创建AlertDialog对象
                    alert.show();*/
                }
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
        if (arg1.getX() > arg0.getX() + 50)
        {
            if (sd==0) {

                LightActivity.this.finish();
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
