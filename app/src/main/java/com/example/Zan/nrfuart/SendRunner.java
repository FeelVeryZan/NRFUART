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
    private int data[] = new int[0];
    private int Channel;
    private int id;
    public final static String DataReview = "com.example.Zan.nrfuart.reviewDataByIdentifier";
    private final int T = 10;

    public SendRunner(int a[], int channel, int Id) {
        Channel = channel;
        id = Id;
        if (a.length > 0)
            data = a.clone();
    }

    public void run() {
        for (int i = 0; i < data.length; i++) {
            DataSend(data[i], Channel);
            broadcastUpdate(DataReview, Channel);
            try {
                Thread.sleep(T);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    void DataSend(int data, int channel) {


    }

    private void broadcastUpdate(final String action, int id) {
        final Intent intent = new Intent(action);
        intent.putExtra("ID", id);
        LocalBroadcastManager.getInstance(MyApplication.getContext()).sendBroadcast(intent);
    }
}
