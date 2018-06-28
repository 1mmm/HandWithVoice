package com.example.nisplab.handwithvoice.controller;


import static com.example.nisplab.handwithvoice.ConstantValue.AUDIO_SAMPLE_RATE;
import static com.example.nisplab.handwithvoice.ConstantValue.BUFFER_SIZE;
import static com.example.nisplab.handwithvoice.ConstantValue.CIC_DEC;
import static com.example.nisplab.handwithvoice.ConstantValue.CIC_DELAY;
import static com.example.nisplab.handwithvoice.ConstantValue.CIC_SEC;
import static com.example.nisplab.handwithvoice.ConstantValue.DC_TREND;
import static com.example.nisplab.handwithvoice.ConstantValue.MAX_NUM_FREQS;
import static com.example.nisplab.handwithvoice.ConstantValue.PEAK_THR;
import static com.example.nisplab.handwithvoice.ConstantValue.POWER_THR;
import static com.example.nisplab.handwithvoice.ConstantValue.TEMPERATURE;
import static com.example.nisplab.handwithvoice.ConstantValue.VOLUME;
import static com.example.nisplab.handwithvoice.controller.DataProc.memmove;
import static com.example.nisplab.handwithvoice.controller.DataProc.vDSP_maxv;
import static com.example.nisplab.handwithvoice.controller.DataProc.vDSP_minv;
import static com.example.nisplab.handwithvoice.controller.DataProc.vDSP_sve;
import static com.example.nisplab.handwithvoice.controller.DataProc.vDSP_vmul;
import static com.example.nisplab.handwithvoice.controller.DataProc.vDSP_vsadd;
import static com.example.nisplab.handwithvoice.controller.DataProc.vDSP_vsdiv;
import static com.example.nisplab.handwithvoice.controller.DataProc.vDSP_vsq;
import static com.example.nisplab.handwithvoice.controller.DataProc.vDSP_vsub;
import static com.example.nisplab.handwithvoice.controller.DataProc.vDSP_vswsum;
import static com.example.nisplab.handwithvoice.controller.DataProc.vDSP_zvmags;
import static com.example.nisplab.handwithvoice.controller.DataProc.vDSP_zvphas;
import static java.lang.Math.PI;

/**
 * Created by OMT on 2017/6/16.
 * 处理麦克风中收录的音频数据，计算距离
 */

public class RangeFinder {

    private int mNumFreqs;//number of frequency
    public int mCurPlayPos;//current play position
    public int mCurProcPos;//current process position
    public int mCurRecPos;//current receive position
    private int mLastCICPos;//last cic filter position
    private int mBufferSize;//buffer size
//    private int mRecDataSize;//receive data size
    private int mDecsize;//buffer size after decimation
//    private double mFreqInterv;//frequency interval
    private double mSoundSpeed;//sound speed
    private double[] mFreqs = new double[MAX_NUM_FREQS];//frequency of the ultsound signal
    private double[] mWaveLength = new double[MAX_NUM_FREQS];//wave length of the ultsound signal

    private short[] mPlayBuffer;
    public byte[] mOutputData;
    public short[] mRecDataBuffer;
    private double[] mFRecDataBuffer;
    private double[][] mSinBuffer = new double[MAX_NUM_FREQS][];
    private double[][] mCosBuffer = new double[MAX_NUM_FREQS][];
    private double[][] mBaseBandReal = new double[MAX_NUM_FREQS][];
    private double[][] mBaseBandImage = new double[MAX_NUM_FREQS][];
    private double[] mTempBuffer;
    private double[][][][] mCICBuffer = new double[MAX_NUM_FREQS][CIC_SEC][2][];
    private double[][] mDCValue = new double[2][MAX_NUM_FREQS];
    private double[][] mMaxValue = new double[2][MAX_NUM_FREQS];
    private double[][] mMinValue = new double[2][MAX_NUM_FREQS];
    private double[] mFreqPower = new double[MAX_NUM_FREQS];

    public int mSocBufPos;

