package com.cheatdatabase.activity.ui.mycheats;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.cheatdatabase.R;

public class MyUnpublishedCheatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mvvm_test_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MyUnpublishedCheatsFragment.newInstance())
                    .commitNow();
        }
    }
}
