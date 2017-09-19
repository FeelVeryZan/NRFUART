package com.example.Zan.nrfuart;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.Zan.nrfuart.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;


/**
 * Created by Administrator_nodgd on 2017/09/15.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static String TAG = "MessageAdapterTag";

    private Context mContext;
    private List<List<Integer>> mMessageList;
    private ViewHolder mViewHolder;

    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    public static interface OnItemClickListener {
        public void onItemClick(View view, int postion);
    }

    public static interface OnItemLongClickListener {
        public void onItemLongClick(View view, int postion);
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {
        private CardView mCardView;
        private TextView mTextView;
        private ImageView mShowImageView;
        private ImageView mCloseImageView;
        private LineChartView mLineChartView;

        private OnItemClickListener mListener;
        private OnItemLongClickListener mLongClickListener;

        public ViewHolder(View view, OnItemClickListener listener, OnItemLongClickListener longClickListener) {
            super(view);
            mCardView = (CardView) view;
            mTextView = (TextView) view.findViewById(R.id.message_textview);
            mShowImageView = (ImageView) view.findViewById(R.id.message_show_button);
            mCloseImageView = (ImageView) view.findViewById(R.id.message_close_button);
            mLineChartView = (LineChartView) view.findViewById(R.id.message_chartview);

            mShowImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mLineChartView.getVisibility() == View.GONE) {
                        mLineChartView.setVisibility(View.VISIBLE);
                        mShowImageView.setRotation(90);
                    } else {
                        mLineChartView.setVisibility(View.GONE);
                        mShowImageView.setRotation(270);
                    }
                }
            });

            this.mListener = listener;
            this.mLongClickListener = longClickListener;
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }
        @Override
        public void onClick(View view) {
            if(mListener != null){
                mListener.onItemClick(view, getPosition());
            }
        }
        @Override
        public boolean onLongClick(View view) {
            if(mLongClickListener != null){
                mLongClickListener.onItemLongClick(view, getPosition());
            }
            return true;
        }
    }

    public MessageAdapter(List<List<Integer>> messageList) {
        mMessageList = messageList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.message_flow_layout, parent, false);
        mViewHolder = new ViewHolder(view, mItemClickListener, mItemLongClickListener);
        return mViewHolder;
    }

    public void addOneCard(List<Integer> msg) {
        mMessageList.add(msg);
        notifyItemInserted(mMessageList.size());
    }
    public void removeOneCard(int pos) {
        notifyItemRemoved(pos);
        mMessageList.remove(pos);
    }
    public void addItemMessage(int pos, List<Integer> msg) {
        for (Integer x: msg) {
            mMessageList.get(pos).add(x);
        }
        notifyItemChanged(pos);
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        /*
         *  图部分
         */
        //设置个随机高度看起来有趣一点
        holder.mCardView.setMinimumHeight((new Random().nextInt()) % 50 + 100);
        //导入图表内容
        holder.mLineChartView.setLineChartData(fromListIntegerToLineChartData(mMessageList.get(position)));
        //其他设置
        holder.mLineChartView.setInteractive(true);
        holder.mLineChartView.setZoomType(ZoomType.HORIZONTAL);
        holder.mLineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        holder.mLineChartView.setFocusableInTouchMode(true);
        holder.mLineChartView.startDataAnimation();
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    //RecyclerView不带有这两个函数，就自己添加
    public void setOnItemClickListener(OnItemClickListener listener){
        this.mItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener){
        this.mItemLongClickListener = listener;
    }

    //把普通数据转换为可以传入的数据
    public static LineChartData fromListIntegerToLineChartData(List<Integer> messageFlow) {

        //流数据变成点序列
        List<PointValue> valueList = new ArrayList<>();
        for(int i = 0; i < messageFlow.size(); i++) {
            valueList.add(new PointValue(i, messageFlow.get(i)));
        }
        //点序列变成线
        Line line = new Line(valueList);
        line.setColor(Color.BLUE);
        line.setCubic(false);
        line.setFilled(false);
        line.setHasPoints(false);
        line.setStrokeWidth(1);
        //线变成线序列，但是线序列里面只有一条线
        List<Line> lineList = new ArrayList<>();
        lineList.add(line);

        //x轴
        Axis axisX = new Axis();
        axisX.setTextColor(Color.RED);
        axisX.setTextSize(10);
        axisX.setAutoGenerated(true);
        axisX.setName("nodgd");
        List<AxisValue> axisXValueList = new ArrayList<>();
        for (int i = 0; i < 100; i += 10) {
            axisXValueList.add(new AxisValue(i));
        }
        axisX.setValues(axisXValueList);

        //y轴
        Axis axisY = new Axis();
        axisY.setTextColor(Color.RED);
        axisY.setTextSize(10);
        axisY.setAutoGenerated(true);
        List<AxisValue> axisYValueList = new ArrayList<>();
        for (int i = 32; i <= 256; i += 64) {
            axisYValueList.add(new AxisValue(i));
        }
        axisY.setValues(axisYValueList);

        //新建一个可以传入的数据，填入数据
        LineChartData lineChartData = new LineChartData();
        lineChartData.setLines(lineList);
        lineChartData.setAxisXBottom(axisX);
        lineChartData.setAxisYLeft(axisY);
        return lineChartData;
    }
}
