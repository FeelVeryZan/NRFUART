package com.example.Zan.nrfuart;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class SendCardAdapter extends RecyclerView.Adapter<SendCardAdapter.ViewHolder> {

    public static String TAG = "SendCardAdapter";

    private Context mContext;
    private List<SendCardData> mDataList;
    private SendCardEditWindow mEditWindow;

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

    public interface OnEditCallBack {
        public void onEdit();
    }

    public SendCardAdapter(List<SendCardData> dataList) {
        mDataList = dataList;
    }

    public SendCardAdapter() {
        mDataList = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.send_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (mEditWindow == null) {
            mEditWindow = new SendCardEditWindow(mContext);
        }
        //把数据灌进去
        final SendCardData cardData = mDataList.get(position);
        Log.d(TAG, "把数据灌进去, position = " + position + " identifier = " + cardData.getIdentifier());
        holder.mTitleView.setText(cardData.getTitle());
        holder.mThisId.setText(String.valueOf(cardData.getChannel()));
        holder.mNowState.setText(cardData.getStateInString());
        holder.mLineChartView.setLineChartData(cardData.getLineChartData());
        //监听关闭按钮
        holder.mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "close: identifier = " + cardData.getIdentifier());
                removeOneCardByIdentifier(cardData.getIdentifier());
            }
        });
        //监听设置按钮
        holder.mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "edit: identifier = " + cardData.getIdentifier());
                mEditWindow.show(new OnEditCallBack() {
                    @Override
                    public void onEdit() {
                        //TODO 修改SendThread等信息的回调函数，可以自行添加onEdit的函数参数
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    /*
     *  接口函数
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
    public void addOneCard(SendCardData cardData) {
        mDataList.add(cardData);
        notifyItemInserted(mDataList.size() - 1);
        //新增的同时要让send线程跑起来
        cardData.startSendThread();
    }

    //通过位置删除一张卡片，位置编号从0开始
    public void removeOneCardByPosition(int position) {
        if (position < 0 || position >= mDataList.size()) {
            Log.e(TAG, "The position is wrong: " + position);
        }
        SendCardData cardData = mDataList.get(position);
        cardData.stopSendThread();
        mDataList.remove(position);
        notifyItemRemoved(position);
    }

    //通过标识符删除一张卡片
    public void removeOneCardByIdentifier(int identifier) {
        removeOneCardByPosition(getPositionFromIdentifier(identifier));
    }

    public void reviewDataByIdentifier(int identifier) {
        int pos = getPositionFromIdentifier(identifier);
        if (pos != -1) {
            mDataList.get(pos).DataReview();
            notifyItemChanged(pos);
        }
    }
    //TODO 修改卡片内容（标题等）的各种接口
}
