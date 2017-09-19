package com.example.Zan.nrfuart;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import java.util.List;

/**
 * Created by nodgd on 2017/09/19.
 */

public class NewDeviceChoosingWindow {

    public static String TAG = "NewDeviceChoosingWindow";

    private Context mContext;

    //界面部分
    private View mContentView;
    private PopupWindow mPopupWindow;
    private RecyclerView mDeviceRecView;
    private StaggeredGridLayoutManager mDeviceLayoutManager;
    private NewDeviceAdapter mDeviceAdapter;

    //蓝牙部分
    private BluetoothAdapter mBluetoothAdapter;
    private List<BluetoothDevice> mDeviceList;
    private boolean isScanning = false;

    public static interface GoDismissListenter {
        public void onDismiss();
    }

    public NewDeviceChoosingWindow(Context context) {
        mContext = context;
        //设置弹出窗口的基本属性
        initPopupWindow();
        //处理设备列表
        initDeviceList();
        //关闭事件的监听
        initCloseEvent();
    }

    //设置弹出窗口的基本属性
    private void initPopupWindow() {
        mContentView = LayoutInflater.from(mContext).inflate(R.layout.new_device_window, null);
        mPopupWindow = new PopupWindow(mContentView);
        //mPopupWindow.setWidth((int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.7));
        //mPopupWindow.setHeight((int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.7));
        mPopupWindow.setWidth(RecyclerView.LayoutParams.MATCH_PARENT);
        mPopupWindow.setHeight(RecyclerView.LayoutParams.MATCH_PARENT);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setContentView(mContentView);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mPopupWindow.setAnimationStyle(R.style.DeviceWindowAnimation);
    }

    //处理设备列表
    private void initDeviceList() {
        mDeviceRecView = (RecyclerView) mContentView.findViewById(R.id.new_device_list);
        mDeviceLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mDeviceRecView.setLayoutManager(mDeviceLayoutManager);
        mDeviceAdapter = new NewDeviceAdapter(new GoDismissListenter() {
            @Override
            public void onDismiss() {
                mPopupWindow.dismiss();
            }
        });
        mDeviceRecView.setAdapter(mDeviceAdapter);
    }

    //监听关闭事件
    private void initCloseEvent() {
        mContentView.findViewById(R.id.new_device_window).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });
        mContentView.findViewById(R.id.new_device_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });
    }

    //正式弹出窗口
    public void show() {
        //背景高斯模糊
        Bitmap bmp1 = BlurBitmap.printScreen(mContext);
        Bitmap bmp2 = BlurBitmap.blurBitmap(mContext, bmp1, 15.0f);
        mContentView.findViewById(R.id.new_device_window).setBackgroundDrawable(new BitmapDrawable(bmp2));
        mPopupWindow.showAtLocation(((Activity)mContext).getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        //再开一个线程加载蓝牙设备。注意一定要在弹出窗口之后才开始加载，不然会产生多线程专属爆炸
        findDevice();
    }

    private void findDevice() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null || !mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return;
        }
        //新开个线程扫描蓝牙设备
        new Thread(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.startLeScan(new BluetoothAdapter.LeScanCallback() {
                    @Override
                    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                        mDeviceAdapter.addDevice(device);
                    }
                });
            }
        }).start();
    }
}
