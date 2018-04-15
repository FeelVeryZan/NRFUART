package com.example.Crofun.Hmilab;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;


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

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            Log.d("BLE", getString(R.string.ble_not_supported));
            finish();
        }
        else
        {
            Toast.makeText(this, "BLE is supported by this device.", Toast.LENGTH_SHORT).show();
            Log.d("BLE", "BLE is supported by this device.");
        }
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
