/*
 * Copyright (c) 2022 Alan M Varghese <alan@digistorm.in>
 *
 * This files is part of Aksharam, a script teaching app for Indic
 * languages.
 *
 * Aksharam is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aksharam is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even teh implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package in.digistorm.aksharam;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.allOf;

import java.util.LinkedHashMap;

import in.digistorm.aksharam.activities.initialise.InitialiseAppActivity;
import in.digistorm.aksharam.activities.main.MainActivity;
import in.digistorm.aksharam.util.LangDataReader;
import in.digistorm.aksharam.util.Language;
import in.digistorm.aksharam.util.Log;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest extends AksharamTestBase {
    @Rule
    public ActivityScenarioRule<InitialiseAppActivity> initActivityRule =
            new ActivityScenarioRule<>(InitialiseAppActivity.class);
    @Rule
    public ActivityScenarioRule<MainActivity> mainActivityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private final LinkedHashMap<String, Language> languages;

    public MainActivityTest() {
        languages = LangDataReader.getAllLanguages(ApplicationProvider.getApplicationContext());
    }

    private void openTab(int resourceId) {
        // Tab headings are assigned automatically by the system and does not have an ID
        // Use this to narrow the match, since these words may be used elsewhere in the system
        onView(allOf(withText(resourceId), withId(-1))).perform(click());
    }

    private void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* This test is flaky. Swipe actions are not supported for ViewPager2.
       I couldn't get the adapter for the pager work with onData either.
       This test must be started on the Letters Tab.
     */
    private void swipeTest() {
        // Swipe tests
        onView(withId(R.id.lettersTabCL)).perform(swipeLeft());
        sleep();
        onView(withId(R.id.transliterateTabCL)).perform(swipeLeft());
        sleep();
        // This doesn't do anything
        onView(withId(R.id.practiceTabNSV)).perform(swipeLeft());


        // Come back to letters tab
        sleep();
        onView(withId(R.id.practiceTabNSV)).perform(swipeRight());
        sleep();
        onView(withId(R.id.transliterateTabCL)).perform(swipeRight());
    }

    @Test
    public void mainActivityTest() {
        Log.d(getLogTag(), "Main activity test");

        // This is flaky and slow
        // swipeTest();

        // Click on the tab headings
        openTab(R.string.letters_tab_header);
        openTab(R.string.practice_tab_header);
        openTab(R.string.transliterate_tab_header);

        openTab(R.string.letters_tab_header);
        new LettersTabTest().startTest();

        openTab(R.string.transliterate_tab_header);
        new TransliterateTabTest().startTest();

        openTab(R.string.practice_tab_header);
        new PracticeTabTest(languages).startTest();
    }
}