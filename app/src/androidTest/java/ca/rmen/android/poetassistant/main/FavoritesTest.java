/*
 * Copyright (c) 2017 Carmen Alvarez
 *
 * This file is part of Poet Assistant.
 *
 * Poet Assistant is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Poet Assistant is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Poet Assistant.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.rmen.android.poetassistant.main;


import android.app.Activity;
import android.content.Context;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.EnumSet;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.rules.ActivityStageIdlingResource;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;
import ca.rmen.android.poetassistant.settings.SettingsActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkAllStarredWords;
import static ca.rmen.android.poetassistant.main.TestAppUtils.clearStarredWords;
import static ca.rmen.android.poetassistant.main.TestAppUtils.search;
import static ca.rmen.android.poetassistant.main.TestAppUtils.starQueryWord;
import static ca.rmen.android.poetassistant.main.TestAppUtils.unStarQueryWord;
import static ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerRight;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class FavoritesTest {

    @Rule
    public PoetAssistantActivityTestRule<MainActivity> mActivityTestRule = new PoetAssistantActivityTestRule<>(MainActivity.class, true);

    @Test
    public void favoritesTest() {
        Context context = mActivityTestRule.getActivity();
        search("cheesecake");
        starQueryWord();
        onView(allOf(withId(R.id.btn_star_result), hasSibling(withText("ache")))).perform(click());
        swipeViewPagerLeft(4);
        checkAllStarredWords(context, "cheesecake", "ache");
        swipeViewPagerRight(3);
        unStarQueryWord();
        swipeViewPagerLeft(3);
        checkAllStarredWords(context, "ache");
        onView(allOf(withId(R.id.btn_star_result), hasSibling(withText("ache")), isDisplayed())).perform(click());
        checkAllStarredWords(context);
        swipeViewPagerRight(2);
        starQueryWord();
        swipeViewPagerLeft(2);
        checkAllStarredWords(context, "cheesecake");
        clearStarredWords();
        checkAllStarredWords(context);
    }

    @Test
    public void exportTest() {
        openMenuItem(R.string.action_settings);
        clickPreference(R.string.action_export_favorites);
        checkActivityPaused(SettingsActivity.class.getName());
    }

    @Test
    public void importTest() {
        openMenuItem(R.string.action_settings);
        clickPreference(R.string.action_import_favorites);
        checkActivityPaused(SettingsActivity.class.getName());
    }

    private void checkActivityPaused(String activityClassName) {
        // Wait for the activity to pause
        ActivityStageIdlingResource waitForActivityPause = new ActivityStageIdlingResource(
                activityClassName,
                EnumSet.of(Stage.PAUSED, Stage.STOPPED));
        IdlingRegistry.getInstance().register(waitForActivityPause);
        getInstrumentation().runOnMainSync(() -> {
            Collection<Activity> pausedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.PAUSED);
            Collection<Activity> stoppedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.STOPPED);
            boolean found = false;
            for (Activity activity : pausedActivities) {
                if (activity.getClass().getName().equals(activityClassName)) {
                    found = true;
                }
            }
            for (Activity activity : stoppedActivities) {
                if (activity.getClass().getName().equals(activityClassName)) {
                    found = true;
                }
            }
            assertTrue("activities doesn't contain " + activityClassName + ": paused: " + pausedActivities + ", stopped: " + stoppedActivities, found);
            IdlingRegistry.getInstance().unregister(waitForActivityPause);
        });
    }

}

