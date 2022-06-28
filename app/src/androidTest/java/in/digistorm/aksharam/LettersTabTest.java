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
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;

import android.widget.ExpandableListView;

import androidx.test.core.app.ApplicationProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import in.digistorm.aksharam.util.LangDataReader;
import in.digistorm.aksharam.util.Log;
import in.digistorm.aksharam.util.Transliterator;

public class LettersTabTest extends AksharamTestBase {
    private final Random random = new Random();

    /* Clicks the info button associated with each language */
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
                transliterator.getLanguage().getLettersCategoryWise();
        Log.d(getLogTag(), "Letters category wise: " + categories);
        if(categories == null) {
            Log.d(getLogTag(), "Obtained null categories list. Exiting test.");
            return;
        }

        ArrayList<String> transLangs = transliterator.getLanguage().getSupportedLanguagesForTransliteration();
        // track what we have clicked so far
        ArrayList<String> clickedLetters = new ArrayList<>();
        for(String transLang: transLangs) {
            // Click the target language selection spinner
            Log.d(getLogTag(), "Clicking " + transLang + " in the transliteration target spinner");
            onView(withId(R.id.lettersTabTransSpinner)).perform(click());
            onData(allOf(is(instanceOf(String.class)), is(transLang))).perform(click());

            // click Language info button
            clickLanguageInfo();

            for(String category: categories.keySet()) {
                int pos = random.nextInt(Objects.requireNonNull(categories.get(category)).size());
                // for each category, touch a couple of random letters
                for(int i = 0; i < 7; i++) {
                    // 20% of the time, we choose the previous letter
                    pos = random.nextInt(100) < 20 ? pos : random.nextInt(Objects.requireNonNull(categories.get(category)).size());
                    String letter = Objects.requireNonNull(categories.get(category)).get(pos);
                    String transliteratedLetter = transliterator.transliterate(letter, transLang);
                    if(!clickedLetters.contains(letter))
                        clickedLetters.add(letter);

                    boolean longClick = random.nextBoolean();
                    Log.d(getLogTag(), "Clicking: " + letter + " long click: " + longClick);
                    onData(allOf(
                            is(instanceOf(ArrayList.class)),
                            is(categories.get(category)))
                    ).inAdapterView(is(instanceOf(ExpandableListView.class)))
                            .onChildView(withId(R.id.LetterGrid))
                            .onChildView(anyOf(withText(letter), withText(transliteratedLetter)))
                            .perform(longClick?longClick() : click());

                    // long click opens a new fragment, return to previous fragment
                    if(longClick)
                        onView(withId(R.id.letterInfoCL)).perform(pressBack());
                }
            }
        }
    }

    public void startTest() {
        Log.d(getLogTag(), "Test started for Letters tab");
        ArrayList<String> languages = LangDataReader.getDownloadedLanguages(
                ApplicationProvider.getApplicationContext());
        // Verify each available source language
        assertNotNull(languages);

        // test each available language
        for (String language: languages) { // Click on each language
            // Click the language selection spinner
            onView(withId(R.id.lettersTabLangSpinner)).perform(click());
            Log.d(getLogTag(), "Trying to find " + language + " in lettersTabLangSpinner");
            onData(allOf(is(instanceOf(String.class)), is(language))).perform(click());

            clickLetters(language);
        }
    }

}
