package com.cheatdatabase.helpers;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import com.cheatdatabase.activity.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ToolsTest {
    private static final String TAG = "ToolsTest";

    private Tools tools;
    private Context instrumentationContext;

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() throws Exception {

        instrumentationContext = InstrumentationRegistry.getInstrumentation().getContext();


        tools = new Tools(instrumentationContext);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void isEmailValid() {
        assertTrue(tools.isEmailValid("waves@gmx.ch"));
        assertFalse(tools.isEmailValid("waves@x.c"));
    }

    @Test
    public void convertDateToLocaleDateFormat() {
        Log.i(TAG, "convertDateToLocaleDateFormat: " + tools.convertDateToLocaleDateFormat("2020-10-14 14:23:11"));
        String formattedDate = tools.convertDateToLocaleDateFormat("2020-10-14 14:23:11");

        assertEquals(formattedDate, "14.10.20");
    }

    @Test
    public void getDayDifference() {
        long differenceDate = Tools.getDayDifference("2020-08-08", "2020-08-15");
        assertEquals(differenceDate, 7);
    }

    @Test
    public void getSystemNameById() {
        activityScenarioRule.getScenario().onActivity(activity -> {
            String systemName = Tools.getSystemNameById(activity, 4);
            Log.i(TAG, "XXXXX getSystemNameById: " + systemName);

            assertEquals(Tools.getSystemNameById(activity, 4), "SNES");
            assertEquals(Tools.getSystemNameById(activity, 10), "Wonderswan");
        });
    }

}