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

package com.example.Crofun.Hmilab;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Restructured by nodgd on 2017/09/19.
 */

public class MainActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    public static final String TAG = "OldVersionMainActivity";

    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;


    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    public final static String SaveRunner_Off = "SaveRunner_off";
    public final static String EOF = "eof";

    public static int SaveRunnerCounter = 0;


    public static int[] Channel_Save = new int[1024];
    public static int[] Channel_Chart = new int[1024];
    //数据存储控制

    private TextView mRemoteRssiVal;
    private RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBluetoothtAdapter = null;
    private ListView mMessageListView;
    private ArrayAdapter<String> mMessageAdapter;
    private Button mConnectBtn, mSendBtn;
    private EditText mSendEditText;

    //public static Button Settings_Button = null;
    public Button mStopSavingBtn = null;

    public NavigationView mNavView;

    private PipedInputStream in = new PipedInputStream();
    private PipedOutputStream out = new PipedOutputStream();


    private SaveRunner save2;
    private Thread saveThread2;

    private final Context context = this;

    private String filepath = null;

    public static int channelnum = 4;

    private static int channel = 0;
    private static int count = 0;
    private static String saveline = "";
    private static String Stop = "o";
    private static String Start = "a";
    private static String Suspend = "u";
    private static String Resume = "r";
    private static int NoRespCo = Color.rgb(0x9f, 0x9f, 0x9f);
    private static int RespCo = Color.BLACK;
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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        //侧滑菜单部分
        initNavView();
        //蓝牙连接模块适配器
        initBluetoothAdapter();
        //连接蓝牙的按钮部分
        initConnectBtn();
        //停止存储按钮部分
        initStopSavingBtn();
        //显示图像部分
        initMessageList();
        //发送信息的部分
        initSend();
        //打开主线程与存储线程之间传输数据的通道
        initSavingPipe();
    }

    //侧滑菜单部分
    private void initNavView() {
        mNavView = (NavigationView) findViewById(R.id.nav_view);
        mNavView.setCheckedItem(R.id.nav_About);
        //给侧滑菜单里面每个选项设置事件
        mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem Item) {
                switch (Item.getItemId()) {
                    case R.id.nav_Setting:
                        SettingsActivity.activitystart(MainActivity.this);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    //蓝牙部分
    private void initBluetoothAdapter() {
        mBluetoothtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            NoBTDeviceAlertDialogFragment noBTDeviceAlertDialogFragment = new NoBTDeviceAlertDialogFragment();
            noBTDeviceAlertDialogFragment.show(getFragmentManager(), "NoBT");
        }
    }

    //Connect按钮
    private void initConnectBtn() {
        mConnectBtn = (Button) findViewById(R.id.btn_select);
        mConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothtAdapter == null || !mBluetoothtAdapter.isEnabled()) {
                    //如果蓝牙模块不可用
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    //如果尚未连接，就启动蓝牙设备选择界面
                    if (mConnectBtn.getText().equals("Connect")) {
                        Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    } else {
                        //如果蓝牙已连接，就让存储线程停下来，然后把服务停下来
                        if (mDevice != null) {
                            //先让存储线程停下来
                            String intentAction;
                            intentAction = SaveRunner_Off;
                            broadcastUpdate(intentAction);
                            mStopSavingBtn.setText("StartSaving");
                            mService.disconnect();
                        }
                    }
                }
            }
        });
        //长按进入新版蓝牙设备选择界面
        mConnectBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //如果尚未连接，就启动新版蓝牙设备选择界面
                if (mConnectBtn.getText().equals("Connect")) {
                    NewDeviceChoosingWindow window = new NewDeviceChoosingWindow(MainActivity.this);
                    window.show();
                } else {
                    //如果蓝牙已连接，就让存储线程停下来，然后把服务停下来
                    if (mDevice != null) {
                        //先让存储线程停下来
                        String intentAction;
                        intentAction = SaveRunner_Off;
                        broadcastUpdate(intentAction);
                        mStopSavingBtn.setText("StartSaving");
                        mService.disconnect();
                    }
                }
                return true;
            }
        });
    }

    //StopSaving按钮
    private void initStopSavingBtn() {
        mStopSavingBtn = (Button) findViewById(R.id.SaveRunnerStop);
        mStopSavingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnectBtn.getText().equals("Connect")) {
                    //如果现在处于没有连接到设备的状态，提示一个Toast
                    Toast.makeText(getApplicationContext(), "Unavailable Option.", Toast.LENGTH_SHORT).show();
                } else if (mStopSavingBtn.getText().equals("StartSaving")) {
                    //如果当前处于暂停状态，就继续
                    int aa[] = new int[4];
                    aa[0] = 1;
                    aa[1] = 1;
                    aa[2] = 1;
                    aa[3] = 0;
                    Log.e(TAG, "onClick: try to startSendThread");
                    save2 = new SaveRunner(SaveRunnerCounter, channelnum, aa);
                    saveThread2 = new Thread(save2);
                    saveThread2.start();
                    mStopSavingBtn.setText("StopSaveing");

                } else {
                    //如果当前处于运行状态，就进入暂停
                    broadcastUpdate(SaveRunner_Off, String.valueOf(0));
                    mStopSavingBtn.setText("StartSaving");
                }
            }
        });
    }

    //显示图像部分
    private void initMessageList() {
        mMessageListView = (ListView) findViewById(R.id.listMessage);
        mMessageAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        mMessageListView.setAdapter(mMessageAdapter);
        mMessageListView.setDivider(null);
        //从ShardPrefences里面获取通道数
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String chnstr = sp.getString("channel_number", "4");
        channelnum = Integer.parseInt(chnstr);
        //启动服务？？
        service_init();
    }

    //发送信息部分
    private void initSend() {
        mSendBtn = (Button) findViewById(R.id.sendButton);
        mSendEditText = (EditText) findViewById(R.id.sendText);
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mSendEditText.getText().toString();
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    mMessageAdapter.add("[" + currentDateTimeString + "] TX: " + message);
                    mMessageListView.smoothScrollToPosition(mMessageAdapter.getCount() - 1);
                    mSendEditText.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
    }

    //主线程与存储线程之间的通道
    private void initSavingPipe() {
        try {
            in.connect(out);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };


    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        mConnectBtn.setText("Disconnect");
                        mStopSavingBtn.setTextColor(RespCo);
                        mSendEditText.setEnabled(true);
                        mSendBtn.setEnabled(true);
                        ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - ready");
                        mMessageAdapter.add("[" + currentDateTimeString + "] Connected to: " + mDevice.getName());
                        mMessageListView.smoothScrollToPosition(mMessageAdapter.getCount() - 1);
                        mState = UART_PROFILE_CONNECTED;
                    }
                });
            }

            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        //save.stopSaveThread();
                        mConnectBtn.setText("Connect");
                        mStopSavingBtn.setTextColor(NoRespCo);
                        mSendEditText.setEnabled(false);
                        mSendBtn.setEnabled(false);
                        ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                        mMessageAdapter.add("[" + currentDateTimeString + "] Disconnected to: " + mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();
                    }
                });
            }

            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }

            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);

                for (int i = 0; i < txValue.length; i++) {
                    //下次在这儿加点
                    if (channel == 0) {
                        //count ++;
                        saveline = "" + System.currentTimeMillis() + ' ';
                    }


                    if (Channel_Save[i] == 1) {
                        saveline = saveline + " " + txValue[i];
                    }
                    //存储控制

                    if (channel == channelnum - 1 && isRecord) {
                         /*try {
                             saveline = saveline + "\n";
                             Log.d(TAG, "saveline =" + saveline);
                             ss1.out.write(saveline.getBytes());
                         } catch (IOException e) {
                             // TODO Auto-generated catch block
                             e.printStackTrace();
                         }*/
                    }
                    channel = (channel + 1) % channelnum;

                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            mMessageAdapter.add("[" + currentDateTimeString + "] RX: " + text);
                            mMessageListView.smoothScrollToPosition(mMessageAdapter.getCount() - 1);

                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }

            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                showMessage("Device doesn't support UART. Disconnecting");
                mConnectBtn.setText("Connect");
                mStopSavingBtn.setTextColor(NoRespCo);
                mService.disconnect();
            }


        }
    };

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
        mService = null;

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
                    ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - connecting");
                    mService.connect(deviceAddress);

                /*try {
                    out.write(startSaveThread.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try
                {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
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
                }
                break;

            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

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
        finish();
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, String name) {
        final Intent intent = new Intent(action);
        intent.putExtra("name", name);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
