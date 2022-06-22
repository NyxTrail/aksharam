package in.digistorm.aksharam.util;

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

import android.content.Context;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LanguageDetector {
    private final String logTag = getClass().getSimpleName();
    private final LinkedHashMap<String, Language> languages;

    public LanguageDetector(Context context) {
        languages = LangDataReader.getAllLanguages(context);
        if(languages.isEmpty()) {
            Log.d(logTag, "Could not find any data files.");
        }
    }

    public String detectLanguage(String input, Context context) {
        Log.d(logTag, "Detecting language for " + input);
        Log.d(logTag, "languages loaded: " + languages.keySet());

        // now, we attempt to detect the input language
        String[] langs = languages.keySet().toArray(new String[0]);
        HashMap<String, Integer> score = new HashMap<>();
        for (char ch: input.toCharArray()) {
            for (Map.Entry<String, Language> entry: languages.entrySet()) {
                LetterDefinition letterDefinition = entry.getValue().getLetterDefinition(ch + "");
                if (letterDefinition != null) {
                    Log.d(logTag, " " + ch + " matches a character in the " + entry.getKey() + " set.");
                    if (score.containsKey(entry.getKey())) {
                        score.put(entry.getKey(), score.get(entry.getKey()) + 1);
                    }
                    else
                        score.put(entry.getKey(), 1);
                }
            }
        }

        String langDetected = null;
        int max_score = 0;
        for (Map.Entry<String, Integer> entry: score.entrySet()) {
            if (langDetected == null) {
                langDetected = entry.getKey();
                max_score = entry.getValue();
                continue;
            }
            if (entry.getValue() > max_score) {
                langDetected = entry.getKey();
                max_score = entry.getValue();
            }
        }
        Log.d(logTag, "Detected " + langDetected + " in input string with score " + max_score);

        return langDetected;
    }
}
