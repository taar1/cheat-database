package com.cheatdatabase.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.cheatdatabase.R

class AppPreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey)
    }
}