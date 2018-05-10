package com.example.Crofun.Hmilab;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by Angel on 2017/6/30.
 */


public class WorkFlow extends BaseActivity {

    private String TAG = "WorkFlow";
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;
    public final static String SaveRunner_Off = "SaveRunner_off";
    private static final int MY_PERMISSION_REQUEST_COARSE_LOCATION = 16;
    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 17;
    private static final int MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 15;

    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ImageButton mConnectBtn;
    private TextView mConnectBtnHint;
    private TextView mDeviceName;
    private FloatingActionButton mFloatingActionButton;
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private Menu mUserInfoMenu;
    private MenuItem[] mUser_info_Items = new MenuItem[3];
    private MenuItem mUser_info_title;

    private RecyclerView mSendCardRecView;
    private SendCardAdapter mSendCardAdapter;
    private RecyclerView mSaveCardRecView;
    private SaveCardAdapter mSaveCardAdapter;
    private RecyclerView mMonitorCardRecView;
    private MonitorCardAdapter mMonitorCardAdapter;

    private static int NoRespCo = Color.rgb(0x9f, 0x9f, 0x9f);
    private static int RespCo = Color.BLACK;

    private static int channel = 0;

    public static int channelNumber = 4;
    public static boolean[] channelHasSendThread = {false, false, false, false};    //这个数组大小应该和通道数相同


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.workflow);

        //悬浮按钮部分
        initFloatActionButton();
        //蓝牙部分
        initBluetooth();
        //卡片列表部分
        initCardList();
        //什么部分
        initService();
        //之前通道数的设置项
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(WorkFlow.this);
        String chnstr = sp.getString("channel_number", "4");
        channelNumber = Integer.parseInt(chnstr);

        //Ask for WRITE_EXTERNAL_STORAGE permission.
        if (!(ContextCompat.checkSelfPermission(MyApplication.getContext(),android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            Log.d(TAG, "onCreate: WRITE_EXTERNAL_STORAGE not permitted.");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        else
            Log.d(TAG, "onCreate: WRITE_EXTERNAL_STORAGE permitted.");

        //Ask for LOCATION permissions.
        if ((ContextCompat.checkSelfPermission(MyApplication.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) != PackageManager.PERMISSION_GRANTED)
        {
            if (!(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)))
            {
                Toast.makeText(this, "Location Permission Needed for Bluetooth Scan! Please Check App Settings.", Toast.LENGTH_LONG).show();
            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        if ((ContextCompat.checkSelfPermission(MyApplication.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)) != PackageManager.PERMISSION_GRANTED)
        {
            if (!(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)))
            {
                Toast.makeText(this, "Location Permission Needed for Bluetooth Scan! Please Check App Settings.", Toast.LENGTH_LONG).show();
            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
            }
        }

        //滑动菜单(NavigationView)部分
        mUser_info_title = (MenuItem) findViewById(R.id.nav_User_info_title);
        mUser_info_Items[0]= (MenuItem) findViewById(R.id.nav_User_info_a);
        mUser_info_Items[1]= (MenuItem) findViewById(R.id.nav_User_info_b);
        mUser_info_Items[2]= (MenuItem) findViewById(R.id.nav_User_info_c);


        // 一个奇怪的调试数据发送线程。
        // new Thread(new DataSource()).start();

        // 一个奇怪的数据处理线程
        new Thread(new DataTransport()).start();

    }


    //悬浮按钮部分
    private void initFloatActionButton() {
        mToolbar = (Toolbar) findViewById(R.id.add_card_toolbar);
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.FloatingButton);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mToolbar.getVisibility() == View.GONE) {
                    mToolbar.animate()
                            .alpha(1.0f)
                            .translationY(0)
                            .setDuration(400)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    mToolbar.setVisibility(View.VISIBLE);
                                    Log.d("animate()", "set mToolbar visible");
                                }
                            });
                    mFloatingActionButton.animate()
                            .alpha(0.5f)
                            .rotation(0)
                            .setDuration(300)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    mFloatingActionButton.setImageResource(R.drawable.down);
                                }
                            });
                } else {
                    mToolbar.animate()
                            .alpha(0.0f)
                            .translationY(mToolbar.getHeight())
                            .setDuration(400)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    mToolbar.setVisibility(View.GONE);
                                    Log.d("animate()", "set mToolbar gone");
                                }
                            });
                    mFloatingActionButton.animate()
                            .rotation(180)
                            .alpha(1.0f)
                            .setDuration(300)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    mFloatingActionButton.setImageResource(R.drawable.plus);
                                }
                            });
                }

            }
        });
        //悬浮按钮点开之后，三个创建卡片的按钮
        findViewById(R.id.add_save_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateCardWindow window = new CreateCardWindow(WorkFlow.this, CreateCardWindow.MODE_SAVECARD);
                window.show();
            }
        });
        findViewById(R.id.add_monitor_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateCardWindow window = new CreateCardWindow(WorkFlow.this, CreateCardWindow.MODE_MONITORCARD);
                window.show();
            }
        });
        findViewById(R.id.add_send_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateCardWindow window = new CreateCardWindow(WorkFlow.this, CreateCardWindow.MODE_SENDCARD);
                window.show();
            }
        });
    }

    //蓝牙部分
    private void initBluetooth() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();

            NoBTDeviceAlertDialogFragment noBTDeviceAlertDialogFragment = new NoBTDeviceAlertDialogFragment();
            noBTDeviceAlertDialogFragment.show(getFragmentManager(), "NoBT");


        }
        //蓝牙连接按钮
        mConnectBtn = (ImageButton) findViewById(R.id.connect_hint);
        mConnectBtnHint = (TextView) findViewById(R.id.connect_hint_text);
        mConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    //如果蓝牙未打开
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                    NewDeviceChoosingWindow window = new NewDeviceChoosingWindow(WorkFlow.this);
                    window.show();
                }
            }
        });
    }

    private void initCardList() {
        //SendCard
        mSendCardRecView = (RecyclerView) findViewById(R.id.recycler_view_send);
        mSendCardRecView.setLayoutManager(new LinearLayoutManager(WorkFlow.this));
        mSendCardAdapter = new SendCardAdapter();
        mSendCardRecView.setAdapter(mSendCardAdapter);
        //SaveCard
        mSaveCardRecView = (RecyclerView) findViewById(R.id.recycler_view_save);
        mSaveCardRecView.setLayoutManager(new LinearLayoutManager(WorkFlow.this));
        mSaveCardAdapter = new SaveCardAdapter();
        mSaveCardRecView.setAdapter(mSaveCardAdapter);
        //MonitorCard
        mMonitorCardRecView = (RecyclerView) findViewById(R.id.recycler_view_monitor);
        mMonitorCardRecView.setLayoutManager(new LinearLayoutManager(WorkFlow.this));
        mMonitorCardAdapter = new MonitorCardAdapter();
        mMonitorCardRecView.setAdapter(mMonitorCardAdapter);
    }

    //滑动菜单点击响应
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_User_info_title:
                for(int i=0; i<3; i++){
                    mUser_info_Items[i].setVisible(false);
                }
                return true;
            default :
                return super.onContextItemSelected(item);
        }
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
        //unbindService(mServiceConnection);
        //mService.stopSelf();
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

    //收广播
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(TAG, "onReceive: " + action);
            //这坨是之前的收数据，貌似废弃了？
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                for (int i = 0; i < txValue.length; i++) {
                    mMonitorCardAdapter.addMessageByChannel(channel, txValue[i]);
                    channel = (channel + 1) % channelNumber;
                }
            }
            //貌似这是现在的收数据
            else if (action.equals(DataTransport.DataTransport)){
                final int[] data=intent.getIntArrayExtra("Data");
                for (int i = 0; i < data.length; i++) {
                    //Log.d(TAG, "onReceive: send"+channel+"   "+data[i]);
                    mMonitorCardAdapter.addMessageByChannel(channel, data[i]);
                    Log.v(TAG, "addMessageByChannel("+channel+",   "+data[i]+"  channelNumber = "+channelNumber);
                    channel = (channel + 1) % channelNumber;
                }
            } else if (action.equals(CreateCardWindow.Action_CreateSaveCard)) {
                Log.d(TAG, "onReceive: " + CreateCardWindow.Action_CreateSaveCard);
                SaveCardData saveCardData = new SaveCardData();
                saveCardData.setChannelNumber(channelNumber);
                int channelList[] = intent.getIntArrayExtra("ChannelList");
                saveCardData.setChannelList(channelList);
                saveCardData.setTitle(intent.getStringExtra("Title"));
                String cardDataContent = "";
                for (int i = 0; i < channelNumber; i++) {
                    if (channelList[i] == 1) {
                        if (cardDataContent.length() != 0) {
                            cardDataContent = cardDataContent + ", ";
                        }
                        cardDataContent = cardDataContent + String.valueOf(i);
                    }
                }
                saveCardData.setContent(cardDataContent);
                mSaveCardAdapter.addOneCard(saveCardData);
                Log.d(TAG, "onReceive: Success to Creat Save");
            } else if (action.equals(CreateCardWindow.Action_CreateMonitorCard)) {
                Log.d(TAG, "onReceive: " + CreateCardWindow.Action_CreateMonitorCard);
                MonitorCardData monitorCardData = new MonitorCardData();
                //monitorCardData.setChannel(intent.getIntExtra("channel", 0));
                monitorCardData.setTitle(intent.getStringExtra("Title"));
                monitorCardData.setChannel(intent.getIntExtra("Channel", -1));
                mMonitorCardAdapter.addOneCard(monitorCardData);
                Log.d(TAG, "nodgd: " + mMonitorCardAdapter.getItemCount());
                Log.d(TAG, "onReceive: Success to Creat Monitor");
            } else if (action.equals(CreateCardWindow.Action_CreateSendCard)) {
                Log.d(TAG, "onReceive: " + CreateCardWindow.Action_CreateSendCard);
                SendCardData sendCardData = new SendCardData();
                sendCardData.setTitle(intent.getStringExtra("Title"));
                sendCardData.setChannel(intent.getIntExtra("Channel", -1));
                sendCardData.cha=intent.getIntExtra("cha",-1);
                if (sendCardData.cha!=-1){
                    sendCardData.cc[0]=intent.getDoubleExtra("cc0",-1);
                    sendCardData.cc[1]=intent.getDoubleExtra("cc1",-1);
                    sendCardData.cc[2]=intent.getDoubleExtra("cc2",-1);
                    sendCardData.cc[3]=intent.getDoubleExtra("cc3",-1);
                    sendCardData.dac_high=intent.getDoubleExtra("cmx",-1);
                    sendCardData.dac_low=intent.getDoubleExtra("cmn",-1);
                    sendCardData.crp=intent.getIntExtra("crp",1);
                }

                mSendCardAdapter.addOneCard(sendCardData);
                Log.d(TAG, "onReceive: Success to Creat Send");
            } else if (action.equals(SendRunner.DataReview)) {
                int id = intent.getIntExtra("ID", -1);
                mSendCardAdapter.reviewDataByIdentifier(id);
            } else if (action.equals(SendRunner.DataSend)){
                byte[] data=intent.getByteArrayExtra("Data");
                if (mService != null)
                    mService.writeRXCharacteristic(data);
            } else if (action.equals(SendRunner.SendRunner_Off)){
                Log.d(TAG, "onReceive: cancel zhuce"+intent.getIntExtra("channel",-1));
                channelHasSendThread[intent.getIntExtra("channel",-1)]=false;
            }

        }
    };

    private void initService() {
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        intentFilter.addAction(CreateCardWindow.Action_CreateSaveCard);
        intentFilter.addAction(CreateCardWindow.Action_CreateSendCard);
        intentFilter.addAction(CreateCardWindow.Action_CreateMonitorCard);
        intentFilter.addAction(SendRunner.DataReview);
        intentFilter.addAction(SendRunner.DataSend);
        intentFilter.addAction(DataTransport.DataTransport);
        intentFilter.addAction(SendRunner.SendRunner_Off);
        return intentFilter;
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case MY_PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Coarse_Location permitted!", Toast.LENGTH_SHORT);

                } else {
                    Toast.makeText(this, "Coarse_Location not permitted!", Toast.LENGTH_SHORT);
                }
            }
            case MY_PERMISSION_REQUEST_FINE_LOCATION:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Fine_Location permitted!", Toast.LENGTH_SHORT);

                }
                else{
                    Toast.makeText(this,"Fine_Location not permitted!", Toast.LENGTH_SHORT);
                }
            }
            case MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"External_Storage permitted!", Toast.LENGTH_SHORT);

                }
                else{
                    Toast.makeText(this,"External_Storage not permitted!", Toast.LENGTH_SHORT);
                }
            }
        }

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

                    mDeviceName = (TextView) findViewById(R.id.device_name);
                    mDeviceName.setText("Device: " + mDevice.getName());
                    mDeviceName.setVisibility(View.VISIBLE);

                    mService.connect(deviceAddress);

                } else {
                    mConnectBtn.setVisibility(View.VISIBLE);
                    mConnectBtnHint.setVisibility(View.VISIBLE);
                }
                break;

            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                    //打开蓝牙扫描弹窗activity
                    Intent newIntent = new Intent(WorkFlow.this, NewDeviceChoosingWindow.class);
                    startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);


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

    //TODO
    public void SendData(){

    }
}


