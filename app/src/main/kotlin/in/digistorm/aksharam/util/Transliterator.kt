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

import android.content.Context
import android.content.Intent
import java.lang.StringBuilder
import `in`.digistorm.aksharam.activities.initialise.InitialiseAppActivity
import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * TODO: Deprecate after moving completely to Transliterator2
 */
// This class is responsible for the actual transliteration
class Transliterator(
    var languageData: Language,
) {
    // The JSON mapping used to transliterate
    private val logTag: String
        get() = javaClass.simpleName

    // Returns whether language is the language we are currently using for transliteration
    fun isTransliteratingLanguage(language: String): Boolean {
        val transliteratingLanguage = languageData.language
        return if(language.isEmpty() || transliteratingLanguage.isEmpty())
            false
        else language.equals(transliteratingLanguage, ignoreCase = true)
    }

    // Get the language current data is based on
    fun getLanguage(): String {
        return languageData.language
    }

    // Transliterate the input string using the mapping and return the transliterated string
    // str is the string that needs to be converted
    // targetLanguage is the language to which the string needs to be converted
    fun transliterate(str: String, targetLanguage: String): String {
        val targetLanguageLC = targetLanguage.lowercase()
        val targetLangCode = languageData.getLanguageCode(targetLanguageLC)
        logDebug(
            logTag, "Transliterating \"" + str
                    + "\" (" + languageData.language + ") to " + targetLanguageLC
                    + "(code: " + targetLangCode + ")"
        )
        var out = StringBuilder()
        var character: String

        // Process the string character by character
        for (ch in str.toCharArray()) {
            character = "" + ch // convert to string
            out =
                if (languageData.letterDefinitions.containsKey(character))
                    if (languageData.getLetterDefinition(character)?.transliterationHints!!.containsKey(targetLangCode))
                        out.append(
                            languageData.getLetterDefinition(character)!!.transliterationHints!![targetLangCode]!![0])
                    else {
                        logDebug(logTag, "Could not find transliteration hints for character: \""
                                    + character + "\" of language: " + languageData.language
                                    + "for transliteration to language: " + targetLanguageLC)
                        out.append(character)
                } else {
                    logDebug(
                        logTag, "Could not find letter definition for letter: \""
                                + character + "\" in language: " + languageData.language
                    )
                    out.append(character)
                }
        }
        logDebug(logTag, "Constructed string: \"$out\"")
        return out.toString()
    }

    companion object {
        fun create(language: String, context: Context): Transliterator {
            return Transliterator(getLanguageData(language, context)!!)
        }

        fun create(context: Context): Transliterator {
            return Transliterator(getLanguageData(
                getDownloadedFiles(context).first(), context
            )!!)
        }
    }
}
