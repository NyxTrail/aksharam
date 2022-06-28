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

package in.digistorm.aksharam.util;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Locale;

import in.digistorm.aksharam.activities.initialise.InitialiseAppActivity;

// This class is responsible for the actual transliteration
public class Transliterator {
    // The JSON mapping used to transliterate
    private final String logTag = getClass().getSimpleName();

    // The backing langDataReader for all tabs
    private Language language;

    private void initialise(String inputLang, Context context) {
        Log.d(logTag, "Initialising transliterator for: " + inputLang);
        language = LangDataReader.getLanguageData(inputLang, context);
    }

    // Constructor for when we don't know which language to load
    // load the first one we can find
    // if we can't, start initialisation activity
    public Transliterator(Context context) {
        // Called the constructor without any input lang, find one.
        // Files are already downloaded, this constructor is called in MainActivity
        // to display a default language.
        ArrayList<String> fileList = LangDataReader.getDownloadedLanguages(context);
        if(fileList.size() == 0) {
            // if no files are available, we restart the Initialisation activity
            // TODO: This is not the right place to do this
            // TODO: This is VERY VERY BAD. A Transliterator constructor that starts an activity?!
            Log.d(logTag, "Could not find any language files. Starting Initialisation activity");
            Intent intent = new Intent(context, InitialiseAppActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        else {
            Log.d(logTag, "Found language file: " + fileList.get(0) + "... Initialising it.");
            initialise(fileList.get(0), context);
        }
    }

    public Transliterator(String inputLang, Context context) {
        initialise(inputLang, context);
    }

    // Transliterate the input string using the mapping and return the transliterated string
    // str is the string that needs to be converted
    // targetLanguage is the language to which the string needs to be converted
    public String transliterate(String str, String targetLanguage) {
        targetLanguage = targetLanguage.toLowerCase(Locale.ROOT);
        String targetLangCode = language.getLanguageCode(targetLanguage);
        Log.d(logTag, "Transliterating " + str
                + " (" + language.getLanguage() + ") to " + targetLanguage
                + "(code: " + targetLangCode + ")");

        StringBuilder out = new StringBuilder();
        String character;

        // Process the string character by character
        for (char ch: str.toCharArray()) {
            character = "" + ch; // convert to string
            if (language.getLetterDefinitons().containsKey(character))
                if (language.getLetterDefinition(character)
                        .getTransliterationHints().containsKey(targetLangCode))
                    out = out.append(language.getLetterDefinition(character)
                            .getTransliterationHints()
                            .get(targetLangCode)
                            .get(0));
                else {
                    Log.d(logTag, "Could not find transliteration hints for character: "
                            + character + "of language: " + language.getLanguage()
                            + "for transliteration to language: " + targetLanguage);
                    out = out.append(character);
                }
            else {
                Log.d(logTag, "Could not find letter definition for letter: "
                        + character + " in language: " + language.getLanguage());
                out = out.append(character);
            }
        }
        Log.d(logTag, "Constructed string: " + out);
        return out.toString();
    }

    public Language getLanguage() {
        return language;
    }
}