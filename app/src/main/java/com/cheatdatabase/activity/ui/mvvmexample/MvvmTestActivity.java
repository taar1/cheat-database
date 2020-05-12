package com.cheatdatabase.activity.ui.mvvmexample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.cheatdatabase.R;
import com.cheatdatabase.activity.ui.mvvmexample.ui.mvvmtest.MvvmTestFragment;

public class MvvmTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mvvm_test_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MvvmTestFragment.newInstance())
                    .commitNow();
        }
    }
}
