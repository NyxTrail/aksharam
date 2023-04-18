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
package `in`.digistorm.aksharam.activities.main.language

import android.content.Context
import `in`.digistorm.aksharam.activities.main.helpers.upperCaseFirstLetter
import `in`.digistorm.aksharam.activities.main.util.logDebug
import java.util.HashMap
import java.util.LinkedHashMap

class LanguageDetector(context: Context?) {
    private val logTag = javaClass.simpleName
    private val languages: LinkedHashMap<String, Language>

    // Assumption: characters in a language are unique and each character exists only once
    // in all language definitions combined. That is we detect a language by its script. If two
    // languages use the same script, we cannot distinguish between them. Such a scenario is not
    // supported in this app, since we are primarily an app for script learning (not language learning).
    fun detectLanguage(input: String): String? {
        logDebug(logTag, "Detecting language for $input")
        logDebug(logTag, "languages loaded: " + languages.keys)

        // Now, we attempt to detect the input language
        val score = HashMap<String, Int>()
        outer@for (ch in input.toCharArray()) {
            for ((languageName, value) in languages) {
                val letterDefinition = value.getLetterDefinition(ch.toString())
                // If a definition exists for letter `ch` in language...
                if (letterDefinition != null) {
                    // If we are already tracking `languageName`, increment it. Else, initialise it to 1.
                    score[languageName] = score[languageName]?.inc() ?: 1
                    // We have found the letter, no point checking other languages.
                    // Proceed with next character.
                    continue@outer
                }
            }
            // We checked all languages and did not find this character.
            logDebug(logTag, "Character $ch in $input is unknown to us. Ignoring.")
        }
        var langDetected: String? = score.keys.firstOrNull()
        var maxScore = score[langDetected] ?: 0
        for ((key, value) in score) {
            if (value > maxScore) {
                langDetected = key
                maxScore = value
            }
        }
        logDebug(logTag, "Detected language $langDetected in string $input.")
        return langDetected?.upperCaseFirstLetter()
    }

    init {
        languages = getAllLanguages(context!!)
        if (languages.isEmpty()) {
            logDebug(logTag, "Could not find any data files.")
        }
    }
}
