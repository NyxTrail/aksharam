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
package `in`.digistorm.aksharam.util

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

class Language(
    val comment: String = "",
    val code: String = "",

    @JsonProperty("trans_langs")
    val transLanguages: ArrayList<HashMap<String, String>>,

    val info: HashMap<String, Map<String, String>>,
    val virama: String = "",

    @JsonProperty("ligatures_auto_generatable")
    private val ligaturesAutoGeneratable: Boolean = false,

    @JsonProperty("data")
    val letterDefinitions: Map<String, LetterDefinition>,

    @JsonIgnore
    var language: String = ""
) {

    // {"vowels": ["a", "e", "i"...], "consonants": ["b", "c", "d"...]...}
    @JsonIgnore
    val lettersCategoryWise: LinkedHashMap<String, ArrayList<String>> = LinkedHashMap()
        get() {
            if (field.isEmpty()) {
                for ((key, value) in letterDefinitions) {
                    if (field[value.type] != null) {
                        field[value.type]!!.add(key)
                    } else {
                        val letterList = ArrayList<String>()
                        letterList.add(key)
                        field[value.type] = letterList
                    }
                }
            }
            return LinkedHashMap(field)
        }

    // Uppercase the first letter
    val supportedLanguagesForTransliteration: ArrayList<String>
        get() {
            val languagesForTransliteration = ArrayList<String>()
            for (language in transLanguages) {
                var lang = language.keys.toTypedArray()[0].lowercase()
                // Uppercase the first letter
                lang = lang.substring(0, 1).uppercase() + lang.substring(1)
                languagesForTransliteration.add(lang)
            }
            return languagesForTransliteration
        }

    fun areLigaturesAutoGeneratable(): Boolean {
        return ligaturesAutoGeneratable
    }

    fun getLetterDefinition(letter: String?): LetterDefinition? {
        if (letter == null)
            return null
        return letterDefinitions[letter]
    }

    @JsonIgnore
    fun getLettersOfCategory(category: String): ArrayList<String> {
        val test = lettersCategoryWise[category]
        return if (test == null) ArrayList() else ArrayList(
            test
        )
    }

    /* Finds and returns the corresponding language code for a language
       in the transLanguages arrays
     */
    @JsonIgnore
    fun getLanguageCode(language: String?): String? {
        if(language == null)
            return null

        val lowercaseLanguage = language.lowercase()
        for (item in transLanguages) {
            if (item.containsKey(lowercaseLanguage))
                return item[lowercaseLanguage]
        }

        return null
    }

    @get:JsonIgnore
    val vowels: ArrayList<String>
        get() = getLettersOfCategory("vowels")

    @get:JsonIgnore
    val consonants: ArrayList<String>
        get() = getLettersOfCategory("consonants")

    @get:JsonIgnore
    val ligatures: ArrayList<String>
        get() = getLettersOfCategory("ligatures")

    @get:JsonIgnore
    val chillu: ArrayList<String>
        get() = getLettersOfCategory("chillu")

    @get:JsonIgnore
    val signs: ArrayList<String>
        get() = getLettersOfCategory("signs")

    /**
     * Returns a list of all possible combinations where a vowel sign can occur after a consonant.
     */
    fun generateDiacriticsForSign(sign: String): List<String> {
        val diacritics = mutableListOf<String>()
        consonants.forEach { consonant ->
            if(getLetterDefinition(consonant)?.shouldExcludeCombiExamples() != true) {
                diacritics.add(consonant + sign)
            }
        }
        ligatures.forEach { ligature ->
            if(getLetterDefinition(ligature)?.shouldExcludeCombiExamples() != true) {
                diacritics.add(ligature + sign)
            }
        }
        return diacritics
    }

    /**
     * Returns a list of all possible combinations of letter with vowel signs.
     * Assumption: letter is a consonant or a ligature that can be combined with vowel signs.
     * If they cannot be combined, excludeCombiExamples must be set true in data file.
     */
    fun generateDiacriticsForLetter(letter: String): List<String> {
        val diacritics = mutableListOf<String>()
        signs.forEach { sign ->
            if(getLetterDefinition(sign)?.shouldExcludeCombiExamples() != true) {
                diacritics.add(letter + sign)
            }
        }
        return diacritics
    }

    /**
     * Generate a list of all possible ligatures formed by combining letter with consonants.
     * A consonant is not used and included in this process if excludeCombiExamples is set true in its
     * definition. The virama character is used for combining the letters.
     * The return value is a Pair of Lists, where the first value is a list of ligatures with letter
     * as a prefix, and the second value is a list of ligatures with letter as a suffix.
     */
    fun generateLigatures(letter: String): Pair<List<String>, List<String>> {
        val ligaturesWithLetterAsPrefix = mutableListOf<String>()
        val ligaturesWithLetterAsSuffix = mutableListOf<String>()
        if(ligaturesAutoGeneratable) {
            consonants.forEach { consonant ->
                if (getLetterDefinition(consonant)?.shouldExcludeCombiExamples() != true) {
                    ligaturesWithLetterAsPrefix.add(letter + virama + consonant)
                    ligaturesWithLetterAsSuffix.add(consonant + virama + letter)
                }
            }
        }
        return Pair(ligaturesWithLetterAsPrefix, ligaturesWithLetterAsSuffix)
    }
}