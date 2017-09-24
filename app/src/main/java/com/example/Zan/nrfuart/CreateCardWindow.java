package com.example.Zan.nrfuart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.PopupWindow;

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

    private View mContentView;
    private PopupWindow mPopupWindow;
    private EditText mEditTitleText;
    private EditText mEditChannelText;
    private RecyclerView mChooseRecView;
    private CreateCardChooseAdapter mChooseAdapter;
    private Button mChooseAllBtn;
    private Button mChooseNoneBtn;
    private Button mChooseInvertBtn;
    private Button mSubmitBtn;
    private Button mCloseBtn;

    public CreateCardWindow(Context context, final int cardType) {
        mContext = context;
        mCardType = cardType;
        //设置弹出窗口的属性
        mContentView = LayoutInflater.from(mContext).inflate(R.layout.create_card_window, null);
        mPopupWindow = new PopupWindow(mContentView, GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT, true);
        mPopupWindow.setContentView(mContentView);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0xDFFFFFFF));
        mPopupWindow.setAnimationStyle(R.style.DeviceWindowAnimation);
        //获取窗口内部各个控件
        mEditTitleText = (EditText) mContentView.findViewById(R.id.edit_title);
        mEditChannelText = (EditText) mContentView.findViewById(R.id.edit_channel);
        mSubmitBtn = (Button) mContentView.findViewById(R.id.edit_submit);
        mContentView.findViewById(R.id.edit_savecard).setVisibility(View.GONE);
        //提交按钮的事件
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //检测输入的合法性
                //MonitorCard必须输入一个通道
                int channelInt = -1;
                if (mCardType == MODE_MONITORCARD) {
                    String channelText = mEditChannelText.getText().toString().trim();
                    try {
                        channelInt = Integer.parseInt(channelText);
                    } catch (NumberFormatException e) {
                        Log.d(TAG, "NumberFormatException " + e.getMessage());
                    }
                    if (channelInt < 0 || channelInt >= WorkFlow.channelNumber) {
                        CreateCardIgnoreFragment ignoreFragment = new CreateCardIgnoreFragment("Wrong channel id.");
                        ignoreFragment.show(((Activity) mContext).getFragmentManager(), "？蛤？");
                        return;
                    }
                }
                //SaveCard至少要选一个通道
                if (mCardType == MODE_SAVECARD) {
                    if (mChooseAdapter.getChooseItemCound() == 0) {
                        CreateCardIgnoreFragment ignoreFragment = new CreateCardIgnoreFragment("Please choose at least one channel.");
                        ignoreFragment.show(((Activity) mContext).getFragmentManager(), "？蛤？");
                        return;
                    }
                }
                //发广播
                String title = mEditTitleText.getText().toString().trim();
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
                    intent.putExtra("ChannelList", mChooseAdapter.getStateInArray());
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
        mContentView.findViewById(R.id.creat_card_main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    private boolean isWindowEmpty() {
        if (mEditTitleText != null && mEditTitleText.getText().toString().trim().length() > 0) {
            return false;
        }
        if (mEditChannelText != null && mEditChannelText.getText().toString().trim().length() > 0) {
            return false;
        }
        if (mCardType == MODE_SAVECARD && mChooseAdapter != null && mChooseAdapter.getChooseItemCound() > 0) {
            return false;
        }
        return true;
    }

    private void closePopupWindow() {
        if (isWindowEmpty()) {
            mPopupWindow.dismiss();
        } else {
            CreateCardDialogFragment dialogFragment = new CreateCardDialogFragment(mPopupWindow);
            dialogFragment.show(((Activity) mContext).getFragmentManager(), "？？？蛤？");
        }

    }

    private void closeInputMethodAnyaway() {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            //imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
            imm.hideSoftInputFromWindow(((Activity) mContext).getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void setOptions(List<String> options) {
        if (mCardType != MODE_SAVECARD) {
            Log.e(TAG, "这不是一个SaveCard的生成器，不知道哪个逗逼调用了不该调用的函数");
            return;
        }
        mContentView.findViewById(R.id.edit_savecard).setVisibility(View.VISIBLE);
        //mContentView.findViewById(R.id.edit_choose_fast).setVisibility(View.VISIBLE);
        mChooseRecView = (RecyclerView) mContentView.findViewById(R.id.edit_choose_recview);
        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 3, RecyclerView.VERTICAL, false);
        //StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, RecyclerView.VERTICAL);
        mChooseRecView.setLayoutManager(layoutManager);
        mChooseAdapter = new CreateCardChooseAdapter(options);
        mChooseRecView.setAdapter(mChooseAdapter);
        //三个按钮的事件
        mChooseAllBtn = (Button) mContentView.findViewById(R.id.edit_choose_all);
        mChooseAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChooseAdapter.chooseAll();
            }
        });
        mChooseNoneBtn = (Button) mContentView.findViewById(R.id.edit_choose_none);
        mChooseNoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChooseAdapter.chooseNone();
            }
        });
        mChooseInvertBtn = (Button) mContentView.findViewById(R.id.edit_choose_invert);
        mChooseInvertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChooseAdapter.chooseInvert();
            }
        });
    }

    public void show() {
        //背景高斯模糊
        Bitmap bmp1 = BlurBitmap.printScreen(mContext);
        Bitmap bmp2 = BlurBitmap.blurBitmap(mContext, bmp1, 15.0f);
        mContentView.findViewById(R.id.creat_card_window).setBackgroundDrawable(new BitmapDrawable(bmp2));
        mPopupWindow.showAtLocation(((Activity) mContext).getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }
}
