package com.example.Zan.nrfuart;

/**
 * Created by biubiubiu on 2017-09-19.
 */

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SendRunner implements Runnable {
    private String Sendhead = "73 74 69";
    private String Sendtail = " 65 6E 64";
    private String[] chopen = {" 20", " 60", " A0", " E0"};
    private String[] chclose = {" 00", " 40", " 80", " C0"};

    private int data[] = new int[0];
    private int Channel;
    private int id;
    private int state=1;
    private int newdata=0;
    private double[] cc=new double[4];
    private double DAC_High;
    private double DAC_LOW;
    private int times;
    public final static String DataReview = "com.example.Zan.nrfuart.reviewDataByIdentifier";
    public final static String DataSend="com.example.Zan.nrfuart.DataSend";
    public final static String NewData="com.example.Zan.nrfuart.NewData";
    public final static String SendRunner_Off="com.example.Zan.nrfuart.SendRunnerOff";
    private final int T = 10;
    private String TAG;

    public SendRunner(int a[], int channel, int Id) {
        Channel = channel;
        TAG="SendRunner_"+String.valueOf(Channel);
        id = Id;
        if (a.length > 0)
            data = a.clone();
    }
    public SendRunner(int a[], int channel, int Id, double[] CC,double dac_high,double dac_low,int crp) {
        Channel = channel;
        TAG="SendRunner_"+String.valueOf(Channel);
        id = Id;
        cc=CC.clone();
        DAC_High=dac_high;
        DAC_LOW=dac_low;
        times=crp;
        newdata=1;
        Log.d(TAG, "onReceive: new data1 ");
        if (a.length > 0)
            data = a.clone();
    }

    public void run() {
        Log.d(TAG, "run: start");
        service_init();
        while (state==1){
            if (newdata==1){
                String message = "";
                Log.d(TAG, "run: data="+cc[0]+"  "+cc[1]+"  "+cc[2]+"  "+cc[3]+"  "+DAC_High+"  "+DAC_LOW+"  "+times);
                message += Sendhead;
                message += " 20";
                message += chopen[Channel];
                String dacstr[] = {"1", "5", "9", "D"};
                message += (" " + dacstr[Channel] + String.format("%02X", (int) (128 + DAC_High)) + "0");
                message += (" " + dacstr[Channel] + String.format("%02X", (int) (128 + DAC_LOW)) + "0");
                message += String.format(" %08X", (int) (4000 * cc[0]));
                message += String.format(" %08X", (int) (4000 * cc[1]));
                message += String.format(" %08X", (int) (4000 * cc[2]));
                message += String.format(" %08X", (int) (4000 * cc[3]));
                message += Sendtail;
                Log.d(TAG, "hexstring = " + message);
                byte[] value = Util.hexStr2byte(message);
                Log.d(TAG,"value length="+value.length);
                String sss="";
                for (int i=0;i<value.length;i++){
                    sss=sss+value[i]+"  ";
                }
                Log.d(TAG, "run: value:"+sss);
                byte[] valuetemp = new byte[20];
                for (int i = 0; i < (value.length / 20 + 1); i ++) {
                    for (int j = 0; j < 20; j ++) {
                        if (i * 20 + j < value.length) {
                            valuetemp[j] = value[i * 20 + j];
                        } else {
                            valuetemp[j] = 0x00;
                        }
                    }
                    final Intent intent =new Intent(DataSend);
                    intent.putExtra("Data",valuetemp);
                    LocalBroadcastManager.getInstance(MyApplication.getContext()).sendBroadcast(intent);
                    try {
                        Thread.currentThread().sleep(200);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                newdata=0;
            }
        }
        Log.d(TAG, "run: stop");
    }

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (state==0)
                return;
            String action = intent.getAction();
            Log.d(TAG, "onReceive:walll "+action);
            if (action.equals(SendRunner.NewData)&& intent.getIntExtra("cha",-1)==Channel){
                Log.d(TAG, "onReceive: new data2 ");
                cc[0]=intent.getDoubleExtra("cc0",0);
                cc[1]=intent.getDoubleExtra("cc1",0);
                cc[2]=intent.getDoubleExtra("cc2",0);
                cc[3]=intent.getDoubleExtra("cc3",0);
                DAC_High=intent.getDoubleExtra("cmx",0);
                DAC_LOW=intent.getDoubleExtra("cmn",0);
                times=intent.getIntExtra("crp",0);
                newdata=1;
            }
            else if (action.equals(SendRunner.SendRunner_Off)){
                Log.d(TAG, "onReceive: try to change"+intent.getIntExtra("channel",-1));
                if (intent.getIntExtra("channel",-1)==Channel){
                    Log.d(TAG, "onReceive: change");
                    state=0;
                }
            }


        }
    };

    private void service_init() {
        LocalBroadcastManager.getInstance(MyApplication.getContext()).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SendRunner.NewData);
        intentFilter.addAction(SendRunner.SendRunner_Off);
        return intentFilter;
    }
}
