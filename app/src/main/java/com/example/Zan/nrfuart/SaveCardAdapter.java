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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nodgd on 2017/09/17.
 */

public class SaveCardAdapter extends RecyclerView.Adapter<SaveCardAdapter.ViewHolder> {
    public static String TAG = "SaveCardAdapterTag";

    private Context mContext;
    private List<SaveCardData> mDataList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView mCardView;
        private TextView mTitleView;
        private ImageButton mCloseButton;
        private TextView mThisId;
        private TextView mContentView;

        public ViewHolder(View view) {
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

    public SaveCardAdapter() {
        mDataList = new ArrayList<>();
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
        final SaveCardData cardData = mDataList.get(position);
        holder.mTitleView.setText(cardData.getTitle());
        //holder.mThisId.setText(String.valueOf(cardData.getIdentifier()));
        holder.mThisId.setText(cardData.getChannelList());
        //holder.mContentView.setText(cardData.getContent());
        holder.mContentView.setText("已经开始存储");
        //监听关闭事件
        holder.mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "close: identifier = " + cardData.getIdentifier());
                removeOneCardByIdentifier(cardData.getIdentifier());
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
    public void addOneCard(SaveCardData cardData) {
        mDataList.add(cardData);
        notifyItemInserted(mDataList.size() - 1);
        cardData.startSaveThread();
    }

    //通过位置删除一张卡片，位置编号从0开始
    public void removeOneCardByPosition(int position) {
        if (position < 0 || position >= mDataList.size()) {
            Log.e(TAG, "The position is wrong: " + position);
        }
        mDataList.get(position).stopSaveThread();
        mDataList.remove(position);
        notifyItemRemoved(position);
    }

    //通过标识符删除一张卡片
    public void removeOneCardByIdentifier(int identifier) {
        removeOneCardByPosition(getPositionFromIdentifier(identifier));
    }

    //TODO 修改卡片内容（标题等）的各种接口
    /*
    //修改唯一标识符为ide的卡片的标题。返回是否成功
    public boolean setItemTitle(int ide, String title) {
        int pos = getPosFromIdentifier(ide);
        if (pos == -1)
            return false;
        mDataList.get(pos).setTitle(title);
        notifyItemChanged(pos);
        return true;
    }
    //修改唯一标识符为ide卡片显示的ID。返回是否成功
    public boolean setItemId(int ide, int id) {
        int pos = getPosFromIdentifier(ide);
        if (pos == -1)
            return false;
        mDataList.get(pos).setId(id);
        notifyItemChanged(pos);
        return true;
    }
    //给唯一标识符为ide的卡片上显示的内容添加数据。返回是否成功
    public boolean addItemContent(int ide, String moreContent) {
        int pos = getPosFromIdentifier(ide);
        if (pos == -1)
            return false;
        mDataList.get(pos).addContent(moreContent);
        notifyItemChanged(pos);
        return true;
    }
    //清空并重新设置唯一标识符为ide的卡片上显示的内容。返回是否成功
    public boolean setItemContent(int ide, String content) {
        int pos = getPosFromIdentifier(ide);
        if (pos == -1)
            return false;
        mDataList.get(pos).setContent(content);
        notifyItemChanged(pos);
        return true;
    }
    */
}








































