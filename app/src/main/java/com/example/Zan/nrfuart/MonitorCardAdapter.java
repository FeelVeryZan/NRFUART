package com.example.Zan.nrfuart;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by nodgd on 2017/09/16.
 */

public class MonitorCardAdapter extends RecyclerView.Adapter<MonitorCardAdapter.ViewHolder> {

    public static String TAG = "MonitorCardAdapterTag";

    private Context mContext;
    private List<MonitorCardData> mDataList;

    static class ViewHolder extends RecyclerView.ViewHolder {

        private CardView mCardView;
        private TextView mTitleView;
        private ImageButton mCloseButton;
        private EditText mEditIDView;
        private Button mJumpToButton;
        private LineChartView mLineChartView;

        public ViewHolder(View itemView) {
            super(itemView);
            //获取各个View
            mCardView = (CardView) itemView;
            mTitleView = (TextView) itemView.findViewById(R.id.card_title);
            mCloseButton = (ImageButton) itemView.findViewById(R.id.close_card);
            mEditIDView = (EditText) itemView.findViewById(R.id.id_editer);
            mJumpToButton = (Button) itemView.findViewById(R.id.jump_to);
            mLineChartView = (LineChartView) itemView.findViewById(R.id.line_chart);
            //设置LineChartView的一些基本属性
            mLineChartView.setInteractive(true);
            mLineChartView.setZoomType(ZoomType.HORIZONTAL);
            mLineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
            mLineChartView.setFocusableInTouchMode(true);
            mLineChartView.startDataAnimation();
        }
    }

    public MonitorCardAdapter(List<MonitorCardData> dataList) {
        mDataList = dataList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.monitor_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //把数据灌进去
        MonitorCardData data = mDataList.get(position);
        holder.mTitleView.setText(data.getTitle());
        holder.mEditIDView.setText(data.getIdInString());
        holder.mLineChartView.setLineChartData(data.getLineChartData());
        //监听关闭按钮
        holder.mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeOneCard(position);
            }
        });
        //监听跳转按钮
        holder.mJumpToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 获取新数据的接口
                Toast.makeText(mContext, "TODO: 跳转到第 " + holder.mEditIDView.getText() + " 个通道", Toast.LENGTH_SHORT).show();
                closeInputMethodAnyaway();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    //尝试关闭搜索模块默认开启的输入法
    public void closeInputMethodAnyaway() {
        Log.d(TAG, "closeInputMethodAnyaway");
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            //imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
            imm.hideSoftInputFromWindow(((MainActivity) mContext).getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /*
     *  接口函数
     *  注意，卡片的id和卡片的编号并不是同一个东西
     */
    //在底部增加一张卡片。返回它的编号
    public int addOneCard(MonitorCardData data) {
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

    public void adddata(int channel, int v) {
        int len = mDataList.size();
        for (int i = 1; i <= len; i++)
            if (mDataList.get(i).checkchannel(channel))
                mDataList.get(i).add(v);
    }
}
