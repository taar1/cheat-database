package com.cheatdatabase.data.helper;

import com.cheatdatabase.data.model.Cheat;

import dagger.Component;

@Component
public interface CheatComponent {
    Cheat getCheat();
}
