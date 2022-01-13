package com.cheatdatabase.events

import com.cheatdatabase.data.model.Cheat

data class CheatRatingFinishedEvent(var isSucceeded: Boolean) {

    lateinit var throwable: Throwable
    lateinit var cheat: Cheat
    var rating: Int = 0

    constructor(cheat: Cheat, rating: Int, isSucceeded: Boolean) : this(isSucceeded) {
        this.isSucceeded = isSucceeded
        this.cheat = cheat
        this.rating = rating
    }

    constructor(throwable: Throwable, isSucceeded: Boolean) : this(isSucceeded) {
        this.isSucceeded = isSucceeded
        this.throwable = throwable
    }
}