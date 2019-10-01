package com.cheatdatabase;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cheatdatabase.fragments.AppPreferencesFragment;
import com.cheatdatabase.helpers.Konstanten;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AppPreferencesActivity extends AppCompatActivity {

    private final static String TAG = AppPreferencesActivity.class.getSimpleName();
    private SharedPreferences sharedPreferences;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.action_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

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
