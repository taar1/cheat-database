package com.cheatdatabase;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.cheatdatabase.helpers.Konstanten;

import butterknife.ButterKnife;

public class AppPreferencesActivity extends AppCompatActivity {

    private final static String TAG = AppPreferencesActivity.class.getSimpleName();
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPreferences = getSharedPreferences(Konstanten.PREFERENCES_FILE, Context.MODE_PRIVATE);
//        editor = settings.edit();

//        if (sharedPreferences.getBoolean(getString(R.string.pref_enable_achievements_key), true)) {
//            enableAchievements.setSummary(R.string.enabled);
//            enableAchievements.setChecked(true);
//        } else {
//            enableAchievements.setSummary(R.string.disabled);
//            enableAchievements.setChecked(false);
//        }

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new AppPreferencesFragment()).commit();
    }

}