    public RangeFinder(int inMaxFramesPerSlice, int inNumFreq, double inStartFreq, double inFreqInterv)
    {
        //Number of frequency
        mNumFreqs = inNumFreq;

        //Buffer size
        mBufferSize = inMaxFramesPerSlice * 4;

        //Frequency interval
//        mFreqInterv = inFreqInterv;

        //Receive data size
//        mRecDataSize = inMaxFramesPerSlice;

        //Sound speed
        mSoundSpeed = 331.3 + 0.606 * TEMPERATURE;

        //Init buffer
        //MAX_NUM_FREQS = 8, for N
        for(int i = 0; i < MAX_NUM_FREQS; i++){
            mSinBuffer[i] = new double[mBufferSize/2];
            mCosBuffer[i] = new double[mBufferSize/2];

            mFreqs[i] = inStartFreq + i * inFreqInterv;	//fk = f0 + k∆f, k = 0 ... N − 1

            mWaveLength[i] = mSoundSpeed/mFreqs[i] * 1000; //all distance is in mm

            mBaseBandReal[i] = new double[mBufferSize/CIC_DEC];
            mBaseBandImage[i] = new double[mBufferSize/CIC_DEC];
            for(int k = 0; k < CIC_SEC; k++)
            {
                mCICBuffer[i][k][0] = new double[mBufferSize/CIC_DEC+CIC_DELAY];
                mCICBuffer[i][k][1] = new double[mBufferSize/CIC_DEC+CIC_DELAY];
            }
        }

        mPlayBuffer = new short[mBufferSize/2];
        mOutputData = new byte[mBufferSize];

        mRecDataBuffer = new short[mBufferSize];
        mFRecDataBuffer = new double[mBufferSize];
        mTempBuffer = new double[mBufferSize];
        mCurPlayPos = 0;
        mCurRecPos = 0;
        mCurProcPos = 0;
        mLastCICPos = 0;
        mDecsize = 0;
        mSocBufPos = 0;

        InitBuffer();
    }

    private void InitBuffer() {
        for(int i = 0; i < mNumFreqs; i++){
            for(int n = 0; n < mBufferSize/2; n++){
                mCosBuffer[i][n] = Math.cos(2*PI*n/AUDIO_SAMPLE_RATE*mFreqs[i]);
                mSinBuffer[i][n] = -Math.sin(2*PI*n/AUDIO_SAMPLE_RATE*mFreqs[i]);
            }
            mDCValue[0][i] = 0;
            mMaxValue[0][i] = 0;
            mMinValue[0][i] = 0;
            mDCValue[1][i] = 0;
            mMaxValue[1][i] = 0;
            mMinValue[1][i] = 0;
        }

        double mTempSample;
        int j = 0;
        short x;
        for(int n = 0; n < mBufferSize/2; n++){
            mTempSample=0;
            for(int i=0; i < mNumFreqs; i++){
                mTempSample += mCosBuffer[i][n]*VOLUME;
            }
            mPlayBuffer[n]=(short) (mTempSample/mNumFreqs*32767);
            mOutputData[j++] = (byte) (mPlayBuffer[n] & 0x00ff);
            mOutputData[j++] = (byte) ((mPlayBuffer[n] & 0xff00) >>> 8);
        }
    }

    public double GetDistanceChange()
    {
        double distancechange = 0;

        //each time we process the data in the RecDataBuffer and clear the mCurRecPos

        //Get base band signal
        GetBaseBand();

        //Remove dcvalue from the baseband signal
        RemoveDC();

        //Calculate distance from the phase change
        distancechange = CalculateDistance();

        return distancechange;
    }

