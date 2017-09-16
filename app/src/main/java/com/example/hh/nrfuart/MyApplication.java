package com.example.hh.nrfuart;

import android.app.Application;
import android.content.Context;

/**
 * Created by Angel on 2017/3/28.
 */

public class MyApplication extends Application
{
    private static Context context;

    @Override
    public void onCreate()
    {
        context=getApplicationContext();
    }
    public static Context getContext()
    {
        return context;
    }
}
