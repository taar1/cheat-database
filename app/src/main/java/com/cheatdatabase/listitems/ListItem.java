package com.cheatdatabase.listitems;

public abstract class ListItem {
    public static final int TYPE_SYSTEM = 0;
    public static final int TYPE_GAME = 1;
    public static final int TYPE_CHEAT = 2;
    public static final int TYPE_FACEBOOK_NATIVE_AD = 3;
    public static final int TYPE_INMOBI_NATIVE_AD = 4;

    abstract public int getType();
}
