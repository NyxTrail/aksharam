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

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class LetterDefinition {
    public String type;
    public Map<String, Map<String, String>> examples;
    public Map<String, String> info;

    public Boolean isVirama;
    public String base;
    @JsonProperty("combine_after")
    public Boolean combineAfter;
    @JsonProperty("combine_before")
    public Boolean combineBefore;
    @JsonProperty("exclude_combi_examples")
    public Boolean excludeCombiExamples;

    public Map<String, ArrayList<String>> transliterationHints;

    @JsonAnySetter
    public void setLetterDefintions(String key, ArrayList<String> list) {
        if(transliterationHints == null)
            transliterationHints = new LinkedHashMap<>();

        transliterationHints.put(key, list);
    }

    public String getType() {
        return type;
    }

    public Map<String, Map<String, String>> getExamples() {
        return examples;
    }

    public Map<String, String> getInfo() {
        return info;
    }

    public Boolean isVirama() {
        if(isVirama == null)
            return Boolean.FALSE;
        return isVirama;
    }

    public String getBase() {
        return base;
    }

    public Boolean shouldCombineAfter() {
        if(combineAfter == null)
            return Boolean.FALSE;
        return combineAfter;
    }

    public Boolean shouldCombineBefore() {
        if(combineBefore == null)
            return Boolean.FALSE;
        return combineBefore;
    }

    public Boolean shouldExcludeCombiExamples() {
        if(excludeCombiExamples == null)
            return Boolean.FALSE;
        return excludeCombiExamples;
    }

    public Map<String, ArrayList<String>> getTransliterationHints() {
        return transliterationHints;
    }
}