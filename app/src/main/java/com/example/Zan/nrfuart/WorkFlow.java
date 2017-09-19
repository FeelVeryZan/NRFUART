package com.example.Zan.nrfuart;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;



/**
 * Created by Angel on 2017/6/30.
 */



public class WorkFlow extends BaseActivity {
    private SaveCardAdapter saveCardAdapter;
    private MonitorCardAdapter monitorCardAdapter;
    private SendCardAdapter sendCardAdapter;

    private String TAG="WorkFlow";
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;
    public final static String SaveRunner_Off="SaveRunner_off";

    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnSend;
    private ImageButton btnConnect;
    private EditText edtMessage;

    public static NavigationView navView;

    private final Context context = this;

    private static int NoRespCo= Color.rgb(0x9f,0x9f,0x9f);
    private static int RespCo=Color.BLACK;

    private static int channel=0;

    public static int channelnum=4;





    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.workflow);





        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null)
        {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();

            NoBTDeviceAlertDialogFragment noBTDeviceAlertDialogFragment = new NoBTDeviceAlertDialogFragment();
            noBTDeviceAlertDialogFragment.show(getFragmentManager(), "NoBT");


        }
        btnConnect=(ImageButton) findViewById(R.id.connect_hint);


        service_init();

        //之前通道数的设置项
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String chnstr = sp.getString("channel_number", "4");
        channelnum = Integer.parseInt(chnstr);


        //貌似是画图用的？
        //WindowManager wm = this.getWindowManager();
        //Point point = new Point();
        //wm.getDefaultDisplay().getSize(point);
        //Log.d(TAG, point.x + " " + point.y);

        //mNavView=(NavigationView)findViewById(R.id.nav_view);

        //mNavView.setCheckedItem(R.id.nav_About);
        //mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
/*
            @Override
            public boolean onNavigationItemSelected(MenuItem Item){
                switch (Item.getItemId())
                {
                    case R.id.nav_Setting:
                        SettingsActivity.activitystart(WorkFlow.this);
                        break;
                }
                return true;
            }
        });
*/
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {

                    //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices

                    Intent newIntent = new Intent(WorkFlow.this, DeviceListActivity.class);
                    startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);


                     //else {
                        //Disconnect button pressed
                        //if (mDevice!=null)
                        //{
                            //修改为删除所有卡片，未完成
                         //   mService.disconnect();

                        //}
                    }
                }

        });

    }
/*
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

*/
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

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action==UartService.ACTION_DATA_AVAILABLE){
                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                for (int i=0;i<txValue.length;i++){
                    monitorCardAdapter.adddata(channel,txValue[i]);
                    channel=channel+1;
                }


            }

        }
    };

    private void service_init() {
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

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    private void broadcastUpdate(final String action,String name) {
        final Intent intent = new Intent(action);
        intent.putExtra("name",name);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}
