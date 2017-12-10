package com.cheatdatabase;

import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.util.Log;

import com.cheatdatabase.helpers.MyPrefs_;

import org.androidannotations.annotations.AfterPreferences;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.PreferenceByKey;
import org.androidannotations.annotations.PreferenceClick;
import org.androidannotations.annotations.PreferenceScreen;
import org.androidannotations.annotations.sharedpreferences.Pref;

@PreferenceScreen(R.xml.pref_general)
@EActivity
public class SettingsActivity extends AppCompatPreferenceActivity {

    private final String TAG = getClass().getSimpleName();

    @App
    CheatDatabaseApplication application;

    @Pref
    MyPrefs_ myPrefs;

    // DISABLE ACHIEVEMENTS
    @PreferenceByKey(R.string.pref_diable_achievements_key)
    SwitchPreference disableAchievements;

    // Called after changing the switch value
    @PreferenceClick(R.string.pref_diable_achievements_key)
    void prefClick(SwitchPreference preference) {
        boolean isAchievementsDisabled = preference.isChecked();
        Log.d(TAG, "setDisableAchievements to: " + isAchievementsDisabled);
        if (isAchievementsDisabled) {
            disableAchievements.setSummary(R.string.pref_achievements_button_enabled);
        } else {
            disableAchievements.setSummary(R.string.pref_achievements_button_disabled);
        }
    }

    @AfterPreferences
    void initPrefs() {
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @OptionsItem(android.R.id.home)
    void homeSelected() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
