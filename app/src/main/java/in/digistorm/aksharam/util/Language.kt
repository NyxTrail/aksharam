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

class Language {
    @JsonIgnore
    private val logTag = javaClass.simpleName

    val comment: String? = null
    val code: String? = null

    @JsonIgnore
    var language: String = ""
        set(lang) {
            field = lang.lowercase()
        }

    @JsonProperty("trans_langs")
    private val transLanguages: ArrayList<HashMap<String, String>>? = null
        get() {
            return if (field != null) {
                ArrayList(field)
            } else
                ArrayList()
        }

    val info: HashMap<String, Map<String, String>>? = null
        get() {
            return if (field != null) {
                HashMap(field)
            } else
                HashMap()
        }

    val virama: String? = null

    @JsonProperty("ligatures_auto_generatable")
    private val ligaturesAutoGeneratable: Boolean? = null

    @JsonProperty("data")
    val letterDefinitions: LinkedHashMap<String, LetterDefinition> = LinkedHashMap()
        get() {
            return if (field.isNotEmpty()) {
                LinkedHashMap(field) // return a new LinkedHashMap
            } else
                LinkedHashMap()
        }

    // {"vowels": ["a", "e", "i"...], "consonants": ["b", "c", "d"...]...}
    @JsonIgnore
    var lettersCategoryWise: LinkedHashMap<String, ArrayList<String>?> = LinkedHashMap()
        get() {
            if (field.isEmpty()) {
                logDebug(logTag, "Finding letters category wise")
                field = LinkedHashMap()
                for ((key, value) in letterDefinitions) {
                    if (field[value.getType()] != null) {
                        field[value.getType()]!!.add(key)
                    } else {
                        val letterList = ArrayList<String>()
                        letterList.add(key)
                        field[value.getType()] = letterList
                    }
                }
            }
            return LinkedHashMap(field)
        }
        private set

    // Uppercase the first letter
    val supportedLanguagesForTransliteration: ArrayList<String>
        get() {
            val languagesForTransliteration = ArrayList<String>()
            for (language in transLanguages!!) {
                var lang = language.keys.toTypedArray()[0].lowercase()
                // Uppercase the first letter
                lang = lang.substring(0, 1).uppercase() + lang.substring(1)
                languagesForTransliteration.add(lang)
            }
            return languagesForTransliteration
        }

    fun areLigaturesAutoGeneratable(): Boolean {
        return ligaturesAutoGeneratable!!
    }

    fun getLetterDefinition(letter: String?): LetterDefinition? {
        if (letter == null)
            return null
        return letterDefinitions[letter]
    }

    @JsonIgnore
    fun getLettersOfCategory(category: String): ArrayList<String> {
        logDebug(logTag, lettersCategoryWise.toString())
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
        for (item in transLanguages!!) {
            if (item.containsKey(lowercaseLanguage))
                return item[lowercaseLanguage]
        }

        return null
    }

    @get:JsonIgnore
    val vowels: ArrayList<String>
        get() = getLettersOfCategory("vowels")

    @get:JsonIgnore
    val diacritics: ArrayList<String>
        get() = getLettersOfCategory("signs")

    @get:JsonIgnore
    val consonants: ArrayList<String>
        get() = getLettersOfCategory("consonants")

    @get:JsonIgnore
    val ligatures: ArrayList<String>
        get() = getLettersOfCategory("ligatures")

    @get:JsonIgnore
    val chillu: ArrayList<String>
        get() = getLettersOfCategory("chillu")
}