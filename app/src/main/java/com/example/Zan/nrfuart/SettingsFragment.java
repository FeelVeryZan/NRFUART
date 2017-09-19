package com.example.Zan.nrfuart;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.example.Zan.nrfuart.R;

/**
 * Created by 恒 on 2017/2/17.
 */
public class SettingsFragment extends PreferenceFragment{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

    }
}
