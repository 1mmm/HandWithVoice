package com.example.nisplab.handwithvoice.controller;

import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Message;
import android.util.Log;

import com.example.nisplab.handwithvoice.AudioService;
import com.example.nisplab.handwithvoice.MainActivity;

import static android.content.ContentValues.TAG;
import static android.media.AudioFormat.CHANNEL_CONFIGURATION_MONO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;
import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.AudioManager.STREAM_RING;
import static android.media.AudioTrack.MODE_STREAM;
import static com.example.nisplab.handwithvoice.ConstantValue.AUDIO_SAMPLE_RATE;
import static com.example.nisplab.handwithvoice.ConstantValue.BUFFER_SIZE;
import static com.example.nisplab.handwithvoice.ConstantValue.CAL_INTERVAL;
import static com.example.nisplab.handwithvoice.ConstantValue.DIS_NEGA;
import static com.example.nisplab.handwithvoice.ConstantValue.DIS_POSI;
import static com.example.nisplab.handwithvoice.ConstantValue.FREQ_INTERVAL;
import static com.example.nisplab.handwithvoice.ConstantValue.IN_DATA;
import static com.example.nisplab.handwithvoice.ConstantValue.MAX_NUM_FREQS;
import static com.example.nisplab.handwithvoice.ConstantValue.START_FREQ;
import static com.example.nisplab.handwithvoice.ConstantValue.TIME_INTERVAL;

/**
 * Created by NIS&PLAB on 2017/7/26.
 * 实现同步放音和录音，并调用RangeFinder对录音数据进行处理
 */

public class AudioController {
    private AudioRecord m_in_rec;
    private AudioTrack m_out_tra;
    private int inBufferSize, outBufferSize, mCurPlayPos;
    private RangeFinder rf;
    private Thread record, play;
    private Message m;
    private boolean flag, isNewGesture;
    private long startTime, timeTamp;
    private double temp, dis = 0;
    private double sensitivity, adjustation;
    private int timeSensitivity;


    public void init(int Sensitivity) {

        inBufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, CHANNEL_CONFIGURATION_MONO, ENCODING_PCM_16BIT);
        m_in_rec = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_SAMPLE_RATE, CHANNEL_CONFIGURATION_MONO, ENCODING_PCM_16BIT, inBufferSize);
        outBufferSize = AudioTrack.getMinBufferSize(AUDIO_SAMPLE_RATE, CHANNEL_CONFIGURATION_MONO, ENCODING_PCM_16BIT);
        m_out_tra = new AudioTrack(STREAM_MUSIC, AUDIO_SAMPLE_RATE, CHANNEL_CONFIGURATION_MONO, ENCODING_PCM_16BIT, outBufferSize, MODE_STREAM);

        rf = new RangeFinder(BUFFER_SIZE, MAX_NUM_FREQS, START_FREQ, FREQ_INTERVAL);
        flag = true;
        timeTamp = 0;
        mCurPlayPos = 0;
        sensitivity = Sensitivity * 0.01;
        adjustation = Sensitivity * 0.015;
        timeSensitivity = Sensitivity * 3;
        record = new Thread(new AudioController.recordSound());
        play = new Thread(new AudioController.playSound());
    }

    public void start() {
        record.start();
        play.start();
    }

    public void stop() {
        flag = false;
        m_in_rec.stop();
        m_in_rec.release();
        m_out_tra.stop();
        m_out_tra.release();
    }

    private class recordSound implements Runnable {
        @Override
        public void run()
        {
            m_in_rec.startRecording();
            isNewGesture = true;
            dis = 0;
            while (flag)
            {

                startTime = System.currentTimeMillis();

                //单声道读取
                /*
                * inBufferSize / 2 = 1920
                * 采样率为48000
                * 在不移动时，temp绝对值最大一般不会超过0.6
                */
                m_in_rec.read(rf.mRecDataBuffer, 0, 1920);
                rf.mCurRecPos = 1920;
                temp = rf.GetDistanceChange();

                // 阈值设定，根据移动距离和移动方向设定阈值
                // 灵敏度越大，可识别的移动距离范围越广
                if ((temp>-12.5-sensitivity&&temp<-2.5+sensitivity||temp<12.5+sensitivity&&temp>2.5-sensitivity)) {
                    timeTamp = startTime;
                    if (isNewGesture&&(temp<-2.6+adjustation||temp>2.6-adjustation)) {
                        Log.i(TAG, "........temp......" + temp);
                        isNewGesture = false;
                        m = new Message();
                        m.what = temp>0?DIS_POSI:DIS_NEGA;
                        AudioService.myHandler.sendMessage(m);
                    }
                } else if (temp>-2.2+sensitivity && temp<2.2-sensitivity){
                    // 距离上次出现满足条件的temp值已经过去TIME_INTERVAL时间，即TIME_INTERVAL表示两次有效操作之间的最短时间间隔
                    if (startTime - timeTamp > TIME_INTERVAL - timeSensitivity)
                        isNewGesture = true;
                } else if ((temp>14.5+sensitivity || temp<-14.5-sensitivity) && isNewGesture){
//                    Log.i(TAG, "........!!!!!!!......超出范围:" + temp);
                }
            }
        }
    }

    private class playSound implements Runnable {
        @Override
        public void run()
        {
            m_out_tra.play();
            while (flag)
            {
                //单声道写入
//                m_out_tra.write(rf.mPlayBuffer, mCurPlayPos, IN_DATA);
                m_out_tra.write(rf.mOutputData, mCurPlayPos*2, IN_DATA*2);
                mCurPlayPos += IN_DATA;

                if (mCurPlayPos >= BUFFER_SIZE*2)
                    mCurPlayPos = 0;
                rf.mCurPlayPos = mCurPlayPos;
            }
        }
    }
}