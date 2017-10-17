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
    //选通道部分
    private CreateCardSelectAdapter mSelectAdapter;
    //参数设置部分
    private CreateCardParameterAdapter mParameterAdapter;


    public CreateCardWindow(Context context, final int cardType) {
        mContext = context;
        mCardType = cardType;
        //设置弹出窗口的根基属性
        mContentView = LayoutInflater.from(mContext).inflate(R.layout.create_card_window, null);
        mPopupWindow = new PopupWindow(mContentView, GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT, true);
        mPopupWindow.setContentView(mContentView);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0xDFFFFFFF));
        mPopupWindow.setAnimationStyle(R.style.DeviceWindowAnimation);
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
            mSelectAdapter = new CreateCardSelectAdapter(mSelectLayout, CreateCardSelectAdapter.MODE_SINGLE);
        } else {
            mSelectAdapter = new CreateCardSelectAdapter(mSelectLayout, CreateCardSelectAdapter.MODE_MULTI);
        }
        //SendCard专享的参数设置部分
        View mParameterLayout = (View) mContentView.findViewById(R.id.edit_parameter_layout);
        if (mCardType != MODE_SENDCARD) {
            mParameterLayout.setVisibility(View.GONE);
        } else {
            mParameterLayout.setVisibility(View.VISIBLE);
            mParameterAdapter = new CreateCardParameterAdapter(mContext, mParameterLayout);
        }
        //提交按钮：检查并提交
        Button mSubmitBtn = (Button) mContentView.findViewById(R.id.edit_submit);
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPopupWindow();
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

    //获取广播的action
    private String getBroadcastAction() {
        if (mCardType == MODE_MONITORCARD) {
            return Action_CreateMonitorCard;
        } else if (mCardType == MODE_SAVECARD) {
            return Action_CreateSaveCard;
        } else if (mCardType == MODE_SENDCARD) {
            return Action_CreateSendCard;
        } else {
            return "";
        }
    }
    //发广播时，获取卡片标题
    private String getBroadcastCardTag() {
        String cardTag = mTagEditText.getText().toString().trim();
        if (cardTag.length() != 0) {
            return cardTag;
        } else if (mCardType == MODE_MONITORCARD) {
            return "No tag Monitor";
        } else if (mCardType == MODE_SAVECARD) {
            return "No tag Saver";
        } else if (mCardType == MODE_SENDCARD) {
            return "No tag Sender";
        } else {
            return "No tag";
        }
    }

    //把弹窗显示出来
    public void show() {
        //背景高斯模糊
        Bitmap bmp1 = BlurBitmap.printScreen(mContext);
        Bitmap bmp2 = BlurBitmap.blurBitmap(mContext, bmp1, 15.0f);
        mContentView.findViewById(R.id.creat_card_window).setBackgroundDrawable(new BitmapDrawable(bmp2));
        //在屏幕中央显示
        mPopupWindow.showAtLocation(((Activity) mContext).getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }
    //提交
    public void submitPopupWindow() {
        ///一边创建广播，一边检测输入的合法性
        Intent intent = new Intent(getBroadcastAction());
        //卡片的标题
        intent.putExtra("Title", getBroadcastCardTag());
        //单选或多选通道
        if (mCardType == MODE_MONITORCARD || mCardType == MODE_SENDCARD) {
            int selectedChannelId = mSelectAdapter.getSelectedItemId();
            if (selectedChannelId < 0 || selectedChannelId >= WorkFlow.channelNumber) {
                new CreateCardIgnoreFragment("Please choose ONE channel.").show(((Activity) mContext).getFragmentManager(), "");
                return;
            }
            if (mCardType == MODE_SENDCARD) {
                if (WorkFlow.channelHasSendThread[selectedChannelId]) {
                    new CreateCardIgnoreFragment("Please choose a UNSELECTED channel.").show(((Activity) mContext).getFragmentManager(), "");
                    return;
                }
                WorkFlow.channelHasSendThread[selectedChannelId] = true;
            }
            intent.putExtra("Channel", selectedChannelId);
        } else if (mCardType == MODE_SAVECARD) {
            if (mSelectAdapter.getSelectedItemCount() == 0) {
                new CreateCardIgnoreFragment("Please choose AT LEAST ONE channel.").show(((Activity) mContext).getFragmentManager(), "");
                return;
            }
            intent.putExtra("ChannelList", mSelectAdapter.getStateInArray());
        }
        //参数单独广播
        if (mCardType == MODE_SENDCARD) {
            intent=mParameterAdapter.makeBroadcast(mSelectAdapter.getSelectedItemId(),intent);
            intent = mParameterAdapter.makeBroadcast(mSelectAdapter.getSelectedItemId(), intent);
        }
        //发广播
        LocalBroadcastManager.getInstance(MyApplication.getContext()).sendBroadcast(intent);

        //发完广播把弹窗关了
        mPopupWindow.dismiss();
    }
    //检测弹窗是否什么都没填
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
            if (mParameterAdapter != null && !mParameterAdapter.isEmpty()) {
                return false;
            }
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

    //专属异常
    public static class CreateCardWindowException extends RuntimeException {
    }
}
