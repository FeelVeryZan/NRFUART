package com.example.hh.nrfuart;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Created by biubiubiu on 2017-06-29.
 */

public class SaveCard {
    public final static String SaveRunner_Off="SaveRunner_Off";
    public static final String TAG = "nRFUART";

    private PipedInputStream in = new PipedInputStream();
    private PipedOutputStream out = new PipedOutputStream();
    private final SaveRunner save = new SaveRunner(in);
    private final Thread saveThread = new Thread(save);
    private static int ChannelNum;
    private static int channel=0;
    private static int[] ChannelFlag=new int[10];
    private static String Flag;
    private static String saveline="";

    public void Setting(int CN,int[] CF,String flag)
    {
        ChannelNum=CN;
        for (int i=0;i<CN;i++)
            ChannelFlag[i]=CF[i];
        Flag=flag;
    }

    public void start()
    {
        saveThread.start();
        String str;
        str=Flag;
        Log.d(TAG, "Flag:"+str);
        try {
            out.write(str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        str=String.valueOf(ChannelNum);
        for (int i=0;i<ChannelNum;i++)
            str=str+" "+String.valueOf(ChannelFlag[i]);
        str=str+"\n";
        Log.d(TAG, "out:"+str);
        try {
            out.write(str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void passmess(byte bb)
    {
        saveline=saveline+" "+bb;
        channel++;
        if (channel==ChannelNum)
        {
            saveline=saveline+"\n";
            Log.d(TAG, "saveline =" + saveline);
            try {
                out.write(saveline.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdown()
    {
        String intentAction;
        intentAction=SaveRunner_Off+Flag;
        broadcastUpdate(intentAction);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(MyApplication.getContext()).sendBroadcast(intent);
    }

}
