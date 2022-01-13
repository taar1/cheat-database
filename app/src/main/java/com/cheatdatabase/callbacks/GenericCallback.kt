package com.cheatdatabase.callbacks

interface GenericCallback {
    fun success()
    fun fail(e: Exception)
}