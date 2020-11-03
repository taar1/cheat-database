package com.cheatdatabase.data.model

import com.google.gson.annotations.SerializedName

data class MyCheatsCount(
    @SerializedName("cheat_submissions") val uncheckedCheats: Int = 0,
    @SerializedName("rejected_cheats") val rejectedCheats: Int = 0,
    @SerializedName("cheats_main") val publishedCheats: Int = 0
)