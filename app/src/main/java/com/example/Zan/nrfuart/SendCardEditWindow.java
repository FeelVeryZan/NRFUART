package com.example.Zan.nrfuart;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.Toast;

/**
 * Created by Administrator_nodgd on 2017/09/24.
 */

public class SendCardEditWindow {

    public static String TAG = "SendCardEditWindow";

    private Context mContext;
    private View mContentView;
    private PopupWindow mPopupWindow;
    private Button mSubmitBtn;
    private Button mCloseBtn;
    private SendCardAdapter.OnEditCallBack mCallBack;

    public SendCardEditWindow(Context context) {
        mContext = context;
        //设置弹出窗口的属性
        mContentView = LayoutInflater.from(mContext).inflate(R.layout.send_card_edit_window, null);
        mPopupWindow = new PopupWindow(mContentView, GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT, true);
        mPopupWindow.setContentView(mContentView);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0xDFFFFFFF));
        mPopupWindow.setAnimationStyle(R.style.DeviceWindowAnimation);

        mSubmitBtn = (Button) mContentView.findViewById(R.id.edit_submit);
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "...", Toast.LENGTH_SHORT).show();
                mCallBack.onEdit(); //TODO 把改动的具体内容从这里通过参数传递回去
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
    }

    private boolean isWindowEmpty() {
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

    public void show(SendCardAdapter.OnEditCallBack callBack) {
        mCallBack = callBack;
        //背景高斯模糊
        Bitmap bmp1 = BlurBitmap.printScreen(mContext);
        Bitmap bmp2 = BlurBitmap.blurBitmap(mContext, bmp1, 15.0f);
        mContentView.findViewById(R.id.creat_card_window).setBackgroundDrawable(new BitmapDrawable(bmp2));
        mPopupWindow.showAtLocation(((Activity) mContext).getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }
}
