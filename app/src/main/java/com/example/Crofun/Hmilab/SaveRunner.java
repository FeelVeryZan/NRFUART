package com.example.Crofun.Hmilab;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SaveRunner implements Runnable {
    public final static String SaveRunner_Off = "com.example.Zan.nrfuart.SaveRunner_off";
    private static final String TAG = "SaveRunner";
    private static final long MAX_LENGTH = 1000000;
    public String name;
    public String channellist;
    public String channellist2;
    public int ChannelList[];
    public int channelnum;
    private int channel;
    private String saveline;

    enum State {
        STOP, RUN
    }

    private volatile State state = State.RUN;
    private byte[] buffer = new byte[1024];
    private RandomAccessFile raf = null;
    private File file = null;
    private long filelen = 0;

    SaveRunner(int SaveRunnerCounter, int Channelnum, int a[]) {
        name = String.valueOf(SaveRunnerCounter);
        channelnum = Channelnum;
        ChannelList = a;
        channellist = "";
        channellist2 = "";
        for (int i = 0; i < channelnum; i++)
            if (a[i] == 1) {
                channellist = channellist + String.valueOf(i) + ',';
                channellist2 = channellist2 + String.valueOf(i) + '_';
            }
        int Len = channellist.length();
        channellist = channellist.substring(0, Len - 1);
        channellist2 = channellist2.substring(0, Len - 1);
        channellist = channellist + '\n';

    }

    @SuppressLint({"SimpleDateFormat", "SdCardPath"})
    private void init() {

        try {
            String path;
            //path = "/sdcard";
            path=String.valueOf(android.os.Environment.getExternalStorageDirectory());
            file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
                //Log.d(TAG, "mkdir:/sdcard");
                Log.d(TAG, "mkdir:/"+path);
            }
            path = path+"/HMILab";
            file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
                //Log.d(TAG, "mkdir:/sdcard/HMILab");
                Log.d(TAG, "mkdir:/"+path);
            }
            path = path+"/Data";
            file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
                //Log.d(TAG, "mkdir:/sdcard/HMILab/Data");
                Log.d(TAG, "mkdir:/"+path);
            }
            if (file.exists()) {
                //file.mkdir();
                //Log.d(TAG, "mkdir:/sdcard/HMILab/Data");
                Log.d(TAG, "mkdir:/"+path);
            }

            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String strDate = formatter.format(date);
            String filename = path + "/data_" + strDate + '_' + channellist2 + ".txt";
            //Log.e(TAG, "init: trytocreat"+filename);
            file = new File(filename);
            if (!file.exists()) {
                file.createNewFile();
                Log.d(TAG, "create file:" + filename);
            }
            raf = new RandomAccessFile(file, "rw");
            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
            String SPi = SP.getString("channel_number", "4");
            //channelNumber=Integer.getInteger(SPi);
            //raf.writeChars(SPi+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED))
            Log.d(TAG, "run: Sdcard:"+android.os.Environment.getExternalStorageDirectory());
        File ff=new File(String.valueOf(android.os.Environment.getExternalStorageDirectory())+"/amap/data");
        if (ff.exists())
            Log.d(TAG, "run: lalala");

        service_init();
        init();
        try {
            raf.write(channellist.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (state == State.RUN) {

        }
        //Log.e(TAG, "run: STOP 123  "+name);
        try {
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "run: Stop"+channellist);
    }

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (state==State.STOP)
                return;
            String action = intent.getAction();

            if (action.equals(UartService.ACTION_DATA_AVAILABLE) && state == State.RUN) {
                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                for (int i = 0; i < txValue.length; i++) {
                    if (channel == 0) {
                        saveline = "" + System.currentTimeMillis() + ' ';
                    }
                    if (ChannelList[channel] == 1) {
                        saveline = saveline + " " + txValue[i];
                    }
                    if (channel == channelnum - 1) {
                        try {
                            saveline = saveline + "\n";
                            Log.d(TAG, "saveline =" + saveline);
                            raf.write(saveline.getBytes());
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    channel = (channel + 1) % channelnum;
                }

            } else if (action.equals(SaveRunner_Off)) {
                String NAME = intent.getStringExtra("name");
                if (NAME.equals(name))
                    state = State.STOP;
            }
            else if (action.equals(DataTransport.DataTransport) && state == State.RUN){
                final int[] intValue=intent.getIntArrayExtra("Data");
                for (int i = 0; i < intValue.length; i++) {
                    if (channel == 0) {
                        saveline = "" + System.currentTimeMillis() + ' ';
                    }
                    if (ChannelList[channel] == 1) {
                        saveline = saveline + " " + String.valueOf(intValue[i]);
                    }
                    if (channel == channelnum - 1) {
                        try {
                            saveline = saveline + "\n";
                            Log.d(TAG, "saveline =" + saveline);
                            raf.write(saveline.getBytes());
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    channel = (channel + 1) % channelnum;
                }


            }
        }
    };

    private void service_init() {
        LocalBroadcastManager.getInstance(MyApplication.getContext()).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(DataTransport.DataTransport);
        intentFilter.addAction(SaveRunner_Off);
        return intentFilter;
    }

}