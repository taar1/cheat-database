package com.cheatdatabase.listitems

abstract class ListItem {
    abstract fun type(): Int
    abstract fun title(): String?

    companion object {
        const val TYPE_SYSTEM = 0
        const val TYPE_GAME = 1
        const val TYPE_CHEAT = 2
        const val TYPE_FACEBOOK_NATIVE_AD = 3
        const val TYPE_INMOBI_NATIVE_AD = 4
        const val TYPE_BLANK = 5
        const val TYPE_UKON_NO_CHIKARA = 6
        const val TYPE_APPLOVIN_NATIVE = 7
    }
}