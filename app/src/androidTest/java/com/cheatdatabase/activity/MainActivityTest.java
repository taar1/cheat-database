package com.cheatdatabase.activity;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.cheatdatabase.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    private static final String TAG = "MainActivityTest";

    /**
     * the {@link RecyclerView}'s resource id
     */
    private int resId = R.id.my_recycler_view;

    /**
     * the {@link RecyclerView}
     */
    private RecyclerView mRecyclerView;

    /**
     * and it's item count
     */
    private int itemCount = 0;

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);


    @Before
    public void setUpTest() {
        Log.d(TAG, "testMe 1");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testMe() {
        Log.d(TAG, "testMe 1");
        activityScenarioRule.getScenario().onActivity(activity -> {
            // Do something with MainActivity...

            this.mRecyclerView = activity.findViewById(this.resId);
            this.itemCount = this.mRecyclerView.getAdapter().getItemCount();

            Log.d(TAG, "testMe itemCount: " + itemCount);
        });

//        onView(withId(R.id.add_new_cheat_button)).perform(click());

        assertEquals("hhhhh", "hhhhh");
    }

//    @Test(expected = PerformException.class)
//    public void itemWithText_doesNotExist() {
//        // Attempt to scroll to an item that contains the special text.
////        onView(ViewMatchers.withId(R.id.my_recycler_view))
////                // scrollTo will fail the test if no item matches.
////                .perform(RecyclerViewActions.scrollToHolder(isInTheMiddle()));
////
//////        // Check that the item has the special text.
//////        String middleElementText =
//////                activityRule.getActivity().getResources()
//////                        .getString(R.string.middle);
//////        onView(withText(middleElementText)).check(matches(isDisplayed()));
////
////        onView(withId(R.id.recycler_view))
////                .check(matches(atPosition(0, withText("Test Text"))));
//
//    }
}