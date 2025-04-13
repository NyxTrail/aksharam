/*
 * Copyright (c) 2023-2025 Alan M Varghese <alan@digistorm.in>
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
package `in`.digistorm.aksharam.activities.main.language

import `in`.digistorm.aksharam.activities.main.util.logDebug
import java.lang.StringBuilder

private const val logTag: String = "Transliterator"

// Transliterate the input string using the mapping and return the transliterated string
// str is the string that needs to be converted
// targetLanguage is the language to which the string needs to be converted
fun transliterate(str: String, targetLanguage: String, languageData: Language): String {
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
                    logDebug(
                        logTag, "Could not find transliteration hints for character: \""
                            + character + "\" of language: " + languageData.language
                            + " for transliteration to language: " + targetLanguageLC)
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
