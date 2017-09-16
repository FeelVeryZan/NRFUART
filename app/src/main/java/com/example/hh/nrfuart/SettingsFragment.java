package com.example.hh.nrfuart;

import android.os.Bundle;
import android.preference.PreferenceFragment;

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
