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
import `in`.digistorm.aksharam.activities.main.util.logDebug
import java.util.HashMap
import java.util.LinkedHashMap

class LanguageDetector(context: Context?) {
    private val logTag = javaClass.simpleName
    private val languages: LinkedHashMap<String, Language>
    fun detectLanguage(input: String, context: Context?): String? {
        logDebug(logTag, "Detecting language for $input")
        logDebug(logTag, "languages loaded: " + languages.keys)

        // now, we attempt to detect the input language
        val langs = languages.keys.toTypedArray()
        val score = HashMap<String, Int>()
        for (ch in input.toCharArray()) {
            for ((key, value) in languages) {
                val letterDefinition = value.getLetterDefinition(ch.toString() + "")
                if (letterDefinition != null) {
                    logDebug(logTag, " $ch matches a character in the $key set.")
                    if (score.containsKey(key)) {
                        score[key] = score[key]!! + 1
                    } else score[key] = 1
                }
            }
        }
        var langDetected: String? = null
        var maxScore = 0
        for ((key, value) in score) {
            if (langDetected == null) {
                langDetected = key
                maxScore = value
                continue
            }
            if (value > maxScore) {
                langDetected = key
                maxScore = value
            }
        }
        logDebug(logTag, "Detected $langDetected in input string with score $maxScore")
        return langDetected
    }

    init {
        languages = getAllLanguages(context!!)
        if (languages.isEmpty()) {
            logDebug(logTag, "Could not find any data files.")
        }
    }
}
