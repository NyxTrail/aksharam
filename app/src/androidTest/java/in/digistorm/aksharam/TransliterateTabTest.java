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
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import androidx.test.platform.app.InstrumentationRegistry;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import in.digistorm.aksharam.util.Log;

public class TransliterateTabTest extends AksharamTestBase {
    public void startTest() {
        Log.d(getLogTag(), "Test started for Practice Tab");

        try {
            Map<String, List<Map<String, String>>> testData = TransliterateTabTestData.getData(
                    InstrumentationRegistry.getInstrumentation().getContext());   // This is the test application's context
            for(String language: testData.keySet()) {
                for(Map<String, String> testCase : Objects.requireNonNull(testData.get(language))) {
                    String src = testCase.get("src");
                    if(src == null)
                        continue;
                    for(String targetLanguage: testCase.keySet()) {
                        if(targetLanguage.toLowerCase(Locale.ROOT).equals("src"))
                            continue;
                        Log.d(getLogTag(), "Testing " + language + " to " + targetLanguage + " transliteration.");
                        String target = testCase.get(targetLanguage);
                        onView(withId(R.id.TransliterateTabInputTextField)).perform(replaceText(src));
                        onView(withId(R.id.LanguageSelectionSpinner)).perform(click());
                        onData(allOf(is(instanceOf(String.class)), is(upperCaseFirstLetter(targetLanguage)))).perform(click());
                        onView(withId(R.id.TransliterateTabOutputTextView)).check(matches(withText(target)));
                    }
                }
            }
        } catch (IOException e) {
            Log.d(getLogTag(), "Error reading test data from JSON File");
            e.printStackTrace();
        }
    }
}
