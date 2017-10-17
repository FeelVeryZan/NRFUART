/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.hh.nrfuart;




import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;


import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import org.achartengine.GraphicalView;
import org.achartengine.model.XYSeries;

public class MainActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;
    public final static String SaveRunner_On="SaveRunner_On";
    public final static String SaveRunner_Off="SaveRunner_Off";

    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    //private Button btnConnectDisconnect,btnSend;
    private EditText edtMessage;

    /*
    HH
     */
    public static Chart mChart = null;
/*
    public static Button Chart_Suspend = null;
    public static Button loadButton = null;
    //public static Button Settings_Button = null;
    public static Button Saverunner_Suspend = null;
    public static Button Saverunner_Stop = null;
*/
    public static NavigationView navView;

    private PipedInputStream in = new PipedInputStream();
    private PipedOutputStream out = new PipedOutputStream();

    private final SaveRunner save = new SaveRunner(in);
    private final Thread saveThread = new Thread(save);
    private final Context context = this;

    private String filepath = null;

    public static int channelnum = 4;

    private static int channel = 0;
    private static int count = 0;
    private static String saveline = "";
    private static String Stop="o";
    private static String Start="a";
    private static String Suspend="u";
    private static String Resume="r";
    private static int NoRespCo=Color.rgb(0x9f,0x9f,0x9f);
    private static int RespCo=Color.BLACK;
    private Timer timer = new Timer();
    private TimerTask task = null;

    private boolean isRun = true;
    private boolean isPaint = true;
    private boolean isPlot = true;
    private boolean isRecord = true;

    public static final int MESSAGE_CHART_UPDATE = 13;
    public static final int MSG_LOAD = 14;
    public static final int MSG_SUSPEND = 15;
    public static final int MSG_RESUME = 16;
    private static final int FILE_SELECTT_CODE = 17;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null)
        {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e){
                e.printStackTrace();
            }

            NoBTDeviceAlertDialogFragment noBTDeviceAlertDialogFragment = new NoBTDeviceAlertDialogFragment();
            noBTDeviceAlertDialogFragment.show(getFragmentManager(), "NoBT");

        }
        //messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        //messageListView.setAdapter(listAdapter);
        //messageListView.setDivider(null);

        service_init();


        //Channel number Preference
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String chnstr = sp.getString("channel_number", "4");
        channelnum = Integer.parseInt(chnstr);



        //Original Chart
        /*
        mChart = new Chart(this, Chart.LINE_CHART, channelnum);
        mChart.setXYMultipleSeriesDataset("curve");
        mChart.setXYMultipleSeriesRenderer(0, 100, -1000, 7000, "Signal Strength", "x", "y",
                Color.BLACK, Color.BLACK, Color.RED, Color.BLACK);
        GraphicalView chartview = mChart.getGraphicalView();
        LinearLayout layout = (LinearLayout)findViewById(R.id.chart);

        WindowManager wm = this.getWindowManager();
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        Log.d(TAG, point.x + " " + point.y);
        layout.addView(chartview, new ViewGroup.LayoutParams(point.x - 50, point.y - 900));

    */

        //
        try {
            in.connect(out);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        saveThread.start();

        //navView
        navView=(NavigationView)findViewById(R.id.nav_view);

        navView.setCheckedItem(R.id.nav_About);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){

            @Override
            public boolean onNavigationItemSelected(MenuItem Item){
                switch (Item.getItemId())
                {
                    case R.id.nav_Setting:
                        SettingsActivity.activitystart(MainActivity.this);
                        break;
                }
                return true;
            }
        });



        //Buttons Initializing

        //Buttons ends;




        timer.schedule(task, 0, 40);
        testProps();



        // Handler Disconnect & Connect button

        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                	//if (btnConnectDisconnect.getText().equals("Connect")){
                    if(btnConnectDisconnect.getText().equals("Connect")) {
                        //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices

                        Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);

                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);


                    } else {
        				//Disconnect button pressed
        				if (mDevice!=null)
        				{
                            try {
                                out.write(Stop.getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            while(save.getState() != SaveRunner.State.STOP)
                            {
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e){
                                    e.printStackTrace();
                                }
                            }


                            mService.disconnect();

        				}
        			}
                }
            }
        });



        // Set initial UI state

    }//onCreate ends here

    
    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
        		mService = ((UartService.LocalBinder) rawBinder).getService();
        		Log.d(TAG, "onServiceConnected mService= " + mService);
        		if (!mService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }

        }

        public void onServiceDisconnected(ComponentName classname) {
       ////     mService.disconnect(mDevice);
        		mService = null;
        }
    };


    //Preference Setting Saving/Loading (using file)
    private void testProps() {
        Properties props = Util.loadConfig(context, "/sdcard/HMILab/config.properties");
        if (props == null) {
            props = new Properties();
            props.put("channelnum", "4");
            props.put("XAxisMax", "1000");
            props.put("XAxisMin", "0");
            props.put("Shift", "2000");
            Util.saveConfig(context, "/sdcard/HMILab/config.properties", props);
        }
        int chnum = Integer.parseInt((String)props.get("channelnum"));
        double max = Double.parseDouble((String)props.get("XAxisMax"));
        double min = Double.parseDouble((String)props.get("XAxisMin"));
        double shift = Double.parseDouble((String)props.get("Shift"));
        //Toast.makeText(context, ""+chnum+" "+max+" "+min+" "+shift, Toast.LENGTH_SHORT).show();


    }

    //Original Chart Message Handler

    private Handler mHandler = new Handler() {
        @Override
        //Handler events that received from UART service 
        public void handleMessage(Message msg) {
            /*switch (msg.what) {

                case MESSAGE_CHART_UPDATE:
                    if (isPaint) {
                        if (mChart.getItemCount() > Chart.MAX_SIZE) {
                            mChart.reset("curve");
                            count = 0;//send.reset();
                        }
                        mChart.update();
                    }
                    break;
                case MSG_SUSPEND:
                    isPlot = false;//send.isPlot = false;
                    isPaint = false;
                    isRun = false;
                    //Chart_Suspend.setText("ResumeChart");
                    Log.d(TAG, "suspend");
                    break;
                case MSG_RESUME:
                    isPlot = true;//send.isPlot = true;
                    isPaint = true;
                    count = 0;//send.reset();
                    isRun = true;
                    //Chart_Suspend.setText("SuspendChart");
                    mChart.reset("curve");
                    mChart.setChartTitle("Signal Strength");
                    mChart.repaint();
                    Log.d(TAG, "resume");
                    break;
                case MSG_LOAD:
                    mChart.reset("curve");
                    XYSeries[] mSeries = (XYSeries[])msg.obj;
                    mChart.add(mSeries);
                    mChart.setChartTitle(filepath);
                    mChart.setXAxis(mSeries[0].getItemCount()-1000, mSeries[0].getItemCount());
                    mChart.setYAxis(0, 8000);
                    mChart.repaint();
                    Toast.makeText(getApplicationContext(), "load file successfully!", Toast.LENGTH_LONG).show();
                    Log.d(TAG,"load");
                    break;
            } */

        }
    };



    //Broadcast Receiver
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED))
            {
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        //btnConnectDisconnect.setText("Disconnect");
                        //Chart_Suspend.setTextColor(RespCo);
                        //Saverunner_Suspend.setTextColor(RespCo);
                        ///Saverunner_Stop.setTextColor(RespCo);
                        //loadButton.setTextColor(NoRespCo);
                        edtMessage.setEnabled(true);
                        //btnSend.setEnabled(true);
                        //((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - ready");
                        listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        mState = UART_PROFILE_CONNECTED;
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        //save.shutdown();
                        //btnConnectDisconnect.setText("Connect");
                        //Chart_Suspend.setTextColor(NoRespCo);
                        //Saverunner_Suspend.setTextColor(NoRespCo);
                        //Saverunner_Stop.setTextColor(NoRespCo);
                        //loadButton.setTextColor(RespCo);
                        edtMessage.setEnabled(false);
                        //btnSend.setEnabled(false);
                        //((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                        listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();
                    }
                });
            }


            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            // get data from UART service
            if (action.equals(UartService.ACTION_DATA_AVAILABLE))
            {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);

                for (int i = 0; i < txValue.length; i ++) {
                    mChart.add(count , txValue[i] + channel * 2048, channel);
                    if (channel == 0) {
                        count ++;
                        saveline = "" + System.currentTimeMillis();
                    }
                    saveline = saveline + " " + txValue[i];
                    if (channel == channelnum - 1 && isRecord) {
                        try {
                            saveline = saveline + "\n";
                            Log.d(TAG, "saveline =" + saveline);
                            out.write(saveline.getBytes());
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    channel = (channel + 1) % channelnum;

                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            listAdapter.add("["+currentDateTimeString+"] RX: "+text);
                            messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);

                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            //*********************//
            //UART not supported
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
                showMessage("Device doesn't support UART. Disconnecting");
                //btnConnectDisconnect.setText("Connect");
                //Chart_Suspend.setTextColor(NoRespCo);
                //Saverunner_Suspend.setTextColor(NoRespCo);
                //Saverunner_Stop.setTextColor(NoRespCo);
                //loadButton.setTextColor(RespCo);
                mService.disconnect();
            }


        }
    };//Broadcast Receiver ends here


    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }


    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onDestroy() {
    	 super.onDestroy();
        Log.d(TAG, "onDestroy()");
        
        try {
        	LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        } 
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;
       
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        /*
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }*/
 
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

        case REQUEST_SELECT_DEVICE:
        	//When the DeviceListActivity return, with the selected device address
            if (resultCode == Activity.RESULT_OK && data != null) {
                String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
               
                Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                //((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                mService.connect(deviceAddress);


                try {
                    out.write(Start.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try
                {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }



            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Turn on Bluetooth failed. ", Toast.LENGTH_SHORT).show();
                /*View any_view = findViewById(R.id.btn_select);
                Snackbar.make(any_view, "Turn on Bluetooth failed.", Snackbar.LENGTH_SHORT)
                        .setAction("OK",new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Toast.makeText(MyApplication.getContext(), "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                            }
                        })
                .show();*/
            }
            break;
            case FILE_SELECTT_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    filepath = Util.getPath(context, uri);
                    LoadTask loadTask = new LoadTask(context);
                    try {
                        loadTask.execute(filepath);
                        loadTask.setTaskFinishListener(new LoadTask.TaskFinishListener() {

                            @Override
                            public void TaskFinish(XYSeries[] arg0, boolean arg1) {
                                // TODO Auto-generated method stub
                                if(arg1 == true) {
                                    Message msg = new Message();
                                    msg.what = MSG_LOAD;
                                    msg.obj = arg0;
                                    mHandler.sendMessage(msg);
                                }
                                Log.d(TAG, "finish:" + arg1);

                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(context, "loading file:" + filepath, Toast.LENGTH_SHORT).show();
                }
                break;
        default:
            Log.e(TAG, "wrong request code");
            break;
        }
    }//onActivityResult ends here

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
       
    }

    
    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
  
    }


    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.popup_title)
            .setMessage(R.string.popup_message)
            .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
   	                finish();
                }
            })
            .setNegativeButton(R.string.popup_no, null)
            .show();
        }
    }
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}




