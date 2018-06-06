package com.example.nisplab.handwithvoice;

/**
 * Created by OMT on 2017/6/16.
 */

public class ConstantValue {

    /*RangeFinder中使用到的常量*/
    //max number of frequency
    static public final int MAX_NUM_FREQS = 8;
    //audio sample rate
    static public final int AUDIO_SAMPLE_RATE = 48000; //should be the same as in controller, will add later
    //default temperature
    static public final double TEMPERATURE = 20;
    //volume0.22
    static public final double VOLUME = 0.25;
    //cic filter stages
    static public final int CIC_SEC = 4;
    //cic filter decimation
    static public final int CIC_DEC = 16;
    //cic filter delay
    static public final int CIC_DELAY = 17;
    //power threshold 15000
    static public final int POWER_THR = 15000;
    //peak threshold 220
    static public final int PEAK_THR = 210;
    //dc_trend threshold
    static public final double DC_TREND = 0.25;

    /*AudioController中使用到的常量*/
    //Start audio frequency 17500
    public static final double START_FREQ = 18000.0;
    //Frequency interval
    public static final double FREQ_INTERVAL = 350.0;
    //Number of frame size
    public static final int BUFFER_SIZE = 1920;

    public static final int IN_DATA = 480;  //以10ms为一帧

    public static final long TIME_INTERVAL = 400;

    public static final long CAL_INTERVAL = 200;


    /*表示方向的常量*/
    public static final int UP = 1;
    public static final int RIGHT = 2;
    public static final int DOWN = 3;
    public static final int LEFT = 4;


    /*标记消息类型的常量*/
    public static final int DIS_POSI = 1;
    public static final int DIS_NEGA = -1;
    public static final int SWIPE_ON = 2;
    public static final int SWIPE_OFF = -2;
    public static final int TAP_ON = 3;
    public static final int TAP_OFF = -3;
    public static final int CALL_ON = 4;
    public static final int CALL_OFF = -4;
    public static final int LR = 5;       // 水平滑动
    public static final int UD = -5;    // 垂直滑动
    public static final int RING = 6;
    public static final int IDLE = -6;
    public static final int ANSWER = 7;
    public static final int TAP = 8;

    public static final int WEAK_LIGHT = -9;
    public static final int HARD_LIGHT = 9;
}
