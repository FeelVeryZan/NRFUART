package com.example.Zan.nrfuart;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator_nodgd on 2017/09/17.
 */

public class CreateCardChooseAdapter extends RecyclerView.Adapter<CreateCardChooseAdapter.ViewHolder> {

    public static String TAG = "CreateCardChooseAdapterTag";

    private Context mContext;
    private List<String> mAllOptionList;
    private List<Boolean> mAllStateList;
    private ViewHolder mViewHolder;

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextView;
        private CheckBox mCheckBox;

        public ViewHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.edit_choose_item_text);
            mCheckBox = (CheckBox) view.findViewById(R.id.edit_choose_item_checkbox);
        }
    }


    public CreateCardChooseAdapter(List<String> allOptionList) {
        mAllOptionList = allOptionList;
        mAllStateList = new ArrayList<>();
        for (int i = 0; i < mAllOptionList.size(); i++) {
            mAllStateList.add(false);
        }
    }

    @Override
    public int getItemCount() {
        return mAllOptionList.size();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.create_card_choose_item, parent, false);
        mViewHolder = new ViewHolder(view);
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder " + position + " " + mAllOptionList.get(position));
        holder.mTextView.setText(mAllOptionList.get(position));
        holder.mCheckBox.setChecked(mAllStateList.get(position));
        holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                mAllStateList.set(position, isChecked);
            }
        });
    }

    public int getChooseItemCound() {
        int cnt = 0;
        for (int i = 0; i < mAllStateList.size(); i++) {
            if (mAllStateList.get(i)) {
                cnt++;
            }
        }
        return cnt;
    }

    public void chooseAll() {
        for (int i = 0; i < mAllOptionList.size(); i++) {
            mAllStateList.set(i, true);
        }
        notifyDataSetChanged();
    }

    public void chooseNone() {
        for (int i = 0; i < mAllOptionList.size(); i++) {
            mAllStateList.set(i, false);
        }
        notifyDataSetChanged();
    }

    public void chooseInvert() {
        for (int i = 0; i < mAllOptionList.size(); i++) {
            mAllStateList.set(i, !mAllStateList.get(i));
        }
        notifyDataSetChanged();
    }

    public int[] getStateInArray() {
        int n = getItemCount();
        int radio[] = new int[n];
        for (int i = 0; i < n; i++)
            radio[i] = mAllStateList.get(i) ? 1 : 0;
        return radio;
    }
}
