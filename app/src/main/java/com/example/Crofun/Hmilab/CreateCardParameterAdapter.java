package com.example.Crofun.Hmilab;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

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
    private double mFrequencyValue;
    private String mFrequencyStr;
    private EditText mPeriodEText;
    private double mPeriodValue;
    private String mPeriodStr;
    private boolean mFPSyncLock;
    //最值
    private EditText mMaxEText;
    private double mMaxValue;
    private EditText mMinEText;
    private double mMinValue;
    //重复次数
    private EditText mRepetitionEText;
    private int mRepetitionValue;
    //转折点们
    private CreateCardTurningTimeAdapter[] mTurningTimeAdapter = new CreateCardTurningTimeAdapter[3];
    private View mDuctCycleReturnZero[] = new View[3];
    //预览图
    private LineChartView mPreviewChart;


    public CreateCardParameterAdapter(Context context, View parameterLayout) {
        mContext = context;
        mParameterLayout = parameterLayout;
        initFrequency();
        initPeriod();
        initMaxAndMin();
        initRepetition();
        initTurningTime();
        initDutyReturn();
        initPreviewChart();
    }

    //频率模块
    private void initFrequency() {
        mFPSyncLock = false;
        mFrequencyValue = 10;
        mFrequencyEText = (EditText) mParameterLayout.findViewById(R.id.edit_f_edittext);
        mFrequencyEText.setText("" + mFrequencyValue);
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
                            mFrequencyValue = f;
                            CreateCardParameterAdapter.this.setNewPeriod(1.0 / f);
                            str = new DecimalFormat("#.######").format(1.0 / mFrequencyValue);
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
        mPeriodValue = 0.1;
        mPeriodEText = (EditText) mParameterLayout.findViewById(R.id.edit_t_edittext);
        mPeriodEText.setText("" + mPeriodValue);
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
                        mPeriodEText.setText("" + mPeriodStr);
                    } else {
                        try {
                            double T = Double.parseDouble(str);
                            if (T < 1e-6 || T > 1e6) {
                                throw new NumberFormatException();
                            }
                            CreateCardParameterAdapter.this.setNewPeriod(T);
                            str = new DecimalFormat("#.######").format(1.0 / mPeriodValue);
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

    //设置新周期后改变转折点显示和重新绘制预览图
    private void setNewPeriod(double T) {
        CreateCardTurningTimeAdapter.setRangeCheckingFlag(false);
        mTurningTimeAdapter[0].setPeriod(T);
        mTurningTimeAdapter[1].setPeriod(T);
        mTurningTimeAdapter[2].setPeriod(T);
        CreateCardTurningTimeAdapter.setRangeCheckingFlag(true);
        mPeriodValue = T;
    }

    //最值模块
    private void initMaxAndMin() {
        mMaxValue = 0.01;
        mMaxEText = (EditText) mParameterLayout.findViewById(R.id.edit_max_edittext);
        mMaxEText.setText("" + mMaxValue);
        mMaxEText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString();
                if (str.length() > 0 && str.length() <= 8) {
                    try {
                        double m = Double.parseDouble(str);
                        if (m < 1e-6 || m > 1e6) {
                            throw new NumberFormatException();
                        }
                        mMaxValue = m;
                        redrawPreviewChart();
                    } catch (NumberFormatException e) {
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mMinValue = 0.01;
        mMinEText = (EditText) mParameterLayout.findViewById(R.id.edit_min_edittext);
        mMinEText.setText("" + mMinValue);
        mMinEText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString();
                if (str.length() > 0 && str.length() <= 8) {
                    try {
                        double m = Double.parseDouble(str);
                        if (m < 1e-6 || m > 1e6) {
                            throw new NumberFormatException();
                        }
                        mMinValue = m;
                        redrawPreviewChart();
                    } catch (NumberFormatException e) {
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    //重复次数
    private void initRepetition() {
        mRepetitionValue = 10;
        mRepetitionEText = (EditText) mParameterLayout.findViewById(R.id.edit_rep_edittext);
        mRepetitionEText.setText("" + mRepetitionValue);
        mRepetitionEText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString();
                if (str.length() > 0 && str.length() <= 8) {
                    try {
                        int r = Integer.parseInt(str);
                        if (r < 0 || r > 1e6) {
                            throw new NumberFormatException();
                        }
                        mRepetitionValue = r;
                        redrawPreviewChart();
                    } catch (NumberFormatException e) {
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    //转折点们
    private void initTurningTime() {
        //先创建适配器
        View mTurningTimeLayout;
        mTurningTimeLayout = (View) mParameterLayout.findViewById(R.id.edit_tt0);
        mTurningTimeAdapter[0] = new CreateCardTurningTimeAdapter(mContext, mTurningTimeLayout, 0, mPeriodValue);
        mTurningTimeLayout = (View) mParameterLayout.findViewById(R.id.edit_tt1);
        mTurningTimeAdapter[1] = new CreateCardTurningTimeAdapter(mContext, mTurningTimeLayout, 1, mPeriodValue);
        mTurningTimeLayout = (View) mParameterLayout.findViewById(R.id.edit_tt2);
        mTurningTimeAdapter[2] = new CreateCardTurningTimeAdapter(mContext, mTurningTimeLayout, 2, mPeriodValue);
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

    //占空比归零
    private void initDutyReturn() {
        mDuctCycleReturnZero[0] = mParameterLayout.findViewById(R.id.edit_dcrz_0);
        mDuctCycleReturnZero[1] = mParameterLayout.findViewById(R.id.edit_dcrz_1);
        mDuctCycleReturnZero[2] = mParameterLayout.findViewById(R.id.edit_dcrz_2);
        mDuctCycleReturnZero[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int per0 = mTurningTimeAdapter[0].getPermillage();
                int per1 = mTurningTimeAdapter[1].getPermillage();
                int per2 = mTurningTimeAdapter[2].getPermillage();
                if (! mTurningTimeAdapter[0].setPermillage(per2 - per1)) {
                    Toast.makeText(mContext, "Failed to modify #1.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mDuctCycleReturnZero[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int per0 = mTurningTimeAdapter[0].getPermillage();
                int per1 = mTurningTimeAdapter[1].getPermillage();
                int per2 = mTurningTimeAdapter[2].getPermillage();
                if (! mTurningTimeAdapter[1].setPermillage(per2 - per0)) {
                    Toast.makeText(mContext, "Failed to modify #2.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mDuctCycleReturnZero[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int per0 = mTurningTimeAdapter[0].getPermillage();
                int per1 = mTurningTimeAdapter[1].getPermillage();
                int per2 = mTurningTimeAdapter[2].getPermillage();
                if (! mTurningTimeAdapter[2].setPermillage(per0 + per1)) {
                    Toast.makeText(mContext, "Failed to modify #3.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //预览图
    private void initPreviewChart() {
        mPreviewChart = (LineChartView) mParameterLayout.findViewById(R.id.edit_preview_chart);
        mPreviewChart.setInteractive(true);
        mPreviewChart.setZoomType(ZoomType.HORIZONTAL);
        mPreviewChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        mPreviewChart.setFocusableInTouchMode(true);
        mPreviewChart.startDataAnimation();
        redrawPreviewChart();
        //给每个参数设置重新渲染的回调函数
        mTurningTimeAdapter[0].setRedrawPreviewCallBack(new RedrawPreviewCallBack() {
            @Override
            public void redraw() {
                redrawPreviewChart();
            }
        });
        mTurningTimeAdapter[1].setRedrawPreviewCallBack(new RedrawPreviewCallBack() {
            @Override
            public void redraw() {
                redrawPreviewChart();
            }
        });
        mTurningTimeAdapter[2].setRedrawPreviewCallBack(new RedrawPreviewCallBack() {
            @Override
            public void redraw() {
                redrawPreviewChart();
            }
        });
    }

    private void redrawPreviewChart() {
        Log.d(TAG, "redrawPreviewChart");
        List<Float> xValue = new ArrayList<>();
        //基本参数
        xValue.add(0f);
        xValue.add((float) mTurningTimeAdapter[0].getPermillage());
        xValue.add((float) mTurningTimeAdapter[1].getPermillage());
        xValue.add((float) mTurningTimeAdapter[2].getPermillage());
        xValue.add(1000f);
        float eps = 1e-6f;
        //线序列
        List<Line> lineList = new ArrayList<>();
        //生成主线
        List<PointValue> valueListMain = new ArrayList<>();
        valueListMain.add(new PointValue(xValue.get(0), 0.0f));
        valueListMain.add(new PointValue(xValue.get(0) + eps, (float) getMaximun()));
        valueListMain.add(new PointValue(xValue.get(1), (float) getMaximun()));
        valueListMain.add(new PointValue(xValue.get(1) + eps, 0.0f));
        valueListMain.add(new PointValue(xValue.get(2), 0.0f));
        valueListMain.add(new PointValue(xValue.get(2) + eps, (float) getMinimun()));
        valueListMain.add(new PointValue(xValue.get(3), (float) getMinimun()));
        valueListMain.add(new PointValue(xValue.get(3) + eps, 0.0f));
        valueListMain.add(new PointValue(xValue.get(4), 0.0f));
        Line lineMain = new Line(valueListMain);
        lineMain.setColor(R.color.ZanLine);
        lineMain.setCubic(false);       //直线或曲线
        lineMain.setFilled(false);      //填充与x轴之间的区域
        lineMain.setHasPoints(false);   //显示节点
        lineMain.setHasLabels(false);   //显示节点数据
        lineMain.setStrokeWidth(3);
        lineList.add(lineMain);
        //生成一条与x轴重合的卡位线，边缘扩充原长的20%
        List<PointValue> valueListRangeX = new ArrayList<>();
        valueListRangeX.add(new PointValue(-200.0f, 0));
        valueListRangeX.add(new PointValue(1200.0f, 0));
        Line lineRangeX = new Line(valueListRangeX);
        lineRangeX.setColor(R.color.ZanAxis);
        lineRangeX.setStrokeWidth(1);
        lineRangeX.setCubic(false);         //直线或曲线
        lineRangeX.setFilled(false);        //填充与x轴之间的区域
        lineRangeX.setHasPoints(false);     //显示节点
        lineRangeX.setHasLabels(false);     //显示节点数据
        lineList.add(lineRangeX);
        //生成x轴，要经过y轴原点
        List<PointValue> valueListAxisX = new ArrayList<>();
        for (int i = 0; i < xValue.size(); i++) {
            valueListAxisX.add(new PointValue(xValue.get(i), 0).setLabel(new DecimalFormat("#.#%").format(xValue.get(i) * 0.001)));
        }
        Line lineAxisX = new Line(valueListAxisX);
        lineAxisX.setColor(R.color.ZanAxis);
        lineAxisX.setStrokeWidth(1);
        lineAxisX.setCubic(false);      //直线或曲线
        lineAxisX.setFilled(false);     //填充与x轴之间的区域
        lineAxisX.setHasPoints(true);   //显示节点
        lineAxisX.setPointRadius(2);    //节点半径
        lineAxisX.setHasLabels(false);   //显示节点数据
        lineList.add(lineAxisX);
        //生成一条与y轴重合的卡位线，边缘扩充原长的20%
        List<PointValue> valueListRangeY = new ArrayList<>();
        valueListRangeY.add(new PointValue(0, (float) (getMinimun() - 0.1 * (getMaximun() - getMinimun()))));
        valueListRangeY.add(new PointValue(0, (float) (getMaximun() + 0.1 * (getMaximun() - getMinimun()))));
        Line lineRangeY = new Line(valueListRangeY);
        lineRangeY.setColor(R.color.ZanAxis);
        lineRangeY.setStrokeWidth(1);
        lineRangeY.setCubic(false);         //直线或曲线
        lineRangeY.setFilled(false);        //填充与x轴之间的区域
        lineRangeY.setHasPoints(false);     //显示节点
        lineRangeY.setHasLabels(false);     //显示节点数据
        lineList.add(lineRangeY);
        /*
        //生成y轴，要经过x轴原点
        List<PointValue> valueListAxisY = new ArrayList<>();
        valueListAxisY.add(new PointValue(0, (float) getMinimun()).setLabel(new DecimalFormat("#.####V").format(getMinimun())));
        valueListAxisY.add(new PointValue(0, 0).setLabel("0"));
        valueListAxisY.add(new PointValue(0, (float) getMaximun()).setLabel(new DecimalFormat("#.####V").format(getMaximun())));
        Line lineAxisY = new Line(valueListAxisY);
        lineAxisY.setColor(R.color.ZanAxis);
        lineAxisY.setStrokeWidth(1);
        lineAxisY.setCubic(false);      //直线或曲线
        lineAxisY.setFilled(false);     //填充与x轴之间的区域
        lineAxisY.setHasPoints(true);   //显示节点
        lineAxisY.setPointRadius(2);    //节点半径
        lineAxisY.setHasLabels(false);   //显示节点数据
        lineList.add(lineAxisY);
        */
        //把这些线放在一起变成线序列
        //新建一个可以传入的数据，填入数据
        LineChartData lineChartData = new LineChartData();
        lineChartData.setLines(lineList);
        mPreviewChart.setLineChartData(lineChartData);
    }

    //获取频率
    public double getFrequency() {
        return mFrequencyValue;
    }
    //获取周期
    public double getPeriod() {
        return mPeriodValue;
    }
    //获取最大值
    public double getMaximun() {
        return mMaxValue;
    }
    //获取最小值
    public double getMinimun() {
        return -mMinValue;
    }
    //获取重复次数
    public int getRepetition() {
        return mRepetitionValue;
    }
    //获取cc0
    public double getCC0() {
        return (mTurningTimeAdapter[0].getPermillage() - 0) * 0.001 * mPeriodValue;
    }
    //获取cc1
    public double getCC1() {
        return (mTurningTimeAdapter[1].getPermillage() - mTurningTimeAdapter[0].getPermillage()) * 0.001 * mPeriodValue;
    }
    //获取cc2
    public double getCC2() {
        return (mTurningTimeAdapter[2].getPermillage() - mTurningTimeAdapter[1].getPermillage()) * 0.001 * mPeriodValue;
    }
    //获取cc3
    public double getCC3() {
        return (1000 - mTurningTimeAdapter[2].getPermillage()) * 0.001 * mPeriodValue;
    }

    //判断是否毛都没有
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
    //发广播
    public Intent makeBroadcast(final int cha,Intent intent) {
        Log.d(TAG, "nodgd send broadcast"+cha);
        //Intent intent = new Intent(SendRunner.NewData);
        intent.putExtra("cha", cha);
        intent.putExtra("cc0", getCC0());
        intent.putExtra("cc1", getCC1());
        intent.putExtra("cc2", getCC2());
        intent.putExtra("cc3", getCC3());
        intent.putExtra("cmx", getMaximun());
        intent.putExtra("cmn", getMinimun());
        return intent;
        //LocalBroadcastManager.getInstance(MyApplication.getContext()).sendBroadcast(intent);
    }

    public static interface RedrawPreviewCallBack {
        public void redraw();
    }
}