    private void GetBaseBand()
    {
        int i,index,decsize,cid;
        decsize = mCurRecPos/CIC_DEC;
        mDecsize = decsize;

        //change data from int to double32

        for(i = 0; i < mCurRecPos; i++)
        {
            mFRecDataBuffer[i] = (double) (mRecDataBuffer[i]/32767.0);	//归一化
        }

        for(i = 0;i < mNumFreqs; i++)//mNumFreqs
        {
            //mTempBuffer[j] = mFRecDataBuffer[j] * mCosBuffer[i][mCurProcPos+j], for j = 0...mCurRecPos-1
            vDSP_vmul(mFRecDataBuffer, 0, mCosBuffer[i], mCurProcPos, mTempBuffer, 0, mCurRecPos); //multiply the cos
            cid = 0;
            //sum CIC_DEC points of data, put into CICbuffer
            memmove(mCICBuffer[i][0][cid], 0, mCICBuffer[i][0][cid], mLastCICPos, CIC_DELAY);
            index = CIC_DELAY;
            for(int k = 0; k < mCurRecPos; k += CIC_DEC)
            {
                //mCICBuffer[i][0][cid][index] = mTempBuffer[k+i], for i = 0...CIC_DEC-1
                mCICBuffer[i][0][cid][index] = vDSP_sve(mTempBuffer, k, CIC_DEC);
                index++;
            }

            //prepare CIC first level
            memmove(mCICBuffer[i][1][cid], 0, mCICBuffer[i][1][cid], mLastCICPos, CIC_DELAY);
            //Sliding window sum
            vDSP_vswsum(mCICBuffer[i][0][cid], 0, mCICBuffer[i][1][cid], CIC_DELAY, decsize, CIC_DELAY);
            //prepare CIC second level
            memmove(mCICBuffer[i][2][cid], 0, mCICBuffer[i][2][cid], mLastCICPos, CIC_DELAY);
            //Sliding window sum
            vDSP_vswsum(mCICBuffer[i][1][cid], 0, mCICBuffer[i][2][cid], CIC_DELAY, decsize, CIC_DELAY);
            //prepare CIC third level
            memmove(mCICBuffer[i][3][cid], 0, mCICBuffer[i][3][cid], mLastCICPos, CIC_DELAY);
            //Sliding window sum
            vDSP_vswsum(mCICBuffer[i][2][cid], 0, mCICBuffer[i][3][cid], CIC_DELAY, decsize, CIC_DELAY);
            //CIC last level to Baseband
            vDSP_vswsum(mCICBuffer[i][3][cid], 0, mBaseBandReal[i], 0, decsize, CIC_DELAY);


            vDSP_vmul(mFRecDataBuffer, 0, mSinBuffer[i], mCurProcPos, mTempBuffer, 0, mCurRecPos); //multiply the sin
            cid = 1;
            //sum CIC_DEC points of data, put into CICbuffer
            memmove(mCICBuffer[i][0][cid], 0, mCICBuffer[i][0][cid], mLastCICPos, CIC_DELAY);
            index = CIC_DELAY;
            for(int k = 0; k < mCurRecPos; k += CIC_DEC)
            {
                mCICBuffer[i][0][cid][index] = vDSP_sve(mTempBuffer, k, CIC_DEC);
                index++;
            }

            //prepare CIC first level
            memmove(mCICBuffer[i][1][cid], 0, mCICBuffer[i][1][cid], mLastCICPos, CIC_DELAY);
            //Sliding window sum
            vDSP_vswsum(mCICBuffer[i][0][cid], 0, mCICBuffer[i][1][cid], CIC_DELAY, decsize, CIC_DELAY);
            //prepare CIC second level
            memmove(mCICBuffer[i][2][cid], 0, mCICBuffer[i][2][cid], mLastCICPos, CIC_DELAY);
            //Sliding window sum
            vDSP_vswsum(mCICBuffer[i][1][cid], 0, mCICBuffer[i][2][cid], CIC_DELAY, decsize, CIC_DELAY);
            //prepare CIC third level
            memmove(mCICBuffer[i][3][cid], 0, mCICBuffer[i][3][cid], mLastCICPos, CIC_DELAY);
            //Sliding window sum
            vDSP_vswsum(mCICBuffer[i][2][cid], 0, mCICBuffer[i][3][cid], CIC_DELAY, decsize, CIC_DELAY);
            //CIC last level to Baseband
            vDSP_vswsum(mCICBuffer[i][3][cid], 0, mBaseBandImage[i], 0, decsize, CIC_DELAY);

        }
        mCurProcPos = mCurProcPos + mCurRecPos;
        if(mCurProcPos >= BUFFER_SIZE)
            mCurProcPos = mCurProcPos - BUFFER_SIZE;
        mCurRecPos = 0;
        mLastCICPos = decsize;

    }

