package com.example.hh.nrfuart;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Angel on 2017/6/30.
 */

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder>
{
    private List<Card> mCardList;
    static class ViewHolder extends RecyclerView.ViewHolder
    {
        CardView save_card_view;
        CardView send_card_view;
        CardView monitor_card_view;


        //画图部分在这里声明
        //其他的View也在这里声明
        public ViewHolder(View view)
        {
            super(view);
            //在这里用FindViewById获取实例(见P123)
        }
    }

    public CardAdapter (List<Card> cardList)
    {
        mCardList = cardList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.save_card, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        Card card = mCardList.get(position);
        //Set Card 中的元素Id
    }

    @Override
    public int getItemCount()
    {
        return mCardList.size();
    }
}
