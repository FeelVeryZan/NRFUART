package com.example.Zan.nrfuart;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by Angel on 2017/4/28.
 */

public class SplashActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent WEintent = new Intent(this, WelcomeActivity.class);
        startActivity(WEintent);
        finish();
    }
}
