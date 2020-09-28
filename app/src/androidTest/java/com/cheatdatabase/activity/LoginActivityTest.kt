package com.cheatdatabase.activity

import android.content.Context
import androidx.test.core.app.ActivityScenario.ActivityAction
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginActivityTest {

    lateinit var instrumentationContext: Context

    @Rule
    var activityScenarioRule = ActivityScenarioRule(
        LoginActivity::class.java
    )

    @Before
    fun setup() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
    }

    @Test
    fun someTest() {
        activityScenarioRule.scenario.onActivity(ActivityAction { activity: LoginActivity? ->
            // Do something with activity
        })
        TODO()
    }

}