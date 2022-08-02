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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class Language {
    @JsonIgnore
    private final String logTag = getClass().getSimpleName();

    private String comment;
    private String code;

    @JsonIgnore
    private String language;

    @JsonProperty("trans_langs")
    private ArrayList<HashMap<String, String>> transLangs;

    private HashMap<String, Map<String, String>> info;
    private String virama;

    @JsonProperty("ligatures_auto_generatable")
    private Boolean ligaturesAutoGeneratable;

    @JsonProperty("data")
    private LinkedHashMap<String, LetterDefinition> letterDefinitons;
    // {"vowels": ["a", "e", "i"...], "consonants": ["b", "c", "d"...]...}
    @JsonIgnore
    private LinkedHashMap<String, ArrayList<String>> lettersCategoryWise;

    public String getComment() {
        return comment;
    }

    public String getCode() {
        return code;
    }

    public ArrayList<HashMap<String, String>> getTransLangs() {
        return new ArrayList<>(transLangs);
    }

    public ArrayList<String> getSupportedLanguagesForTransliteration() {
        ArrayList<String> languagesForTransliteration = new ArrayList<>();
        for(HashMap<String, String> language: transLangs) {
            String lang = language.keySet().toArray()[0]
                    .toString().toLowerCase(Locale.ROOT);
            // Uppercase the first letter
            lang = lang.substring(0, 1).toUpperCase(Locale.ROOT) + lang.substring(1);
            languagesForTransliteration.add(lang);
        }
        return languagesForTransliteration;
    }

    public HashMap<String, Map<String, String>> getInfo() {
        return new HashMap<>(info);
    }

    public String getVirama() {
        return virama;
    }

    public boolean areLigaturesAutoGeneratable() {
        return ligaturesAutoGeneratable;
    }

    public HashMap<String, LetterDefinition> getLetterDefinitons() {
        return new HashMap<>(letterDefinitons);
    }

    public LetterDefinition getLetterDefinition(String letter) {
        return letterDefinitons.get(letter);
    }

    public LinkedHashMap<String, ArrayList<String>> getLettersCategoryWise() {
        if(lettersCategoryWise == null) {
            LogKt.logDebug(logTag, "Finding letters category wise");
            lettersCategoryWise = new LinkedHashMap<>();

            for(Map.Entry<String, LetterDefinition> entry : letterDefinitons.entrySet()) {
                if(lettersCategoryWise.get(entry.getValue().getType()) != null) {
                    lettersCategoryWise.get(entry.getValue().getType()).add(entry.getKey());
                }
                else {
                    ArrayList<String> letterList = new ArrayList<>();
                    letterList.add(entry.getKey());
                    lettersCategoryWise.put(entry.getValue().getType(), letterList);
                }
            }
        }
        return new LinkedHashMap<>(lettersCategoryWise);
    }

    public String getLanguage() {
        return language;
    }

    /* This should only be called in LangDataReader and nowhere else */
    public void setLanguage(String lang) {
        language = lang.toLowerCase(Locale.ROOT);
    }

    @JsonIgnore
    public ArrayList<String> getLettersOfCategory(String category) {
        if(lettersCategoryWise == null)
            lettersCategoryWise = getLettersCategoryWise();

        in.digistorm.aksharam.util.LogKt.logDebug(logTag, lettersCategoryWise.toString());

        ArrayList<String> test = lettersCategoryWise.get(category);
        if(test == null)
            return new ArrayList<>();
        else
            return new ArrayList<>(test);
    }

    /* Finds and returns the corresponding language code for a language
       in the transLangs arrays
     */
    @JsonIgnore
    public String getLanguageCode(String language) {
        language = language.toLowerCase(Locale.ROOT);
        for(Map<String, String> item : transLangs) {
            if(item.containsKey(language))
                return item.get(language);
        }
        return null;
    }

    @JsonIgnore
    public ArrayList<String> getVowels() {
        return getLettersOfCategory("vowels");
    }

    @JsonIgnore
    public ArrayList<String> getDiacritics() {
        return getLettersOfCategory("signs");
    }

    @JsonIgnore
    public ArrayList<String> getConsonants() {
        return getLettersOfCategory("consonants");
    }

    @JsonIgnore
    public ArrayList<String> getLigatures() {
        return getLettersOfCategory("ligatures");
    }

    @JsonIgnore
    public ArrayList<String> getChillu() {
        return getLettersOfCategory("chillu");
    }
}