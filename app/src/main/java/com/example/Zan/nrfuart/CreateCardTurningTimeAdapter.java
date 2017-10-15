package com.example.Zan.nrfuart;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

/**
 * Created by nodgd on 2017/10/15.
 */

public class CreateCardTurningTimeAdapter {

    public static String TAG = "CreateCardTurningTimeAdapter";

    //根基
    private Context mContext;
    private View mTurningTimeLayout;
    private int mId;
    private RangeCallBack mRangeCallBack;
    //内容
    private EditText mPEditText;
    private String mPStr;
    private SeekBar mPSeekBar;
    private int mPermillage;
    private boolean mPSyncLock;

    public CreateCardTurningTimeAdapter(Context context, View turningLayout, int id) {
        //根基
        mContext = context;
        mTurningTimeLayout = turningLayout;
        mId = id;
        mPermillage = (mId + 1) * 100;
        //内容
        initDescription();
        initEditText();
        initSeekBar();
    }

    //描述文字模块
    private void initDescription() {
        TextView mDescription = (TextView) mTurningTimeLayout.findViewById(R.id.edit_turn_description);
        mDescription.setText("Turning time #" + mId + ":");
    }

    //输入框模块
    private void initEditText() {
        mPEditText = (EditText) mTurningTimeLayout.findViewById(R.id.edit_turn_edittext);
        mPEditText.setText("" + (0.1 * mPermillage));
        mPEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mPStr = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mPSyncLock) {
                    mPSyncLock = true;
                    String str = s.toString();
                    if (str.length() == 0) {
                        mPSeekBar.setProgress(mRangeCallBack.getMinimun());
                    } else if (str.length() > 8) {
                        Toast.makeText(mContext, "The input is too long.", Toast.LENGTH_SHORT).show();
                        mPEditText.setText(mPStr);

                    } else {
                        try {
                            double pd = Double.parseDouble(str);
                            int p = (int) Math.round(pd * 10);
                            if (p < mRangeCallBack.getMinimun() || p > mRangeCallBack.getMaximun()) {
                                throw new NumberFormatException();
                            }
                            mPermillage = p;
                            mPSeekBar.setProgress(p);
                        } catch (NumberFormatException e) {
                            Toast.makeText(mContext, "The input must be in range.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    mPSyncLock = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }
    //拖拽条模块
    private void initSeekBar() {
        mPSeekBar = (SeekBar) mTurningTimeLayout.findViewById(R.id.edit_turn_seekbar);
        mPSeekBar.setProgress(mPermillage);
        mPSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !mPSyncLock) {
                    mPSyncLock = true;
                    Log.d(TAG, "min = " + mRangeCallBack.getMinimun() + ", max = " + mRangeCallBack.getMaximun());
                    if (progress < mRangeCallBack.getMinimun()) {
                        mPermillage = mRangeCallBack.getMinimun();
                        mPSeekBar.setProgress(mPermillage);
                        String str = new DecimalFormat("#.#").format(mPermillage * 0.1);
                        mPEditText.setText(str);
                    } else if (progress > mRangeCallBack.getMaximun()) {
                        mPermillage = mRangeCallBack.getMaximun();
                        mPSeekBar.setProgress(mPermillage);
                        String str = new DecimalFormat("#.#").format(mPermillage * 0.1);
                        mPEditText.setText(str);
                    } else {
                        mPermillage = progress;
                        String str = new DecimalFormat("#.#").format(mPermillage * 0.1);
                        mPEditText.setText(str);
                    }
                    mPSyncLock = false;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    //获取千分比
    public int getPermillage() {
        return mPermillage;
    }
    //设置回调接口
    public void setRangeCallBack(RangeCallBack rangeCallBack) {
        mRangeCallBack = rangeCallBack;
    }

    //回调函数，实时获取可调节范围。千分比，0~1000，闭区间。
    public static interface RangeCallBack{
        public int getMinimun();
        public int getMaximun();
    }
}