    private void RemoveDC()
    {
        int f,i;
        double[] tempdata = new double[4096], tempdata2 = new double[4096];
        double temp_val;
        double vsum,dsum,max_valr,min_valr,max_vali,min_vali;
        if(mDecsize > 4096)
            return;

        //'Levd' algorithm to calculate the DC value;
        for(f = 0; f < mNumFreqs; f++)
        {
            vsum=0;
            dsum=0;
            //real part
            max_valr = vDSP_maxv(mBaseBandReal[f], 0, mDecsize);
            min_valr = vDSP_minv(mBaseBandReal[f], 0, mDecsize);
            //getvariance,first remove the first value
            temp_val = -mBaseBandReal[f][0];
            vDSP_vsadd(mBaseBandReal[f], 0, temp_val, tempdata, 0, mDecsize);
            temp_val = vDSP_sve(tempdata, 0, mDecsize);
            dsum = dsum + Math.abs(temp_val)/mDecsize;
            vDSP_vsq(tempdata, 0, tempdata2, 0, mDecsize);
            temp_val = vDSP_sve(tempdata2, 0, mDecsize);
            vsum = vsum + Math.abs(temp_val)/mDecsize;

            //imag part
            max_vali = vDSP_maxv(mBaseBandImage[f], 0, mDecsize);
            min_vali = vDSP_minv(mBaseBandImage[f], 0, mDecsize);
            //getvariance,first remove the first value
            temp_val = -mBaseBandImage[f][0];
            vDSP_vsadd(mBaseBandImage[f], 0, temp_val, tempdata, 0, mDecsize);
            temp_val = vDSP_sve(tempdata, 0, mDecsize);
            dsum = dsum + Math.abs(temp_val)/mDecsize;
            vDSP_vsq(tempdata, 0, tempdata2, 0, mDecsize);
            temp_val = vDSP_sve(tempdata2, 0, mDecsize);
            vsum = vsum + Math.abs(temp_val)/mDecsize;

            mFreqPower[f] = (vsum + dsum * dsum);///fabs(vsum-dsum*dsum)*vsum;

            //Get DC estimation
            if(mFreqPower[f]>POWER_THR)
            {
                if ( max_valr > mMaxValue[0][f] ||
                        (max_valr > mMinValue[0][f]+PEAK_THR &&
                                (mMaxValue[0][f]-mMinValue[0][f]) > PEAK_THR*4) )
                {
                    mMaxValue[0][f]=max_valr;
                }

                if ( min_valr < mMinValue[0][f] ||
                        (min_valr < mMaxValue[0][f]-PEAK_THR &&
                                (mMaxValue[0][f]-mMinValue[0][f]) > PEAK_THR*4) )
                {
                    mMinValue[0][f]=min_valr;
                }

                if ( max_vali > mMaxValue[1][f] ||
                        (max_vali > mMinValue[1][f]+PEAK_THR &&
                                (mMaxValue[1][f]-mMinValue[1][f]) > PEAK_THR*4) )
                {
                    mMaxValue[1][f]=max_vali;
                }

                if ( min_vali < mMinValue[1][f] ||
                        (min_vali < mMaxValue[1][f]-PEAK_THR &&
                                (mMaxValue[1][f]-mMinValue[1][f]) > PEAK_THR*4) )
                {
                    mMinValue[1][f]=min_vali;
                }


                if ( (mMaxValue[0][f]-mMinValue[0][f]) > PEAK_THR &&
                        (mMaxValue[1][f]-mMinValue[1][f]) > PEAK_THR )
                {
                    for(i=0;i<=1;i++)
                        mDCValue[i][f]=(1-DC_TREND)*mDCValue[i][f]+
                                (mMinValue[i][f]+mMinValue[i][f])/2*DC_TREND;
                }

            }

            //remove DC
            for(i=0;i<mDecsize;i++)
            {
                mBaseBandReal[f][i]=mBaseBandReal[f][i]-mDCValue[0][f];
                mBaseBandImage[f][i]=mBaseBandImage[f][i]-mDCValue[1][f];
            }

        }
    }

