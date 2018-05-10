package com.example.Crofun.Hmilab;

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
 * Update by nodgd on 2018/05/10: 百分比变成绝对值
 */

public class CreateCardTurningTimeAdapter {

    public static String TAG = "CreateCardTurningTimeAdapter";

    //根基
    private Context mContext;
    private View mTurningTimeLayout;
    private int mId;
    //回调接口
    private RangeCallBack mRangeCallBack;
    private CreateCardParameterAdapter.RedrawPreviewCallBack mRedrawPreviewCallBack;
    //内容
    private EditText mPEditText;
    private String mPStr;
    private SeekBar mPSeekBar;
    private boolean mPSyncLock;
    private double mPeriod;
    private double mAbsoluteValue;
    private int mPermillage;
    private static boolean mRangeCheckingFlag = true;

    public CreateCardTurningTimeAdapter(Context context, View turningLayout, int id, double period) {
        //根基
        mContext = context;
        mTurningTimeLayout = turningLayout;
        mId = id;
        mPeriod = period;
        mAbsoluteValue = mPeriod * (mId + 1) * 0.1;
        mPermillage = (mId + 1) * 100;
        //内容
        initDescription();
        initEditText();
        initSeekBar();
    }

    //描述文字模块
    private void initDescription() {
        TextView mDescription = (TextView) mTurningTimeLayout.findViewById(R.id.edit_turn_description);
        mDescription.setText("Turning time #" + (mId + 1) + ":");
    }

    //输入框模块
    private void initEditText() {
        mPEditText = (EditText) mTurningTimeLayout.findViewById(R.id.edit_turn_edittext);
        mPEditText.setText(new DecimalFormat("#.#########").format(mAbsoluteValue));
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
                    } else if (str.length() > 11) {
                        Toast.makeText(mContext, "The input is too long.", Toast.LENGTH_SHORT).show();
                        mPEditText.setText(mPStr);
                    } else {
                        try {
                            double pd = Double.parseDouble(str);
                            Log.d(TAG, "onTextChanged() pd = " + pd);
                            if (mRangeCheckingFlag && (pd < mPeriod * 0.001 * mRangeCallBack.getMinimun() || pd > mPeriod * 0.001 * mRangeCallBack.getMaximun())) {
                                throw new NumberFormatException();
                            }
                            mAbsoluteValue = pd;
                            mPermillage = (int) Math.round(pd / mPeriod * 1000);
                            Log.d(TAG, "onTextChanged() mPermillage = " + mPermillage);
                            mPSeekBar.setProgress(mPermillage);
                            mRedrawPreviewCallBack.redraw();
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
                    } else if (progress > mRangeCallBack.getMaximun()) {
                        mPermillage = mRangeCallBack.getMaximun();
                        mPSeekBar.setProgress(mPermillage);
                    } else {
                        mPermillage = progress;
                    }
                    mAbsoluteValue = mPeriod * mPermillage * 0.001;
                    String str = new DecimalFormat("#.#########").format(mAbsoluteValue);
                    mPEditText.setText(str);
                    mRedrawPreviewCallBack.redraw();
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
    //设置千分比
    public boolean setPermillage(int permillage) {
        if (mRangeCallBack.getMinimun() <= permillage && permillage <= mRangeCallBack.getMaximun()) {
            mPermillage = permillage;
            mAbsoluteValue = mPeriod * mPermillage * 0.001;
            mPEditText.setText(new DecimalFormat("#.#########").format(mAbsoluteValue));
            return true;
        }
        return false;
    }
    //设置获取范围回调接口
    public void setRangeCallBack(RangeCallBack rangeCallBack) {
        mRangeCallBack = rangeCallBack;
    }
    //设置重新绘图回调接口
    public void setRedrawPreviewCallBack(CreateCardParameterAdapter.RedrawPreviewCallBack redrawPreviewCallBack) {
        mRedrawPreviewCallBack = redrawPreviewCallBack;
    }
    //设置是否检查范围
    public static void setRangeCheckingFlag(boolean flag) {
        mRangeCheckingFlag = flag;
    }
    //设置新的周期
    public void setPeriod(double T) {
        mPeriod = T;
        double minAbsVal = mPeriod * 0.001 * (1 + mId);
        mAbsoluteValue = mAbsoluteValue < minAbsVal ? minAbsVal : mAbsoluteValue;
        double maxAbsVal = mPeriod * 0.001 * (997 + mId);
        mAbsoluteValue = mAbsoluteValue > maxAbsVal ? maxAbsVal : mAbsoluteValue;
        mPermillage = (int) Math.round(mAbsoluteValue / mPeriod * 1000);
        mPEditText.setText("" + new DecimalFormat("#.#########").format(mAbsoluteValue));
        mPSeekBar.setProgress(mPermillage);
        mRedrawPreviewCallBack.redraw();
    }

    //回调函数，实时获取可调节范围。千分比，0~1000，闭区间。
    public static interface RangeCallBack{
        public int getMinimun();
        public int getMaximun();
    }
}
