package com.example.Zan.nrfuart;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

/**
 * Created by nodgd on 2017/10/14.
 */

public class CreateCardSelectAdapter {
    public static String TAG = "CreateCardSelectAdapterTag";

    public static int MODE_SINGLE = 1;
    public static int MODE_MULTI = 2;

    private int mSelectMode;
    private CheckBox mCheckBox[] = new CheckBox[4];
    private int mSelectNow;     //这个变量单选模式才有用

    public CreateCardSelectAdapter(View selectLayout, int selectMode) {
        mSelectMode = selectMode;
        if (mSelectMode != MODE_SINGLE && mSelectMode != MODE_MULTI) {
            Log.e(TAG, "错误的selectMode");
            throw new CreateCardSelectAdapter.SelectAdapterException();
        }
        mSelectNow = -1;
        //获取每一个CheckBox控件
        mCheckBox[0] = (CheckBox) selectLayout.findViewById(R.id.edit_select_0).findViewById(R.id.edit_select_checkbox);
        mCheckBox[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                oneBoxChanged(0, isChecked);
            }
        });
        mCheckBox[1] = (CheckBox) selectLayout.findViewById(R.id.edit_select_1).findViewById(R.id.edit_select_checkbox);
        mCheckBox[1].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                oneBoxChanged(1, isChecked);
            }
        });
        mCheckBox[2] = (CheckBox) selectLayout.findViewById(R.id.edit_select_2).findViewById(R.id.edit_select_checkbox);
        mCheckBox[2].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                oneBoxChanged(2, isChecked);
            }
        });
        mCheckBox[3] = (CheckBox) selectLayout.findViewById(R.id.edit_select_3).findViewById(R.id.edit_select_checkbox);
        mCheckBox[3].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                oneBoxChanged(3, isChecked);
            }
        });
        //给每个通道选项改名字
        TextView mChannelName;
        mChannelName = (TextView) selectLayout.findViewById(R.id.edit_select_0).findViewById(R.id.edit_select_channelname);
        mChannelName.setText("#0");
        mChannelName = (TextView) selectLayout.findViewById(R.id.edit_select_1).findViewById(R.id.edit_select_channelname);
        mChannelName.setText("#1");
        mChannelName = (TextView) selectLayout.findViewById(R.id.edit_select_2).findViewById(R.id.edit_select_channelname);
        mChannelName.setText("#2");
        mChannelName = (TextView) selectLayout.findViewById(R.id.edit_select_3).findViewById(R.id.edit_select_channelname);
        mChannelName.setText("#3");
        //处理介绍文字
        TextView mDescription = (TextView) selectLayout.findViewById(R.id.edit_select_description);
        if (mSelectMode == MODE_SINGLE) {
            mDescription.setText("Choose one channel:");
        } else {
            mDescription.setText("Choose channel(s) to save:");
        }
        //处理三个快速选择按钮
        View mBtns = (View) selectLayout.findViewById(R.id.edit_select_btns);
        if (mSelectMode == MODE_SINGLE) {
            mBtns.setVisibility(View.GONE);
        } else {
            Button mAll = (Button) selectLayout.findViewById(R.id.edit_select_all);
            mAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectAll();
                }
            });
            Button mNone = (Button) selectLayout.findViewById(R.id.edit_select_none);
            mNone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectNone();
                }
            });
            Button mOther = (Button) selectLayout.findViewById(R.id.edit_select_others);
            mOther.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectOthers();
                }
            });
        }
    }

    //某一个CheckBox发生了变化
    private void oneBoxChanged(int id, boolean isChecked) {
        Log.d(TAG, "oneBoxChanged: id = " + id + " isChecked = " + isChecked);
        Log.d(TAG, "oneBoxChanged: mSelectMode = " + mSelectMode + " mSelectNow = " + mSelectNow);
        if (mSelectMode == MODE_SINGLE) {
            if (isChecked) {
                if (0 <= mSelectNow && mSelectNow <= 3) {
                    mCheckBox[mSelectNow].setChecked(false);
                }
                mSelectNow = id;
            } else {
                mSelectNow = -1;
            }
        }
    }
    //全选
    private void selectAll() {
        for (int i = 0; i < 4; i++) {
            mCheckBox[i].setChecked(true);
        }
    }
    //全不选
    private void selectNone() {
        for (int i = 0; i < 4; i++) {
            mCheckBox[i].setChecked(false);
        }
    }
    //反选
    private void selectOthers() {
        for (int i = 0; i < 4; i++) {
            mCheckBox[i].setChecked(!mCheckBox[i].isChecked());
        }
    }

    //单选模式下，查询选中的通道编号
    public int getSelectedItemId() {
        if (mSelectMode == MODE_MULTI) {
            throw new CreateCardSelectAdapter.SelectAdapterException();
        } else {
            return mSelectNow;
        }
    }
    //多选模式下，查询已经选中了多少个
    public int getSelectedItemCount() {
        if (mSelectMode == MODE_SINGLE) {
            throw new CreateCardSelectAdapter.SelectAdapterException();
        } else {
            int cnt = 0;
            for (int i = 0; i < 4; i++) {
                if (mCheckBox[i].isChecked()) {
                    cnt++;
                }
            }
            return cnt;
        }
    }
    //多选模式下，传一个数组回去发广播
    public int[] getStateInArray() {
        if (mSelectMode == MODE_SINGLE) {
            throw new CreateCardSelectAdapter.SelectAdapterException();
        } else {
            int radio[] = new int[4];
            for (int i = 0; i < 4; i++) {
                if (mCheckBox[i].isChecked()) {
                    radio[i] = 1;
                } else {
                    radio[i] = 0;
                }
            }
            return radio;
        }
    }
    //是否毛都没选
    public boolean isEmpty() {
        if (mSelectMode == MODE_SINGLE) {
            return mSelectNow == -1;
        } else {
            for (int i = 0; i < 4; i++) {
                if (mCheckBox[i].isChecked()) {
                    return false;
                }
            }
            return true;
        }
    }

    //专属异常
    public static class SelectAdapterException extends RuntimeException {
    }
}
