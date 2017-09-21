package com.example.Zan.nrfuart;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;


public class WelcomeActivity extends BaseActivity {
    public static ImageView mGoToOldVersion = null;
    public static ImageView mTestFlow = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_layout);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mGoToOldVersion = (ImageView) findViewById(R.id.StartOldVersion);
        mGoToOldVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyApplication.getContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        mTestFlow = (ImageView) findViewById(R.id.testFlow);
        mTestFlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyApplication.getContext(), WorkFlow.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.popup_title)
                .setMessage(R.string.popup_message)
                .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCollecter.finishall();
                    }
                })
                .setNegativeButton(R.string.popup_no, null)
                .show();

    }


}
