package com.example.hh.nrfuart;

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
import java.io.PipedInputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;



public class SaveRunner implements Runnable {
    public  static String SaveRunner_Off="SaveRunner_Off";
	private static final String TAG = "SaveRunner";
	private static final long MAX_LENGTH = 1000000;
	
	enum State {
		STOP, RUN
	}
	
	private PipedInputStream in;
	private volatile State state = State.RUN;

	private byte[] buffer = new byte[1024];
	private RandomAccessFile raf = null;
	private File file = null;
	private long filelen = 0;

	SaveRunner(PipedInputStream in) {
		this.in = in;
	}


	public synchronized void shutdown()
	{
		state = State.STOP;
		Log.d("SaveRunner","Shutdown, Now State is "+state);
		try
		{
			raf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized State getState()
	{
		return state;
	}

	@SuppressLint({ "SimpleDateFormat", "SdCardPath" })
	private void init() {
		try {
			String path;
			path = "/sdcard/HMILab";
			file = new File(path);
			if (!file.exists()) {
				file.mkdir();
				Log.d(TAG, "mkdir:/sdcard/HMILab");
			}
			path = "/sdcard/HMILab/Data";
			file = new File(path);
			if (!file.exists()) {
				file.mkdir();
				Log.d(TAG, "mkdir:/sdcard/HMILab/Data");
			}
		
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			String strDate = formatter.format(date);
			String filename = "/sdcard/HMILab/Data/" + "data_" + strDate + ".txt";
			file = new File(filename);
			if (!file.exists()) {
				file.createNewFile();
				Log.d(TAG, "create file:" + filename);
			}
			raf = new RandomAccessFile(file, "rw");
			SharedPreferences SP= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
			String SPi=SP.getString("channel_number", ""+4);
			//raf.writeChars(SPi+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void run()
	{
		// TODO Auto-generated method stub

		String str = "";
		str=getmess();
		SaveRunner_Off=SaveRunner_Off+str;
		
		service_init();
		init();




		while(state==State.RUN)
		{
            str=getmess();
			if(state == State.RUN)
			{
				Log.v(TAG,"raf.write(" + str + ")");
				try {
					raf.write(str.getBytes());
				} catch(IOException e) {
					e.printStackTrace();
				}
				str="";
				filelen += 1;
				if(filelen >= MAX_LENGTH) {
					try {
						raf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					filelen = 0;
                    init();
				}
			}
		}
        try {
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(SaveRunner_Off))
			{
				state=State.STOP;
			}
		}
	};
	private void service_init() {

		LocalBroadcastManager.getInstance(MyApplication.getContext()).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
	}
	static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(SaveRunner_Off);
		return intentFilter;
	}

	private String getmess()
    {
        String str = "";
        while(str.length() == 0) {
            int len = 0;
            try {
                len = in.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (len > 0) {
                str = new String(buffer, 0, len);
                Log.d(TAG,"Have got a new string from pipe. str = " + str);
            }
        }
        return str;
    }
}