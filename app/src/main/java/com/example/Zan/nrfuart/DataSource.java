package com.example.Zan.nrfuart;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by Administrator on 2017/10/9.
 */

public class DataSource implements Runnable {
    private String TAG = "DataSource";

    public void run() {
        int[] a = {40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51};
        int count = 0;
        while (true) {
            int[] dd = new int[4];
            String ss = "";
            for (int i = 0; i < 4; i++) {
                dd[i] = a[count + i];
                ss = ss + String.valueOf(dd[i]) + '_';
            }

            count = (count + 4) % 12;
            Intent intent = new Intent(DataTransport.DataTransport);
            intent.putExtra("Data", dd);
            Log.v(TAG, "run:senddata " + ss);
            LocalBroadcastManager.getInstance(MyApplication.getContext()).sendBroadcast(intent);
            try {
                Thread.currentThread().sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
