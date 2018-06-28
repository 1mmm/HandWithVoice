package com.example.nisplab.handwithvoice;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nisplab.handwithvoice.controller.AudioController;
import com.example.nisplab.handwithvoice.controller.VoiceActivity;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;


public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener{
    static public int sd=0;

    private AcceptThread acceptThread;
    BluetoothServerSocket serverSocket;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(), String.valueOf(msg.obj),
                    Toast.LENGTH_LONG).show();
            super.handleMessage(msg);
        }
    };
    private class AcceptThread extends Thread {
        public AcceptThread() {
            try {
                serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("raspberrypi", MY_UUID);
            } catch (Exception e) {
            }
        }
        public void run() {
            try {
                socket = serverSocket.accept();
                InputStream is = socket.getInputStream();
                while(true) {
                    byte[] buffer =new byte[1024];
                    int count = is.read(buffer);
                    Message msg = new Message();
                    msg.obj = new String(buffer, 0, count, "utf-8");
                    handler.sendMessage(msg);
                }
            }
            catch (Exception e) {
            }
        }
    }
    private class ClientThread extends Thread {

        private BluetoothDevice device;

        public ClientThread(BluetoothDevice device) {
            this.device = device;
        }

        @Override
        public void run() {

            BluetoothSocket socket = null;

            try {
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUI));

                Log.d(tag, "连接服务端...");
                socket.connect();
                Log.d(tag, "连接建立.");
                out=socket.getOutputStream();
                while (true) {
                    try {
                        sleep(50);
                        String s = (AudioService.direction==1?"777":"888")+AudioController.tp + "\n";
                        if (sd==1) {
                            out.write(strToByteArray(s));
                            out.flush();
                        }
                    }catch (Exception e) {

                        e.printStackTrace();
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static byte[] strToByteArray(String str) { if (str == null) { return null; } byte[] byteArray = str.getBytes(); return byteArray; }
    private BluetoothAdapter mBluetoothAdapter;
    public OutputStream out=null;
    public BluetoothSocket socket;
    private final String tag = "1mmm";
    private final UUID MY_UUID = UUID
            .fromString("abcd1234-ab12-ab12-ab12-abcdef123456");
    private final String MY_UUI = "abcd1234-ab12-ab12-ab12-abcdef123456";
    public GestureDetector detector;
    private TextView aa;
    private Switch aSwitch, fun1, fun2, fun3;
    private Message m;
    private DiscreteSeekBar seekBar;



//    public static Handler myHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        List<String> devices = new ArrayList<String>();
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        acceptThread = new AcceptThread();
        acceptThread.start();
        for (BluetoothDevice device : bondedDevices) {
            if (device.getName().equals("1mmm")) new Thread(new ClientThread(device)).start();
            Log.d(device.getName() + "-" + device.getAddress(), "onClick: ");
            devices.add(device.getName() + "-" + device.getAddress());
        }
        aa=(TextView) findViewById(R.id.aa);
        aa.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {
                                            MainActivity.this.finish();
                                  }
                              });
        detector=new GestureDetector(this,this);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Log.i(TAG, "heigth : " + dm.heightPixels);
        Log.i(TAG, "width : " + dm.widthPixels);




        Intent startIntent = new Intent(MainActivity.this, AudioService.class);
        startIntent.putExtra("sensitivity", 10);
        startService(startIntent);

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
