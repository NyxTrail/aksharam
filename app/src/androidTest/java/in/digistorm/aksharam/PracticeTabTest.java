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
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;

import androidx.test.core.app.ApplicationProvider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import in.digistorm.aksharam.util.Language;
import in.digistorm.aksharam.util.Log;
import in.digistorm.aksharam.util.Transliterator;

public class PracticeTabTest extends AksharamTestBase {
    private final LinkedHashMap<String, Language> languages;

    public PracticeTabTest(LinkedHashMap<String, Language> langs) {
        languages = langs;
    }

    public void startTest() {
        Log.d(getLogTag(), "Test started for practice tab");

        assertNotNull(languages);

        for(Map.Entry<String, Language> entry: languages.entrySet()) {
            Language language = entry.getValue();
            String languageName = upperCaseFirstLetter(entry.getKey());
            ArrayList<String> practiceIn = language.getSupportedLanguagesForTransliteration();

            Log.d(getLogTag(), "Clicking the Language spinner");
            onView(withId(R.id.PracticeTabLangSpinner)).perform(click());
            Log.d(getLogTag(), "Trying to find " + languageName + " in Language spinner");
            onData(allOf(is(instanceOf(String.class)), is(languageName))).perform(click());

            LinkedHashMap<String, ArrayList<String>> lettersCategoryWise = language.getLettersCategoryWise();
            ArrayList<String> practiceTypes = new ArrayList<>(lettersCategoryWise.keySet());
            if(language.areLigaturesAutoGeneratable())
                practiceTypes.add("Random Ligatures");
            practiceTypes.add("Random Words");

            Transliterator transliterator = new Transliterator(languageName.toLowerCase(Locale.ROOT),
                    ApplicationProvider.getApplicationContext());

            for(String practiceInLanguage: practiceIn) {
                Log.d(getLogTag(), "Clicking the Practice In spinner");
                onView(withId(R.id.PracticeTabPracticeInSpinner)).perform(click());
                Log.d(getLogTag(), "Trying to find " + practiceInLanguage + " in Practice In Spinner");
                onData(allOf(is(instanceOf(String.class)), is(practiceInLanguage))).perform(click());

                for(String practiceType: practiceTypes) {
                    onView(withId(R.id.PracticeTabPracticeTypeSpinner)).perform(click());
                    practiceType = upperCaseFirstLetter(practiceType);
                    Log.d(getLogTag(), "Trying to find practice type: " + practiceType);
                    onData(allOf(is(instanceOf(String.class)), is(practiceType))).perform(click());

                    String practiceText = getText(withId(R.id.PracticeTabPracticeTextTV));
                    Log.d(getLogTag(), "Obtained practice text: " + practiceText);

                    String transliteratedString = transliterator.transliterate(practiceText, practiceInLanguage);
                    Log.d(getLogTag(), "Expected transliteration: " + transliteratedString);
                    onView(withId(R.id.PracticeTabInputTIET)).perform(replaceText(transliteratedString));
                }
            }
        }
    }
}
