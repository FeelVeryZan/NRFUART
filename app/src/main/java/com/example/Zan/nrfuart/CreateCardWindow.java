package com.example.Zan.nrfuart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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
    String[] cardTypeStr = {"", "SaveCard", "SendCard", "MonitorCard"};

    //窗口共享部分
    private View mContentView;
    private PopupWindow mPopupWindow;
    private TextView mWindowTitle;
    private EditText mTitleEditText;
    private LinearLayout mSingleSelectLayout;
    private LinearLayout mMultiSelectLayout;
    private View mParameterLayout;
    private Button mSubmitBtn;
    private Button mCloseBtn;
    private List<String> mOptionList;
    //单选通道部分
    private RecyclerView mSingleSelectRecView;
    private LinearLayoutManager mSingleSelectLayoutManager;
    private CreateCardSingleSelectAdapter mSingleSelectAdapter;
    //多选通道部分
    private RecyclerView mMultiSelectRecView;
    private GridLayoutManager mMultiSelectLayoutManager;
    private CreateCardMultiSelectAdapter mMultiSelectAdapter;
    //参数设置部分

    private Button mChooseAllBtn;
    private Button mChooseNoneBtn;
    private Button mChooseInvertBtn;

    public CreateCardWindow(Context context, final int cardType) {
        mContext = context;
        mCardType = cardType;
        //设置弹出窗口的属性
        mContentView = LayoutInflater.from(mContext).inflate(R.layout.create_card_window, null);
        mPopupWindow = new PopupWindow(mContentView, GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT, true);
        mPopupWindow.setContentView(mContentView);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0xDFFFFFFF));
        mPopupWindow.setAnimationStyle(R.style.DeviceWindowAnimation);
        //共享部分和专享部分
        initSharedPart();
        initSelectPart();
        initSendCard();

        Log.d(TAG, "cardType = " + cardType);
    }

    //共享部分
    private void initSharedPart() {
        //窗口的标题
        mWindowTitle = (TextView) mContentView.findViewById(R.id.edit_window_title);
        try {
            mWindowTitle.setText("Create a " + cardTypeStr[mCardType]);
        } catch (IndexOutOfBoundsException e) {
            mWindowTitle.setText("Create a ????Card");
        }
        //获取控件
        mTitleEditText = (EditText) mContentView.findViewById(R.id.edit_title);
        mSingleSelectLayout = (LinearLayout) mContentView.findViewById(R.id.edit_singleselect_layout);
        mMultiSelectLayout = (LinearLayout) mContentView.findViewById(R.id.edit_multiselect_layout);
        mParameterLayout = (View) mContentView.findViewById(R.id.edit_parameter_layout);
        mSubmitBtn = (Button) mContentView.findViewById(R.id.edit_submit);
        //提交按钮的事件
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //检测输入的合法性
                //MonitorCard和SendCard都必须选一个通道
                int channelInt = -1;
                if (mCardType == MODE_MONITORCARD || mCardType == MODE_SENDCARD) {
                    channelInt = mSingleSelectAdapter.getSelectedItemId();
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
                    if (mMultiSelectAdapter.getChooseItemCound() == 0) {
                        CreateCardIgnoreFragment ignoreFragment = new CreateCardIgnoreFragment("Please choose AT LEAST ONE channel.");
                        ignoreFragment.show(((Activity) mContext).getFragmentManager(), "？蛤？");
                        return;
                    }
                }
                //发广播
                Log.d(TAG, "发广播");
                String title = mTitleEditText.getText().toString().trim();
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
                    intent.putExtra("ChannelList", mMultiSelectAdapter.getStateInArray());
                } else {
                    intent.putExtra("cha", channelInt);
                }
                LocalBroadcastManager.getInstance(MyApplication.getContext()).sendBroadcast(intent);
                //发完广播把弹窗关了
                mPopupWindow.dismiss();
            }
        });
        //关闭按钮事件：尝试关闭
        mCloseBtn = (Button) mContentView.findViewById(R.id.edit_close);
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
        //通道名称数组
        mOptionList = new ArrayList<>();
        for (int i = 0; i < WorkFlow.channelNumber; i++) {
            mOptionList.add("channel #" + i);
        }
    }

    //设置单选或多选
    private void initSelectPart() {
        if (mCardType == MODE_MONITORCARD || mCardType == MODE_SENDCARD) {
            //可见性
            mSingleSelectLayout.setVisibility(View.VISIBLE);
            mMultiSelectLayout.setVisibility(View.GONE);
            //单选模块
            mSingleSelectRecView = (RecyclerView) mContentView.findViewById(R.id.edit_mo_choose_recview);
            mSingleSelectLayoutManager = new LinearLayoutManager(mContext);
            mSingleSelectLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            //mSingleSelectLayoutManager = new FullyLinearLayoutManager(mContext);
            mSingleSelectRecView.setLayoutManager(mSingleSelectLayoutManager);
            mSingleSelectAdapter = new CreateCardSingleSelectAdapter(mSingleSelectRecView, mOptionList);
            mSingleSelectRecView.setAdapter(mSingleSelectAdapter);
        } else {
            //可见性
            mSingleSelectLayout.setVisibility(View.GONE);
            mMultiSelectLayout.setVisibility(View.VISIBLE);
            //多选模块
            mMultiSelectRecView = (RecyclerView) mContentView.findViewById(R.id.edit_sa_choose_recview);
            mMultiSelectLayoutManager = new GridLayoutManager(mContext, 2, RecyclerView.HORIZONTAL, false);
            mMultiSelectRecView.setLayoutManager(mMultiSelectLayoutManager);
            mMultiSelectAdapter = new CreateCardMultiSelectAdapter(mOptionList);
            mMultiSelectRecView.setAdapter(mMultiSelectAdapter);
            //三个快捷操作按钮的事件
            mChooseAllBtn = (Button) mContentView.findViewById(R.id.edit_sa_choose_all);
            mChooseAllBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMultiSelectAdapter.chooseAll();
                }
            });
            mChooseNoneBtn = (Button) mContentView.findViewById(R.id.edit_sa_choose_none);
            mChooseNoneBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMultiSelectAdapter.chooseNone();
                }
            });
            mChooseInvertBtn = (Button) mContentView.findViewById(R.id.edit_sa_choose_invert);
            mChooseInvertBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMultiSelectAdapter.chooseInvert();
                }
            });
        }
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
        if (mTitleEditText != null && mTitleEditText.getText().toString().trim().length() > 0) {
            return false;
        }
        //检测MonitorCard专享部分
        if (mCardType == MODE_MONITORCARD) {
            if (mSingleSelectAdapter != null && mSingleSelectAdapter.getSelectedItemId() != -1) {
                return false;
            }
        }
        //检测SaveCard专享部分
        if (mCardType == MODE_SAVECARD) {
            if (mMultiSelectAdapter != null && mMultiSelectAdapter.getChooseItemCound() > 0) {
                return false;
            }
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
}
