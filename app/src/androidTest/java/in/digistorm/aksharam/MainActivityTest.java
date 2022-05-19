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

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import android.widget.ExpandableListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import in.digistorm.aksharam.activities.initialise.InitialiseAppActivity;
import in.digistorm.aksharam.activities.main.MainActivity;
import in.digistorm.aksharam.util.LangDataReader;
import in.digistorm.aksharam.util.Log;
import in.digistorm.aksharam.util.Transliterator;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {
    private final String logTag = getClass().getSimpleName();

    @Rule
    public ActivityScenarioRule<InitialiseAppActivity> initActivityRule =
            new ActivityScenarioRule<>(InitialiseAppActivity.class);
    @Rule
    public ActivityScenarioRule<MainActivity> mainActivityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private final Random random = new Random();

    /*
    @Test
    public void initActivityTest() {
        Log.d(logTag, "Init Activity Test Started");
    }
    */

    public void openTab(int resourceId) {
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

    private String upperCaseFirstLetter(String s) {
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }

    /* This test is flaky. Swipe actions are not supported for ViewPager2.
       I couldn't get the adapter for the pager work with onData either.
       This test must be started on the Letters Tab.
     */
    public void swipeTest() {
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

    private void clickLanguageInfo() {
        onView(withId(R.id.lettersTabInfoButton)).perform(click());
        onView(withId(R.id.languageInfoCL)).perform(pressBack());
    }

    private void clickLetters(String language) {
        // let's try to click a letter
        // String targetLanguage = LettersTabFragment.getViewModel
        Transliterator transliterator = new Transliterator(language,
                ApplicationProvider.getApplicationContext());
        HashMap<String, ArrayList<String>> categories =
                transliterator.getLangDataReader().getCategories();
        if(categories == null) {
            Log.d(logTag, "Obtained null categories list. Exiting test.");
            return;
        }

        ArrayList<String> transLangs = transliterator.getLangDataReader().getTransLangs();
        // track what we have clicked so far
        HashMap<String, String> clickedLetters = new HashMap<>();
        for(String transLang: transLangs) {
            // Click the target language selection spinner
            Log.d(logTag, "Clicking " + transLang + " in the transliteration target spinner");
            onView(withId(R.id.lettersTabTransSpinner)).perform(click());
            onData(allOf(is(instanceOf(String.class)), is(transLang))).perform(click());

            // click Language info button
            clickLanguageInfo();

            for(String category: categories.keySet()) {

                // for each category, touch a couple of random letters
                for(int i = 0; i < 4; i++) {
                    int pos = random.nextInt(Objects.requireNonNull(categories.get(category)).size());
                    String letter = Objects.requireNonNull(categories.get(category)).get(pos);
                    String transliteratedLetter = transliterator.transliterate(letter, transLang);
                    if(!clickedLetters.containsKey(letter))
                        clickedLetters.put(letter, letter);

                    Log.d(logTag, "Clicking " + clickedLetters.get(letter));

                    boolean longClick = random.nextBoolean();
                    onData(allOf(
                            is(instanceOf(ArrayList.class)),
                            is(categories.get(category)))
                    ).inAdapterView(is(instanceOf(ExpandableListView.class)))
                            .onChildView(withId(R.id.LetterGrid))
                            .onChildView(anyOf(withText(clickedLetters.get(letter)), withText(letter)))
                            .perform(longClick?longClick() : click());

                    if(longClick)
                        onView(withId(R.id.letterInfoCL)).perform(pressBack());

                    if(Objects.equals(clickedLetters.get(letter), transliteratedLetter))
                        clickedLetters.put(letter, letter);
                    else
                        clickedLetters.put(letter, transliteratedLetter);
                }
            }
        }
    }

    private void lettersTabTest() {
        Log.d(logTag, "Test started for Letters tab");
        ArrayList<String> languages = LangDataReader.getAvailableSourceLanguages(
                ApplicationProvider.getApplicationContext());
        // Verify each available source language
        assertNotNull(languages);

        // test each available language
        for (String language: languages) { // Click on each language
            // Click the language selection spinner
            onView(withId(R.id.lettersTabLangSpinner)).perform(click());
            Log.d(logTag, "Trying to find " + language + " in lettersTabLangSpinner");
            onData(allOf(is(instanceOf(String.class)), is(language))).perform(click());

            clickLetters(language);
        }
    }

    private void transliterateTabTest() {
        Log.d(logTag, "Test started for Practice Tab");

        try {
            Map<String, List<Map<String, String>>> testData = TransliterateTabTestData.getData(
                    InstrumentationRegistry.getInstrumentation().getContext());
            for(String language: testData.keySet()) {
                for(Map<String, String> testCase : Objects.requireNonNull(testData.get(language))) {
                    String src = testCase.get("src");
                    if(src == null)
                        continue;
                    for(String targetLanguage: testCase.keySet()) {
                        if(targetLanguage.toLowerCase(Locale.ROOT).equals("src"))
                            continue;
                        android.util.Log.d(logTag, "Testing " + language + " to " + targetLanguage + " transliteration.");
                        String target = testCase.get(targetLanguage);
                        onView(withId(R.id.TransliterateTabInputTextField)).perform(replaceText(src));
                        onView(withId(R.id.LanguageSelectionSpinner)).perform(click());
                        onData(allOf(is(instanceOf(String.class)), is(upperCaseFirstLetter(targetLanguage)))).perform(click());
                        onView(withId(R.id.TransliterateTabOutputTextView)).check(matches(withText(target)));
                    }
                }
            }

        } catch (IOException e) {
            Log.d(logTag, "Error reading test data from JSON File");
            e.printStackTrace();
        }
    }

    @Test
    public void mainActivityTest() {
        Log.d(logTag, "Main activity test");

        // This is flaky and slow
        // swipeTest();

        // Click on the tab headings
        openTab(R.string.letters_tab_header);
        openTab(R.string.practice_tab_header);
        openTab(R.string.transliterate_tab_header);

        openTab(R.string.letters_tab_header);
        lettersTabTest();

        openTab(R.string.transliterate_tab_header);
        transliterateTabTest();
    }
}