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

import java.util.List;
import java.util.Random;

/**
 * Created by nodgd on 2017/10/09.
 */

public class CreateCardSingleSelectAdapter extends RecyclerView.Adapter<CreateCardSingleSelectAdapter.ViewHolder> {

    public static String TAG = "CreateCardSingleSelectAdapterTag";

    private Context mContext;
    private RecyclerView mRecView;
    private List<String> mAllOptionList;
    private int mSelectedItemId;
    private int mZeroItemId;
    private ViewHolder mViewHolder;

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextView;
        private CheckBox mCheckBox;

        public ViewHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.edit_select_item_text);
            mCheckBox = (CheckBox) view.findViewById(R.id.edit_select_item_checkbox);
        }
    }

    public CreateCardSingleSelectAdapter(RecyclerView recView, List<String> allOptionList) {
        mRecView = recView;
        mAllOptionList = allOptionList;
        mSelectedItemId = -1;
        mZeroItemId = new Random().nextInt(65536);
        Log.d(TAG, "mZeroItemId = " + mZeroItemId);
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
        View view = LayoutInflater.from(mContext).inflate(R.layout.create_card_select_item, parent, false);
        mViewHolder = new ViewHolder(view);
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mTextView.setText(mAllOptionList.get(position));
        holder.mCheckBox.setChecked(position == mSelectedItemId);
        holder.mCheckBox.setId(mZeroItemId + position);
        Log.d(TAG, "set CheckBox id = " + (mZeroItemId + position));
        Log.d(TAG, "mZeroItemId = " + mZeroItemId);
        holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                if (isChecked) {//实现checkbox的单选功能,同样适用于radiobutton
                    if (mSelectedItemId != -1) {
                        //找到上次点击的checkbox,并把它设置为false,对重新选择时可以将以前的关掉
                        Log.d(TAG, "mZeroItemId = " + mZeroItemId);
                        Log.d(TAG, "find CheckBox: id =  " + (mZeroItemId + mSelectedItemId));
                        CheckBox tempCheckBox = (CheckBox) mRecView.findViewById(mZeroItemId + mSelectedItemId);
                        if (tempCheckBox != null) {
                            Log.d(TAG, "find successfully");
                            tempCheckBox.setChecked(false);
                        }
                    }
                    mSelectedItemId = position;//保存当前选中的checkbox的id值
                    Log.d(TAG, "mZeroItemId = " + mZeroItemId);
                } else {
                    mSelectedItemId = -1;
                }
            }
        });
    }

    public int getSelectedItemId() {
        return mSelectedItemId;
    }
}