    private double CalculateDistance()
    {
        double distance=0;
        double[] tempdata = new double[4096], tempdata2 = new double[4096], tempdata3 = new double[4096];
        double temp_val;
        double[][] phasedata = new double[MAX_NUM_FREQS][4096];
        int[] ignorefreq = new int[MAX_NUM_FREQS];

        //mDecsize = mCurRecPos/CIC_DEC
        if (mDecsize>4096)
            return 0;

        for(int f = 0; f < mNumFreqs; f++)
        {
            ignorefreq[f]=0;

            //get magnitude
            vDSP_zvmags(mBaseBandReal[f], 0, mBaseBandImage[f], 0, tempdata, 0, mDecsize);	//tempdata[i] = tempcomplex[i].realp^2 + tempcomplex[i].imagp^2, for i = 0...mDecsize
            temp_val = vDSP_sve(tempdata, 0, mDecsize);			//temp_val = tempdata[0] +...+ tempdata[i] +..., for i = 0...mDecsize
            if(temp_val/mDecsize>POWER_THR) //only calculate the high power vectors
            {
                vDSP_zvphas(mBaseBandReal[f], 0, mBaseBandImage[f], 0, phasedata[f], 0, mDecsize);	//phasedata[f][i] = arctan(tempcomplex[i].imagp/tempcomplex[i].realp), for i = 0...mDecsize
                //phase unwarp
                for(int i=1;i<mDecsize;i++)
                {
                    while(phasedata[f][i]-phasedata[f][i-1]>PI)
                        phasedata[f][i]=phasedata[f][i]-2*PI;
                    while(phasedata[f][i]-phasedata[f][i-1]<-PI)
                        phasedata[f][i]=phasedata[f][i]+2*PI;
                }
                if(Math.abs(phasedata[f][mDecsize-1]-phasedata[f][0])>PI/4)
                {
                    for(int i=0;i<=1;i++)
                        mDCValue[i][f]=(1-DC_TREND*2)*mDCValue[i][f]+
                                (mMinValue[i][f]+mMinValue[i][f])/2*DC_TREND*2;
                }

                //prepare linear regression
                //remove start phase
                temp_val = -phasedata[f][0];
                vDSP_vsadd(phasedata[f], 0, temp_val, tempdata, 0, mDecsize);	//tempdata[i] = phasedata[f][i] - phasedata[f][0], for i = 0...mDecsize
                //divide the constants
                temp_val=2*PI/mWaveLength[f];
                vDSP_vsdiv(tempdata, 0, temp_val, phasedata[f], 0, mDecsize);	//phasedata[f][i] = (tempdata[i] / (2*PI)) * mWaveLength[f], for i = 0...mDecsize
            }
            else //ignore the low power vectors
            {
                ignorefreq[f]=1;
            }

        }

        //linear regression
        for(int i  =0; i < mDecsize; i++)
            tempdata2[i] = i;
        double sumxy = 0;
        double sumy = 0;
        int numfreqused = 0;
        for(int f = 0; f < mNumFreqs; f++)
        {
            if(ignorefreq[f]==1)
            {
                continue;
            }

            numfreqused++;

            vDSP_vmul(phasedata[f], 0, tempdata2, 0, tempdata, 0, mDecsize);	//tempdata[i] = phasedata[f][i] * tempdata2[i], for i = 0...mDecsize
            temp_val = vDSP_sve(tempdata, 0, mDecsize);		//temp_val = tempdata[0] +...+ tempdata[i] +..., for i = 0...mDecsize
            sumxy += temp_val;
            temp_val = vDSP_sve(phasedata[f], 0, mDecsize);	//temp_val = phasedata[f][0] +...+ phasedata[f][i] +..., for i = 0...mDecsize
            sumy += temp_val;

        }
        if(numfreqused==0)
        {
            distance = 0;
            return distance;
        }

        double deltax = mNumFreqs*((mDecsize-1)*mDecsize*(2*mDecsize-1)/6-(mDecsize-1)*mDecsize*(mDecsize-1)/4);
        double delta = (sumxy-sumy*(mDecsize-1)/2.0)/deltax*mNumFreqs/numfreqused;

        double varsum = 0;
        double[] var_val = new double[MAX_NUM_FREQS];
        for(int i = 0; i < mDecsize; i++)
            tempdata2[i] = i*delta;

        //get variance of each freq;
        for(int f = 0; f < mNumFreqs; f++)
        {   var_val[f] = 0;
            if(ignorefreq[f]==1)
            {
                continue;
            }
            vDSP_vsub(tempdata2, 0, phasedata[f], 0, tempdata, 0, mDecsize);	//tempdata[i] = phasedata[f][i] - tempdata2[i], for i = 0...mDecsize
            vDSP_vsq(tempdata, 0, tempdata3, 0, mDecsize);		//tempdata3[i] = tempdata[i]^2, for i = 0...mDecsize
            var_val[f] = vDSP_sve(tempdata3, 0, mDecsize);		//var_val[f] = tempdata3[0] +...+ tempdata3[i] +..., for i = 0...mDecsize
            varsum += var_val[f];
        }
        varsum = varsum/numfreqused;
        for(int f = 0; f < mNumFreqs; f++)
        {
            if(ignorefreq[f]==1)
            {
                continue;
            }
            if(var_val[f]>varsum)
                ignorefreq[f] = 1;
        }

        //linear regression
        for(int i = 0; i < mDecsize; i++)
            tempdata2[i]=i;

        sumxy = 0;
        sumy = 0;
        numfreqused = 0;
        for(int f = 0; f < mNumFreqs; f++)
        {
            if(ignorefreq[f]==1)
            {
                continue;
            }

            numfreqused++;

            vDSP_vmul(phasedata[f], 0, tempdata2, 0, tempdata, 0, mDecsize);	//tempdata[i] = phasedata[f][i] * tempdata2[i], for i = 0...mDecsize
            temp_val = vDSP_sve(tempdata, 0, mDecsize);		//temp_val = tempdata[0] +...+ tempdata[i] +..., for i = 0...mDecsize
            sumxy += temp_val;
            temp_val = vDSP_sve(phasedata[f], 0, mDecsize);	//temp_val = phasedata[f][0] +...+ phasedata[f][i] +..., for i = 0...mDecsize
            sumy += temp_val;

        }
        if(numfreqused==0)
        {
            distance = 0;
            return distance;
        }

        delta = (sumxy-sumy*(mDecsize-1)/2.0)/deltax*mNumFreqs/numfreqused;

        distance = -delta*mDecsize/2;
        return distance;
    }

}
