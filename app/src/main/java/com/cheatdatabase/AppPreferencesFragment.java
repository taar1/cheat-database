package com.cheatdatabase;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

public class AppPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey);



//        Toast.makeText(getActivity(), "enable_achievements: " + getPreferenceScreen().getSharedPreferences().getBoolean("enable_achievements", false), Toast.LENGTH_SHORT).show();
    }

}
