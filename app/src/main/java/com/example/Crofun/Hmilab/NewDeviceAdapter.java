package com.example.Crofun.Hmilab;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nodgd on 2017/09/19.
 */

public class NewDeviceAdapter extends RecyclerView.Adapter<NewDeviceAdapter.ViewHolder> {

    public static String TAG = "NewDeviceAdapter";

    private Context mContext;
    private ViewHolder mViewHolder;
    private NewDeviceChoosingWindow.GoDismissListenter mGoDismissListener;
    private List<BluetoothDevice> mDeviceList;
    private int mDeviceCnt;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout mLinearLayout;
        private TextView mDeviceNameText;
        private ImageView mDeviceGogogo;

        public ViewHolder(View view) {
            super(view);
            mLinearLayout = (LinearLayout) view;
            mDeviceNameText = (TextView) view.findViewById(R.id.device_name);
            mDeviceGogogo = (ImageView) view.findViewById(R.id.device_gogogo);
        }
    }

    public NewDeviceAdapter(NewDeviceChoosingWindow.GoDismissListenter listener) {
        mGoDismissListener = listener;
        mDeviceList = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            mDeviceList.add(null);
        }
        mDeviceCnt = 0;
    }

    //重写添加函数，使界面上自动保持行数
    public void addDevice(BluetoothDevice device) {
        if (mDeviceCnt < mDeviceList.size()) {
            mDeviceList.set(mDeviceCnt, device);
            mDeviceCnt++;
            notifyItemChanged(mDeviceCnt);
        } else {
            mDeviceList.add(device);
            mDeviceCnt++;
            notifyItemInserted(mDeviceList.size());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.new_device_item, parent, false);
        mViewHolder = new ViewHolder(view);
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int pos) {
        BluetoothDevice device = mDeviceList.get(pos);
        if (device != null) {
            holder.mDeviceNameText.setText(device.getName());
        } else {
            holder.mDeviceGogogo.setVisibility(View.GONE);
        }
        holder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //连接到设备
                BluetoothDevice device = mDeviceList.get(pos);
                if (device == null) {
                    Toast.makeText(mContext, "这是个假的设备", Toast.LENGTH_SHORT).show();
                    mGoDismissListener.onDismiss();
                } else {
                    Toast.makeText(mContext, "连接蓝牙：" + device.getName() + "\n然后：" + device.getAddress(), Toast.LENGTH_SHORT).show();
                    Bundle b = new Bundle();
                    b.putString(BluetoothDevice.EXTRA_DEVICE, device.getAddress());
                    mGoDismissListener.onDismiss();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }
}
