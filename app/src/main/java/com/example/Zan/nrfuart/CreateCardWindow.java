package com.example.Zan.nrfuart;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.util.List;

/**
 * Created by nodgd on 2017/09/17.
 */

public class CreateCardWindow {

    public static String TAG = "CreateCardWindow";
    public static int MODE_SAVECARD = 1;
    public static int MODE_SENDCARD = 2;
    public static int MODE_MONITORCARD = 3;

    private Context mContext;
    int mCardType = 0;

    private View mContentView;
    private PopupWindow mPopupWindow;
    private EditText mEditTitleText;
    private EditText mEditIdText;
    private RecyclerView mChooseRecView;
    private CreateCardChooseAdapter mChooseAdapter;
    private Button mChooseAllBtn;
    private Button mChooseNoneBtn;
    private Button mChooseInvertBtn;
    private Button mSubmitBtn;
    private Button mCloseBtn;

    public CreateCardWindow(Context context, int cardType) {
        mContext = context;
        mCardType = cardType;
        //设置弹出窗口的属性
        mContentView = LayoutInflater.from(mContext).inflate(R.layout.create_card_window, null);
        mPopupWindow = new PopupWindow(mContentView, GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setContentView(mContentView);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0xDFFFFFFF));
        mPopupWindow.setAnimationStyle(R.style.EditWindowAnimation);
        //获取窗口内部各个控件
        mEditTitleText = (EditText) mContentView.findViewById(R.id.edit_title);
        mEditIdText = (EditText) mContentView.findViewById(R.id.edit_id);
        mSubmitBtn = (Button) mContentView.findViewById(R.id.edit_submit);
        //提交按钮的事件
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mEditTitleText.getText().toString().trim();
                String id = mEditIdText.getText().toString().trim();
                //TODO 检测输入的合法性
                String radio = "发送广播：\n";
                radio = radio + "cardtype = " + mCardType + "\n";
                radio = radio + "title = " + title + "\n";
                radio = radio + "id = " + id + "\n";
                if (mCardType == MODE_SAVECARD) {
                    radio += mChooseAdapter.getRadio();
                }
                //TODO 发送广播
                Toast.makeText(mContext, radio, Toast.LENGTH_LONG).show();
                mPopupWindow.dismiss();
            }
        });
        //关闭按钮事件
        mCloseBtn = (Button) mContentView.findViewById(R.id.edit_close);
        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });
    }

    public void setOptions(List<String> options) {
        if(mCardType != MODE_SAVECARD) {
            Log.e(TAG, "这不是一个SaveCard的生成器，不知道哪个逗逼调用了不该调用的函数");
            return;
        }
        View view = mContentView.findViewById(R.id.edit_savecard);
        view.setVisibility(View.VISIBLE);
        mChooseRecView = (RecyclerView) mContentView.findViewById(R.id.edit_choose_recview);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, RecyclerView.VERTICAL);
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
        mPopupWindow.showAtLocation(((Activity)mContext).getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }
}
