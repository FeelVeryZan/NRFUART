package com.example.Zan.nrfuart;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

/**
 * Created by nodgd on 2017/09/17.
 */

public class SaveCardAdapter extends RecyclerView.Adapter<SaveCardAdapter.ViewHolder>
{
    public static String TAG = "SaveCardAdapterTag";

    private Context mContext;
    private List<SaveCardData> mDataList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView mCardView;
        private TextView mTitleView;
        private ImageButton mCloseButton;
        private TextView mThisId;
        private TextView mContentView;

        public ViewHolder(View view)
        {
            super(view);
            //获取各个View
            mCardView = (CardView) view;
            mTitleView = (TextView) itemView.findViewById(R.id.card_title);
            mCloseButton = (ImageButton) itemView.findViewById(R.id.close_card);
            mThisId = (TextView) itemView.findViewById(R.id.this_id);
            mContentView = (TextView) itemView.findViewById(R.id.save_content);
            mContentView.setMovementMethod(ScrollingMovementMethod.getInstance());
        }
    }

    public SaveCardAdapter(List<SaveCardData> cardList) {
        mDataList = cardList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.save_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        //把数据灌进去
        SaveCardData data = mDataList.get(position);
        holder.mTitleView.setText(data.getTitle());
        holder.mThisId.setText(data.getIdInString());
        holder.mContentView.setText(data.getContent());
        //监听关闭事件
        holder.mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeOneCard(position);
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
    //由唯一标识符得到pos。这个函数只在内部调用
    private int getPosFromIdentifier(int ide) {
        for (int i = 0; i < mDataList.size(); i++) {
            if(mDataList.get(i).getIdentifier() == ide) {
                return i;
            }
        }
        Log.e(TAG, "错误的标识符：ide = " + ide);
        return -1;
    }

    //在底部增加一张卡片。返回它的唯一标识符
    public int addOneCard(SaveCardData data) {
        int ide = IdentifierManager.getNewIdentifier();
        data.setIdentifier(ide);
        mDataList.add(data);
        notifyItemInserted(mDataList.size());
        mDataList.get(mDataList.size()).Start();
        return ide;
    }

    //删除唯一标识符为ide的卡片。返回是否成功
    public boolean removeOneCard(int ide) {
        int pos = getPosFromIdentifier(ide);
        mDataList.get(pos).shutdown();
        mDataList.remove(pos);
        notifyItemRemoved(pos);
        return true;
    }

    //修改唯一标识符为ide的卡片的标题。返回是否成功
    public boolean setItemTitle(int ide, String title) {
        int pos = getPosFromIdentifier(ide);
        mDataList.get(pos).setTitle(title);
        notifyItemChanged(pos);
        return true;
    }

    //修改唯一标识符为ide卡片显示的ID。返回是否成功
    public boolean setItemId(int ide, int id) {
        int pos = getPosFromIdentifier(ide);
        mDataList.get(pos).setId(id);
        notifyItemChanged(pos);
        return true;
    }

    //给唯一标识符为ide的卡片上显示的内容添加数据。返回是否成功
    public boolean addItemContent(int ide, String moreContent) {
        int pos = getPosFromIdentifier(ide);
        mDataList.get(pos).addContent(moreContent);
        notifyItemChanged(pos);
        return true;
    }

    //清空并重新设置唯一标识符为ide的卡片上显示的内容。返回是否成功
    public boolean setItemContent(int ide, String content) {
        int pos = getPosFromIdentifier(ide);
        mDataList.get(pos).setContent(content);
        notifyItemChanged(pos);
        return true;
    }
}








































