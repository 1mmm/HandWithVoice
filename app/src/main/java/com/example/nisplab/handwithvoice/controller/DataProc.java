package com.example.nisplab.handwithvoice.controller;

/**
 * Created by OMT on 2017/6/17.
 * RangeFinder中使用到的数据处理函数
 */

public class DataProc {

    public static void vDSP_vmul(double[] A, int startA, double[] B, int startB, double[] C, int startC, int n) {
        for (int x = 0; x < n; x++)
        {
            C[startC+x] = A[startA+x] * B[startB+x];
        }
    }

    public static void vDSP_vsub(double[] A, int startA, double[] B, int startB, double[] C, int startC, int n) {
        for (int x = 0; x < n; x++)
        {
            C[startC+x] = A[startA+x] - B[startB+x];
        }
    }

    public static void vDSP_vsq(double[] A, int i, double[] C, int j, int n) {
        for (int x = 0; x < n; x++)
        {
            C[j+x] = A[i+x] * A[i+x];
        }
    }

    public static double vDSP_sve(double[] A, int i, int n) {
        double C = 0;
        for (int x = 0; x < n; x++)
        {
            C += A[x+i];
        }
        return C;
    }

    public static void vDSP_vswsum(double[] A, int i, double[] C, int j, int n, int p) {
        for (int x = 0; x < n; x++)
        {
            C[j+x] = 0;
            for (int y = 0; y < p; y++)
            {
                C[j+x] += A[i+x+y];
            }
        }
    }

    public static double vDSP_maxv(double[] A, int i, int n) {
        double C = A[i];
        for (int x = 1; x < n; x++) {
            if (A[i+x] > C)
                C = A[i+x];
        }
        return C;
    }

    public static double vDSP_minv(double[] A, int i, int n) {
        double C = A[i];
        for (int x = 1; x < n; x++) {
            if (A[i+x] < C)
                C = A[i+x];
        }
        return C;
    }

    public static void vDSP_vsadd(double[] A, int i, double B, double[] C, int j, int n) {
        for (int x = 0; x < n; x++)
        {
            C[j+x] = A[i+x] + B;
        }
    }

    public static void vDSP_vsdiv(double[] A, int i, double B, double[] C, int j, int n) {
        for (int x = 0; x < n; x++)
        {
            C[j+x] = A[i+x] / B;
        }
    }

    public static void vDSP_zvmags(double[] realp, int i, double[] imagp, int j, double[] data, int k, int n) {
        for (int x = 0; x < n; x++)
        {
            data[k+x] = realp[i+x] * realp[i+x] - imagp[j+x] * imagp[j+x];
        }
    }

    public static void vDSP_zvphas(double[] realp, int i, double[] imagp, int j, double[] data, int k, int n) {
        for (int x = 0; x < n; x++)
        {
            data[k+x] = Math.atan2(imagp[j+x], realp[i+x]);
        }
    }

    public static void memmove(byte[] dest, int i, byte[] src, int j, int n) {
        System.arraycopy(src, j, dest, i, n);
    }

    public static void memmove(double[] dest, int i, double[] src, int j, int n) {
        System.arraycopy(src, j, dest, i, n);
    }

    public static void memmove(short[] dest, int i, short[] src, int j, int n) {
        System.arraycopy(src, j, dest, i, n);
    }
}
