package com.example.Crofun.Hmilab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Administrator on 2017/10/7.
 */

public class DataTransport implements Runnable {
    public final static String DataTransport="com.example.Zan.nrfuart.DataTransport";
    final int a[]={0,0,1,1,2,2,3,3};
    int data[]=new int[8];
    int last_data[]=new int[8];
    int count=0;


    public void run(){
        service_init();
        while (true){}
    }

    private void add(byte dd){
        while (a[count]!=(dd>>6)){
            int ss=(count/2)*2;
            data[ss]=last_data[ss];
            data[ss+1]=last_data[ss+1];
            count=(count+1)%8;
            if (count==0)
            {
                int[] datasend=new int[4];
                for (int i=0;i<4;i++)
                    datasend[i]=data[2*i]<<6+data[2*i+1];
                Intent intent=new Intent(DataTransport);
                intent.putExtra("Data",datasend);
                LocalBroadcastManager.getInstance(MyApplication.getContext()).sendBroadcast(intent);
                last_data=data.clone();
            }
        }
        data[count]=dd % 64;
        count=(count+1)%8;
        if (count==0)
        {
            int[] datasend=new int[4];
            for (int i=0;i<4;i++)
                datasend[i]=data[2*i]<<6+data[2*i+1];
            Intent intent=new Intent(DataTransport);
            intent.putExtra("Data",datasend);
            LocalBroadcastManager.getInstance(MyApplication.getContext()).sendBroadcast(intent);
            last_data=data.clone();
        }
    }

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final byte[] value=intent.getByteArrayExtra(UartService.EXTRA_DATA);
            for (int i=0;i<data.length;i++)
                add(value[i]);


        }
    };

    private void service_init() {
        LocalBroadcastManager.getInstance(MyApplication.getContext()).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
