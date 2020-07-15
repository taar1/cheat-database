package com.cheatdatabase.fragments;

import androidx.fragment.app.Fragment;

import com.cheatdatabase.activity.MainActivity;


public abstract class MainFragment extends Fragment {

    public abstract void forceRefresh();

    public void setMainActivity(MainActivity mainActivity) {
    }
}
