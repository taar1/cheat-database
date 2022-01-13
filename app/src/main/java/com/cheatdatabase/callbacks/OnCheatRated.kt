package com.cheatdatabase.callbacks

import com.cheatdatabase.events.CheatRatingFinishedEvent

interface OnCheatRated {
    fun onCheatRated(cheatRatingFinishedEvent: CheatRatingFinishedEvent)
}