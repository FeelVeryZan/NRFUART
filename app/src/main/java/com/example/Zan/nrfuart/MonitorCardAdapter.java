package com.example.Zan.nrfuart;

import android.app.Activity;
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

import java.util.ArrayList;
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
    private List<RecyclerView.ViewHolder> mViewHolderList = new ArrayList<>();
    private int counter = 0;

    static class ViewHolder extends RecyclerView.ViewHolder {

        private CardView mCardView;
        private TextView mTitleView;
        private ImageButton mCloseButton;
        private EditText mEditChannelView;
        private Button mJumpToButton;
        private LineChartView mLineChartView;


        public ViewHolder(View itemView) {
            super(itemView);
            //获取各个View
            mCardView = (CardView) itemView;
            mTitleView = (TextView) itemView.findViewById(R.id.card_title);
            mCloseButton = (ImageButton) itemView.findViewById(R.id.close_card);
            mEditChannelView = (EditText) itemView.findViewById(R.id.channel_editer);
            mEditChannelView.setVisibility(View.GONE);
            mJumpToButton = (Button) itemView.findViewById(R.id.jump_to);
            mJumpToButton.setVisibility(View.GONE);
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

    public MonitorCardAdapter() {
        mDataList = new ArrayList<>();
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
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //先在List报道
        mViewHolderList.add(position,holder);
        //把数据灌进去
        final MonitorCardData data = mDataList.get(position);
        holder.mTitleView.setText(data.getTitle());
        holder.mEditChannelView.setText(String.valueOf(data.getChannel()));
        holder.mLineChartView.setLineChartData(data.getLineChartData());
        //监听关闭按钮
        holder.mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeOneCardByIdentifier(data.getIdentifier());
            }
        });
        //监听输入框失去焦点
        holder.mEditChannelView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    closeInputMethodAnyaway();
                }
            }
        });
        //监听跳转按钮
        holder.mJumpToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String channelText = holder.mEditChannelView.getText().toString().trim();
                int channelInt = -1;
                try {
                    channelInt = Integer.parseInt(channelText);
                } catch (NumberFormatException e) {
                    Log.d(TAG, "NumberFormatException: " + e.getMessage());
                }
                if (channelInt < 0 || channelInt >= WorkFlow.channelNumber) {
                    CreateCardIgnoreFragment ignoreFragment = new CreateCardIgnoreFragment("Wrong channel.");
                    ignoreFragment.show(((Activity) mContext).getFragmentManager(), "？蛤？");
                    closeInputMethodAnyaway();
                    return;
                }
                if (data.getChannel() != channelInt) {
                    Log.d(TAG, "从通道" + data.getChannel() + "跳转到" + channelInt);
                    changeChannelByIdentifier(data.getIdentifier(), channelInt);
                }
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
            try {
                imm.hideSoftInputFromWindow(((Activity) mContext).getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            } catch (NullPointerException e) {
                Log.e(TAG, "关闭输入法失败");
            }
        }
    }

    /*
     *  接口函数
     *  注意，卡片的id和卡片的编号并不是同一个东西
     */
    //由标识符获取位置
    public int getPositionFromIdentifier(int identifier) {
        for (int i = 0; i < mDataList.size(); i++) {
            if (mDataList.get(i).getIdentifier() == identifier) {
                return i;
            }
        }
        Log.e(TAG, "The identifier is wrong: " + identifier);
        return -1;
    }

    //在底部增加一张卡片
    public void addOneCard(MonitorCardData cardData) {
        mDataList.add(cardData);
        notifyItemInserted(mDataList.size() - 1);
    }

    //通过位置删除一张卡片，位置编号从0开始
    public void removeOneCardByPosition(int position) {
        if (position < 0 || position >= mDataList.size()) {
            Log.e(TAG, "The position is wrong: " + position);
        }
        mDataList.remove(position);
        notifyItemRemoved(position);
    }

    //通过标识符删除一张卡片
    public void removeOneCardByIdentifier(int identifier) {
        removeOneCardByPosition(getPositionFromIdentifier(identifier));
    }

    //给某个通道的所有卡片添加一个数据
    public void addMessageByChannel(int channel, int message) {
        int n = mDataList.size();
        counter ++;
        for (int i = 0; i < n; i++) {
            if (mDataList.get(i).getChannel() == channel) {
                Log.d(TAG, "addMessageByChannel: "+channel+"   "+message);
                mDataList.get(i).addMessage(message);
                if ((counter = counter % 5) == 0)
                {
                    notifyItemChanged(i);
                }
            }
        }
    }

    //将一张卡片跳转到一个新的通道
    public void changeChannelByIdentifier(int identifier, int channel) {
        int position = getPositionFromIdentifier(identifier);
        mDataList.get(position).setChannel(channel);
        mDataList.get(position).clearMessage();
        notifyItemChanged(position);
    }
}
