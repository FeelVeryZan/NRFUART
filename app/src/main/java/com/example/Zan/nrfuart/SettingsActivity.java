package com.example.Zan.nrfuart;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceActivity;

import com.example.Zan.nrfuart.R;

import java.util.List;

/**
 * Created by 恒 on 2017/2/17.
 */
public class SettingsActivity extends PreferenceActivity{

    public static void activitystart(Context context)
    {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.headers_preference, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return SettingsFragment.class.getName().equals(fragmentName);
    }
}
