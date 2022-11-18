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
import kotlin.math.log

// This class is responsible for the actual transliteration
class Transliterator {
    // The JSON mapping used to transliterate
    private val logTag = Transliterator::class.simpleName

    lateinit var languageData: Language
        private set

    fun setInputLanguage(inputLang: String, context: Context) {
        logDebug(logTag, "Initialising transliterator for: $inputLang")
        val mLanguageData: Language? = getLanguageData(inputLang, context)
        if (mLanguageData == null) {
            // TODO: How should we fail?
            logDebug(logTag, "Failure handling unimplemented.")
        } else
            languageData = mLanguageData
    }

    // Constructor for when we don't know which language to load
    // load the first one we can find
    // if we can't, start initialisation activity
    constructor(context: Context) {
        // Called the constructor without any input lang, find one.
        // Files are already downloaded, this constructor is called in MainActivity
        // to display a default language.
        val fileList = getDownloadedLanguages(context)
        if (fileList.size == 0) {
            // if no files are available, we restart the Initialisation activity
            // TODO: This is not the right place to do this
            // TODO: This is VERY VERY BAD. A Transliterator constructor that starts an activity?!
            logDebug(logTag, "Could not find any language files. Starting Initialisation activity")
            val intent = Intent(context, InitialiseAppActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } else {
            logDebug(logTag, "Found language file: " + fileList[0] + "... Initialising it.")
            setInputLanguage(fileList[0], context)
        }
    }

    constructor(inputLang: String, context: Context) {
        setInputLanguage(inputLang, context)
    }

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
            logTag, "Transliterating " + str
                    + " (" + languageData.language + ") to " + targetLanguageLC
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
        logDebug(logTag, "Constructed string: $out")
        return out.toString()
    }
}