package com.example.Zan.nrfuart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * Created by nodgd on 2017/09/17.
 */

public class CreateCardWindow {

    public static String TAG = "CreateCardWindow";
    public static int MODE_SAVECARD = 1;
    public static int MODE_SENDCARD = 2;
    public static int MODE_MONITORCARD = 3;

    public final static String Action_CreateSaveCard = "com.example.Zan.nrfuart.Action_CreateSaveCard";
    public final static String Action_CreateSendCard = "com.example.Zan.nrfuart.Action_CreateSendCard";
    public final static String Action_CreateMonitorCard = "com.example.Zan.nrfuart.Action_CreateMonitorCard";


    private Context mContext;
    int mCardType = 0;
    String[] cardTypeStr = {"", "Saver", "Sender", "Monitor"};

    //窗口根基部分
    private View mContentView;
    private PopupWindow mPopupWindow;
    //卡片标题输入框
    private EditText mTagEditText;
    private View mParameterLayout;
    //选通道部分
    private SelectAdapter mSelectAdapter;
    //参数设置部分

    private Button mChooseAllBtn;
    private Button mChooseNoneBtn;
    private Button mChooseInvertBtn;

    public CreateCardWindow(Context context, final int cardType) {
        mContext = context;
        mCardType = cardType;
        //设置弹出窗口的根基属性
        mContentView = LayoutInflater.from(mContext).inflate(R.layout.create_card_window, null);
        mPopupWindow = new PopupWindow(mContentView, GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT, true);
        mPopupWindow.setContentView(mContentView);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0xDFFFFFFF));
        mPopupWindow.setAnimationStyle(R.style.DeviceWindowAnimation);
        //共享部分
        initBasePart();
        initSendCard();
    }

    //公用部分
    private void initBasePart() {
        //窗口的标题
        TextView mWindowTitle = (TextView) mContentView.findViewById(R.id.edit_window_title);
        if (mCardType == MODE_SAVECARD) {
            mWindowTitle.setText("Create a Saver");
        } else if (mCardType == MODE_SENDCARD) {
            mWindowTitle.setText("Create a Sender");
        } else if (mCardType == MODE_MONITORCARD) {
            mWindowTitle.setText("Create a Monitor");
        } else {
            Log.e(TAG, "错误的卡片格式");
            throw new CreateCardWindowException();
        }
        //卡片标题输入框
        mTagEditText = (EditText) mContentView.findViewById(R.id.edit_title);
        //单选模块or多选模块
        View mSelectLayout = (View) mContentView.findViewById(R.id.edit_select_layout);
        if (mCardType == MODE_MONITORCARD || mCardType == MODE_SENDCARD) {
            mSelectAdapter = new SelectAdapter(mSelectLayout, SelectAdapter.MODE_SINGLE);
        } else {
            mSelectAdapter = new SelectAdapter(mSelectLayout, SelectAdapter.MODE_MULTI);
        }
        //TODO 刚才改到这个位置



        mParameterLayout = (View) mContentView.findViewById(R.id.edit_parameter_layout);
        //提交按钮
        Button mSubmitBtn = (Button) mContentView.findViewById(R.id.edit_submit);
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //检测输入的合法性
                //MonitorCard和SendCard都必须选一个通道
                int channelInt = -1;
                if (mCardType == MODE_MONITORCARD || mCardType == MODE_SENDCARD) {
                    channelInt = mSelectAdapter.getSelectedItemId();
                    if (channelInt < 0 || channelInt >= WorkFlow.channelNumber) {
                        CreateCardIgnoreFragment ignoreFragment = new CreateCardIgnoreFragment("Please choose ONE channel.");
                        ignoreFragment.show(((Activity) mContext).getFragmentManager(), "？蛤？");
                        return;
                    }
                }
                //SendCard必须选一个没有被选过的通道
                if (mCardType == MODE_SENDCARD) {
                    if (WorkFlow.channelHasSendThread[channelInt]) {
                        CreateCardIgnoreFragment ignoreFragment = new CreateCardIgnoreFragment("Please choose a UNSELECTED channel.");
                        ignoreFragment.show(((Activity) mContext).getFragmentManager(), "？蛤？");
                        return;
                    } else {
                        WorkFlow.channelHasSendThread[channelInt] = true;
                    }
                }
                //SaveCard至少要选一个通道
                if (mCardType == MODE_SAVECARD) {
                    if (mSelectAdapter.getSelectedItemCount() == 0) {
                        CreateCardIgnoreFragment ignoreFragment = new CreateCardIgnoreFragment("Please choose AT LEAST ONE channel.");
                        ignoreFragment.show(((Activity) mContext).getFragmentManager(), "？蛤？");
                        return;
                    }
                }
                //发广播
                Log.d(TAG, "发广播");
                String title = mTagEditText.getText().toString().trim();
                String action = "";
                if (mCardType == MODE_MONITORCARD) {
                    action = Action_CreateMonitorCard;
                } else if (mCardType == MODE_SAVECARD) {
                    action = Action_CreateSaveCard;
                } else if (mCardType == MODE_SENDCARD) {
                    action = Action_CreateSendCard;
                }
                final Intent intent = new Intent(action);
                intent.putExtra("Id", channelInt);
                intent.putExtra("Title", title);
                if (mCardType == MODE_SAVECARD) {
                    intent.putExtra("ChannelList", mSelectAdapter.getStateInArray());
                } else {
                    intent.putExtra("cha", channelInt);
                }
                LocalBroadcastManager.getInstance(MyApplication.getContext()).sendBroadcast(intent);
                //发完广播把弹窗关了
                mPopupWindow.dismiss();
            }
        });
        //关闭按钮事件：尝试关闭
        Button mCloseBtn = (Button) mContentView.findViewById(R.id.edit_close);
        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePopupWindow();
            }
        });
        //弹窗界面的外部点击事件：尝试关闭
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePopupWindow();
            }
        });
        //弹窗界面的空白处点击事件：空事件。否则会响应外部点击事件
        mContentView.findViewById(R.id.creat_card_window).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }




    //SendCard专享部分
    private void initSendCard() {
        //设置可见性
        if (mCardType == MODE_SENDCARD) {
            mParameterLayout.setVisibility(View.VISIBLE);
            //频率周期二选一

            //TODO


        } else {
            mParameterLayout.setVisibility(View.GONE);
        }
    }

    //检测弹窗是否没有包含任何信息
    private boolean isWindowEmpty() {
        //检测共享部分
        if (mTagEditText != null && mTagEditText.getText().toString().trim().length() > 0) {
            return false;
        }
        //检测单选或多选
        if (mSelectAdapter != null && !mSelectAdapter.isEmpty()) {
            return false;
        }
        //检测SendCard专享部分
        if (mCardType == MODE_SENDCARD) {

        }
        return true;
    }

    //关闭之前检测是否空
    private void closePopupWindow() {
        if (isWindowEmpty()) {
            mPopupWindow.dismiss();
        } else {
            CreateCardDialogFragment dialogFragment = new CreateCardDialogFragment(mPopupWindow);
            dialogFragment.show(((Activity) mContext).getFragmentManager(), "？蛤？");
        }
    }

    public void show() {
        //背景高斯模糊
        Bitmap bmp1 = BlurBitmap.printScreen(mContext);
        Bitmap bmp2 = BlurBitmap.blurBitmap(mContext, bmp1, 15.0f);
        mContentView.findViewById(R.id.creat_card_window).setBackgroundDrawable(new BitmapDrawable(bmp2));
        //在屏幕中央显示
        mPopupWindow.showAtLocation(((Activity) mContext).getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }

    //单选模块或多选模块的适配器
    public static class SelectAdapter {
        public static String TAG = "SelectViewHolderTag";

        public static int MODE_SINGLE = 1;
        public static int MODE_MULTI = 2;

        private int mSelectMode;
        private CheckBox mCheckBox[] = new CheckBox[4];
        private int mSelectNow;     //这个变量单选模式才有用

        public SelectAdapter(View selectView, int selectMode) {
            mSelectMode = selectMode;
            if (mSelectMode != MODE_SINGLE && mSelectMode != MODE_MULTI) {
                Log.e(TAG, "错误的selectMode");
                throw new SelectAdapterException();
            }
            mSelectNow = -1;
            //获取每一个CheckBox控件
            mCheckBox[0] = (CheckBox) selectView.findViewById(R.id.edit_select_0).findViewById(R.id.edit_select_checkbox);
            mCheckBox[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    oneBoxChanged(0, isChecked);
                }
            });
            mCheckBox[1] = (CheckBox) selectView.findViewById(R.id.edit_select_1).findViewById(R.id.edit_select_checkbox);
            mCheckBox[1].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    oneBoxChanged(1, isChecked);
                }
            });
            mCheckBox[2] = (CheckBox) selectView.findViewById(R.id.edit_select_2).findViewById(R.id.edit_select_checkbox);
            mCheckBox[2].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    oneBoxChanged(2, isChecked);
                }
            });
            mCheckBox[3] = (CheckBox) selectView.findViewById(R.id.edit_select_3).findViewById(R.id.edit_select_checkbox);
            mCheckBox[3].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    oneBoxChanged(3, isChecked);
                }
            });
            //给每个通道选项改名字
            TextView mChannelName;
            mChannelName = (TextView) selectView.findViewById(R.id.edit_select_0).findViewById(R.id.edit_select_channelname);
            mChannelName.setText("#0");
            mChannelName = (TextView) selectView.findViewById(R.id.edit_select_1).findViewById(R.id.edit_select_channelname);
            mChannelName.setText("#1");
            mChannelName = (TextView) selectView.findViewById(R.id.edit_select_2).findViewById(R.id.edit_select_channelname);
            mChannelName.setText("#2");
            mChannelName = (TextView) selectView.findViewById(R.id.edit_select_3).findViewById(R.id.edit_select_channelname);
            mChannelName.setText("#3");
            //处理介绍文字
            TextView mDescription = (TextView) selectView.findViewById(R.id.edit_select_description);
            if (mSelectMode == MODE_SINGLE) {
                mDescription.setText("Choose one channel:");
            } else {
                mDescription.setText("Choose channel(s) to save:");
            }
            //处理三个快速选择按钮
            View mBtns = (View) selectView.findViewById(R.id.edit_select_btns);
            if (mSelectMode == MODE_SINGLE) {
                mBtns.setVisibility(View.GONE);
            } else {
                Button mAll = (Button) selectView.findViewById(R.id.edit_select_all);
                mAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectAll();
                    }
                });
                Button mNone = (Button) selectView.findViewById(R.id.edit_select_none);
                mNone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectNone();
                    }
                });
                Button mOther = (Button) selectView.findViewById(R.id.edit_select_others);
                mOther.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectOthers();
                    }
                });
            }
        }

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
        private void selectAll() {
            for (int i = 0; i < 4; i++) {
                mCheckBox[i].setChecked(true);
            }
        }
        private void selectNone() {
            for (int i = 0; i < 4; i++) {
                mCheckBox[i].setChecked(false);
            }
        }
        private void selectOthers() {
            for (int i = 0; i < 4; i++) {
                mCheckBox[i].setChecked(!mCheckBox[i].isChecked());
            }
        }

        public int getSelectedItemId() {
            if (mSelectMode == MODE_MULTI) {
                throw new SelectAdapterException();
            } else {
                return mSelectNow;
            }
        }
        public int getSelectedItemCount() {
            if (mSelectMode == MODE_SINGLE) {
                throw new SelectAdapterException();
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
        public int[] getStateInArray() {
            if (mSelectMode == MODE_SINGLE) {
                throw new SelectAdapterException();
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

        public static class SelectAdapterException extends RuntimeException {
        }
    }
    public static class CreateCardWindowException extends RuntimeException {
    }
}
