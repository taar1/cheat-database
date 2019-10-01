package com.cheatdatabase.fragments;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import com.cheatdatabase.R;

public class AppPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey);



//        Toast.makeText(getActivity(), "enable_achievements: " + getPreferenceScreen().getSharedPreferences().getBoolean("enable_achievements", false), Toast.LENGTH_SHORT).show();
    }

}
