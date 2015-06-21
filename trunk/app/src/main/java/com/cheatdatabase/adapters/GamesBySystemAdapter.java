package com.cheatdatabase.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.cheatdatabase.businessobjects.Game;


public class GamesBySystemAdapter extends ArrayAdapter<Game> {

    public GamesBySystemAdapter(Context context, int resource) {
        super(context, resource);
    }
}
