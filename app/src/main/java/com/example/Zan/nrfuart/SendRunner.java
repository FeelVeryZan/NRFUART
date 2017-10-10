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
    private int[] cc=new int[4];
    private int DAC_High;
    private int DAC_LOW;
    private int times;
    public final static String DataReview = "com.example.Zan.nrfuart.reviewDataByIdentifier";
    public final static String DataSend="com.example.Zan.nrfuart.DataSend";
    public final static String NewData="com.example.Zan.nrfuart.NewData";
    private final int T = 10;
    private String TAG="SendRunner_"+String.valueOf(Channel)+"_";

    public SendRunner(int a[], int channel, int Id) {
        Channel = channel;
        id = Id;
        if (a.length > 0)
            data = a.clone();
    }

    public void run() {
        while (state==1){
            if (newdata==1){
                String message = "";
                message += Sendhead;
                message += " 20";
                message += chopen[Channel];
                String dacstr[] = {"1", "5", "9", "D"};
                message += (" " + dacstr[Channel] + String.format("%02X", (int) (128 + DAC_High)) + "0");
                message += (" " + dacstr[Channel] + String.format("%02X", (int) (128 - DAC_LOW)) + "0");
                message += String.format(" %08X", (int) (4000 * cc[0]));
                message += String.format(" %08X", (int) (4000 * cc[1]));
                message += String.format(" %08X", (int) (4000 * cc[2]));
                message += String.format(" %08X", (int) (4000 * cc[3]));
                message += Sendtail;
                Log.d(TAG, "hexstring = " + message);
                byte[] value = Util.hexStr2byte(message);
                Log.d(TAG,"value length="+value.length);
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
    }

    private void broadcastUpdate(final String action, int id) {
        final Intent intent = new Intent(action);
        intent.putExtra("ID", id);
        LocalBroadcastManager.getInstance(MyApplication.getContext()).sendBroadcast(intent);
    }
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(SendRunner.NewData)&& intent.getIntExtra("cha",-1)==Channel){
                cc[0]=intent.getIntExtra("cc0",0);
                cc[1]=intent.getIntExtra("cc1",0);
                cc[2]=intent.getIntExtra("cc2",0);
                cc[3]=intent.getIntExtra("cc3",0);
                DAC_High=intent.getIntExtra("cmx",0);
                DAC_LOW=intent.getIntExtra("cmn",0);
                times=intent.getIntExtra("crp",0);
                newdata=1;
            }


        }
    };

    private void service_init() {
        LocalBroadcastManager.getInstance(MyApplication.getContext()).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SendRunner.NewData);
        return intentFilter;
    }
}
