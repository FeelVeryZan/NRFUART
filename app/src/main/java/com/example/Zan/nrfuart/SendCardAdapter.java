package com.example.Zan.nrfuart;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.Zan.nrfuart.R;

import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by nodgd on 2017/09/16.
 */

public class SendCardAdapter extends RecyclerView.Adapter<SendCardAdapter.ViewHolder> {

    public static String TAG = "MonitorCardAdapterTag";

    private Context mContext;
    private List<SendCardData> mDataList;
    private PopupWindow mPopupWindow;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView mCardView;
        private TextView mTitleView;
        private ImageButton mCloseButton;
        private TextView mThisId;
        private TextView mNowState;
        private Button mEditButton;
        private LineChartView mLineChartView;

        public ViewHolder(View itemView) {
            super(itemView);
            //获取各个View
            mCardView = (CardView) itemView;
            mTitleView = (TextView) itemView.findViewById(R.id.card_title);
            mCloseButton = (ImageButton) itemView.findViewById(R.id.close_card);
            mThisId = (TextView) itemView.findViewById(R.id.this_id);
            mNowState = (TextView) itemView.findViewById(R.id.now_state);
            mEditButton = (Button) itemView.findViewById(R.id.edit_state);
            mLineChartView = (LineChartView) itemView.findViewById(R.id.line_chart);
            //设置LineChartView的一些基本属性
            mLineChartView.setInteractive(true);
            mLineChartView.setZoomType(ZoomType.HORIZONTAL);
            mLineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
            mLineChartView.setFocusableInTouchMode(true);
            mLineChartView.startDataAnimation();
        }
    }

    public SendCardAdapter(List<SendCardData> dataList) {
        mDataList = dataList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.send_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (mPopupWindow == null) {
            //弹出设置窗口的属性
            View contentView = LayoutInflater.from(mContext).inflate(R.layout.send_card_edit_window, null);
            mPopupWindow = new PopupWindow(contentView);
            mPopupWindow.setFocusable(true);
            mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x20FFFFFF));
            mPopupWindow.setAnimationStyle(R.style.EditWindowAnimation);
            //监听弹出窗口的关闭按钮
            Button closeButton = (Button) contentView.findViewById(R.id.close);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPopupWindow.dismiss();
                }
            });
        }
        //把数据灌进去
        Log.d(TAG, "把数据灌进去, position=" + position);
        SendCardData data = mDataList.get(position);
        holder.mTitleView.setText(data.getTitle());
        holder.mThisId.setText(data.getIdInString());
        holder.mNowState.setText(data.getStateInString());
        holder.mLineChartView.setLineChartData(data.getLineChartData());
        //监听关闭按钮
        holder.mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeOneCard(position);
            }
        });
        //监听设置按钮
        holder.mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopupWindow.showAsDropDown(view);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    /*
     *  接口函数
     *  注意，卡片的id和卡片的编号并不是同一个东西
     */
    //在底部增加一张卡片。返回它的编号
    public int addOneCard(SendCardData data) {
        mDataList.add(data);
        notifyItemInserted(mDataList.size());
        return mDataList.size();
    }

    //删除当前的第pos张卡片，编号从0开始。返回是否成功
    public boolean removeOneCard(int pos) {
        if (pos < 0 || pos >= mDataList.size()) {
            Log.e(TAG, "错误的编号：pos=" + pos);
            return false;
        }
        mDataList.remove(pos);
        notifyItemRemoved(pos);
        return true;
    }

    //修改第pos张卡片的标题。返回是否成功
    public boolean setItemTitle(int pos, String title) {
        if (pos < 0 || pos >= mDataList.size()) {
            Log.e(TAG, "错误的编号：pos=" + pos);
            return false;
        }
        mDataList.get(pos).setTitle(title);
        notifyItemChanged(pos);
        return true;
    }

    //修改第pos张卡片显示的ID。返回是否成功
    public boolean setItemId(int pos, int id) {
        if (pos < 0 || pos >= mDataList.size()) {
            Log.e(TAG, "错误的编号：pos=" + pos);
            return false;
        }
        mDataList.get(pos).setId(id);
        notifyItemChanged(pos);
        return true;
    }
}
