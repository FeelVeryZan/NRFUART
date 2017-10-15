package com.example.Zan.nrfuart;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DecimalFormat;

/**
 * Created by nodgd on 2017/10/14.
 */

public class CreateCardParameterAdapter {

    public static String TAG = "CreateCardParameterAdapterTag";

    //根基
    private Context mContext;
    private View mParameterLayout;
    //频率or周期
    private EditText mFrequencyEText;
    private String mFrequencyStr;
    private EditText mPeriodEText;
    private String mPeriodStr;
    private boolean mFPSyncLock;
    //最值
    private EditText mMaxEText;
    private EditText mMinEText;
    //重复次数
    private EditText mRepetitionEText;
    private String mRepetitionStr;
    private boolean mRepetitionLock;
    //转折点们
    private CreateCardTurningTimeAdapter[] mTurningTimeAdapter = new CreateCardTurningTimeAdapter[3];


    public CreateCardParameterAdapter(Context context, View parameterLayout) {
        mContext = context;
        mParameterLayout = parameterLayout;
        initFrequency();
        initPeriod();
        initMaxAndMin();
        initRepetition();
        initTurningTime();

    }

    //频率模块
    private void initFrequency() {
        mFPSyncLock = false;
        mFrequencyEText = (EditText) mParameterLayout.findViewById(R.id.edit_f_edittext);
        mFrequencyEText.setText("10");
        mFrequencyEText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mFrequencyStr = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mFPSyncLock) {
                    mFPSyncLock = true;
                    String str = s.toString();
                    if (str.length() == 0) {
                        mPeriodEText.setText("");
                    } else if (str.length() > 8) {
                        Toast.makeText(mContext, "The input is too long.", Toast.LENGTH_SHORT).show();
                        mFrequencyEText.setText(mFrequencyStr);
                    } else {
                        try {
                            double f = Double.parseDouble(str);
                            if (f < 1e-6 || f > 1e6) {
                                throw new NumberFormatException();
                            }
                            str = new DecimalFormat("#.######").format(1.0 / f);
                            mPeriodEText.setText(str);
                        } catch (NumberFormatException e) {
                            Toast.makeText(mContext, "The input must be a positive floating point numbers which must be from 1e-6 to 1e6.", Toast.LENGTH_SHORT).show();
                            mPeriodEText.setText("");
                        }
                    }
                    mFPSyncLock = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    //周期模块
    private void initPeriod() {
        mPeriodEText = (EditText) mParameterLayout.findViewById(R.id.edit_t_edittext);
        mPeriodEText.setText("0.1");
        mPeriodEText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mPeriodStr = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mFPSyncLock) {
                    mFPSyncLock = true;
                    String str = s.toString();
                    if (str.length() == 0) {
                        mFrequencyEText.setText("");
                    } else if (str.length() > 8) {
                        Toast.makeText(mContext, "The input is too long.", Toast.LENGTH_SHORT).show();
                        mPeriodEText.setText(mPeriodStr);
                    } else {
                        try {
                            double T = Double.parseDouble(str);
                            if (T < 1e-6 || T > 1e6) {
                                throw new NumberFormatException();
                            }
                            str = new DecimalFormat("#.######").format(1.0 / T);
                            mFrequencyEText.setText(str);
                        } catch (NumberFormatException e) {
                            Toast.makeText(mContext, "The input must be from 1e-6 to 1e6.", Toast.LENGTH_SHORT).show();
                            mFrequencyEText.setText("");
                        }
                    }
                    mFPSyncLock = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    //最值模块
    private void initMaxAndMin() {
        mMaxEText = (EditText) mParameterLayout.findViewById(R.id.edit_max_edittext);
        mMaxEText.setText("0.01");
        mMinEText = (EditText) mParameterLayout.findViewById(R.id.edit_min_edittext);
        mMinEText.setText("0.01");
        //TODO 不同步模式，对称模式，积分为0模式
    }

    //重复次数
    private void initRepetition() {
        mRepetitionLock = false;
        mRepetitionEText = (EditText) mParameterLayout.findViewById(R.id.edit_rep_edittext);
        mRepetitionEText.setText("10");
    }

    //转折点们
    private void initTurningTime() {
        //先创建适配器
        View mTurningTimeLayout;
        mTurningTimeLayout = (View) mParameterLayout.findViewById(R.id.edit_tt0);
        mTurningTimeAdapter[0] = new CreateCardTurningTimeAdapter(mContext, mTurningTimeLayout, 0);
        mTurningTimeLayout = (View) mParameterLayout.findViewById(R.id.edit_tt1);
        mTurningTimeAdapter[1] = new CreateCardTurningTimeAdapter(mContext, mTurningTimeLayout, 1);
        mTurningTimeLayout = (View) mParameterLayout.findViewById(R.id.edit_tt2);
        mTurningTimeAdapter[2] = new CreateCardTurningTimeAdapter(mContext, mTurningTimeLayout, 2);
        //设置回调接口
        mTurningTimeAdapter[0].setRangeCallBack(new CreateCardTurningTimeAdapter.RangeCallBack() {
            @Override
            public int getMinimun() {
                return 1;
            }
            @Override
            public int getMaximun() {
                return mTurningTimeAdapter[1].getPermillage() - 1;
            }
        });
        mTurningTimeAdapter[1].setRangeCallBack(new CreateCardTurningTimeAdapter.RangeCallBack() {
            @Override
            public int getMinimun() {
                return mTurningTimeAdapter[0].getPermillage() + 1;
            }
            @Override
            public int getMaximun() {
                return mTurningTimeAdapter[2].getPermillage() - 1;
            }
        });
        mTurningTimeAdapter[2].setRangeCallBack(new CreateCardTurningTimeAdapter.RangeCallBack() {
            @Override
            public int getMinimun() {
                return mTurningTimeAdapter[1].getPermillage() + 1;
            }
            @Override
            public int getMaximun() {
                return 999;
            }
        });
    }

    public boolean isEmpty() {
        //频率和周期
        String sf = mFrequencyEText.getText().toString();
        String sT = mPeriodEText.getText().toString();
        if (!sf.equals("") && !sT.equals("") && !sf.equals("10") && !sT.equals("0.1")) {
            return false;
        }
        //最值
        String sMax = mMaxEText.getText().toString();
        if (!sMax.equals("") && !sMax.equals("0.01")) {
            return false;
        }
        String sMin = mMinEText.getText().toString();
        if (!sMin.equals("") && !sMin.equals("0.01")) {
            return false;
        }
        //重复次数
        String sRep = mRepetitionEText.getText().toString();
        if (!sRep.equals("") && !sRep.equals("10")) {
            return false;
        }
        //转折点们
        if (mTurningTimeAdapter[0].getPermillage() != 100) {
            return false;
        }
        if (mTurningTimeAdapter[1].getPermillage() != 200) {
            return false;
        }
        if (mTurningTimeAdapter[2].getPermillage() != 300) {
            return false;
        }
        return true;
    }

    /*
    public static class ParameterAdapterException extends RuntimeException {
    }
    */
}
