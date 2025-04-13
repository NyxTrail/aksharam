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
package `in`.digistorm.aksharam.activities.main.fragments.practice

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import `in`.digistorm.aksharam.activities.main.language.Language
import `in`.digistorm.aksharam.activities.main.language.getLanguageData
import `in`.digistorm.aksharam.activities.main.language.transliterate
import `in`.digistorm.aksharam.util.containsOneOf
import org.junit.Test

class MalayalamTest: PracticeTabTest() {
    override val logTag: String = this.javaClass.simpleName
    private val language: String = "Malayalam"
    private val practiceInLanguages = listOf(
        "Hindi", "Kannada"
    )
    private val practiceTypes = listOf(
        "Vowels", "Consonants", "Signs", "Ligatures", "Random Words"
    )

    private lateinit var malayalamData: Language

    private fun initialiseData() {
        malayalamData = getLanguageData(
            "malayalam.json",
            ApplicationProvider.getApplicationContext()
        ) ?: throw(Exception("Could not load language data for Malayalam"))
    }

    override fun initialise() {
        initialiseData()
        super.initialise()
    }

    private fun practiceVowels(practiceInLanguage: String) {
        choosePracticeType("Vowels")

        val practiceText = getPracticeText()
            ?: throw(Exception("Could not get practice text from UI"))

        val transliteratedString = transliterate(practiceText, practiceInLanguage, malayalamData)

        Log.d(logTag, "Practice String: $practiceText")
        Log.d(logTag, "Transliterated String: $transliteratedString")
        inputText(transliteratedString)
        checkTransliterationSuccess()
        Log.d(logTag, "Practice vowels in language: $practiceInLanguage success.")
    }

    private fun practiceConsonants(practiceInLanguage: String) {
        choosePracticeType("Consonants")

        val practiceText = getPracticeText()
            ?: throw(Exception("Could not get practice text from UI"))

        val transliteratedString = transliterate(practiceText, practiceInLanguage, malayalamData)

        Log.d(logTag, "Practice String: $practiceText")
        Log.d(logTag, "Transliterated String: $transliteratedString")
        inputText(transliteratedString)
        checkTransliterationSuccess()
        Log.d(logTag, "Practice consonants in language: $practiceInLanguage success.")
    }

    private fun practiceSigns(practiceInLanguage: String) {
        choosePracticeType("Signs")

        val practiceText = getPracticeText()
            ?: throw(Exception("Could not get practice text from UI"))

        val transliteratedString = transliterate(practiceText, practiceInLanguage, malayalamData)

        Log.d(logTag, "Practice String: $practiceText")
        Log.d(logTag, "Transliterated String: $transliteratedString")
        inputText(transliteratedString)
        checkTransliterationSuccess()
        Log.d(logTag, "Practice signs in language: $practiceInLanguage success.")
    }

    private fun practiceChillu(practiceInLanguage: String) {
        choosePracticeType("Signs")

        val practiceText = getPracticeText()
            ?: throw(Exception("Could not get practice text from UI"))

        val transliteratedString = transliterate(practiceText, practiceInLanguage, malayalamData)

        Log.d(logTag, "Practice String: $practiceText")
        Log.d(logTag, "Transliterated String: $transliteratedString")
        inputText(transliteratedString)
        checkTransliterationSuccess()
        Log.d(logTag, "Practice chillu in language: $practiceInLanguage success.")
    }

    private fun practiceLigatures(practiceInLanguage: String) {
        choosePracticeType("Signs")

        val practiceText = getPracticeText()
            ?: throw(Exception("Could not get practice text from UI"))

        val transliteratedString = transliterate(practiceText, practiceInLanguage, malayalamData)

        Log.d(logTag, "Practice String: $practiceText")
        Log.d(logTag, "Transliterated String: $transliteratedString")
        inputText(transliteratedString)
        checkTransliterationSuccess()
        Log.d(logTag, "Practice ligatures in language: $practiceInLanguage success.")
    }

    private fun practiceRandomWords(practiceInLanguage: String, withChillu: Boolean = false) {
        choosePracticeType(practiceTypes.last())

        var counter = 1
        var practiceText
                : String
        do {
            clickRefreshButton()
            practiceText = getPracticeText()
                ?: throw(Exception("Could not get practice text from UI"))
            counter++
        } while (practiceText.containsOneOf(malayalamData.chillu) && !withChillu)
        if(withChillu)
            Log.d(logTag, "Found string with chillu: $practiceText after $counter tries.")
        else
            Log.d(logTag, "Found string without chillu: $practiceText after $counter tries.")

        val transliteratedString =
            transliterate(practiceText, practiceInLanguage, malayalamData)

        inputText(transliteratedString)
        checkTransliterationSuccess()

        if(withChillu)
            Log.d(logTag, "Practice random words with chillu in language: $practiceInLanguage success.")
        else
            Log.d(logTag, "Practice random words without chillu in language: $practiceInLanguage success.")
    }

    @Test
    fun practiceInKannada() {
        runMainActivityTest {
            initialise()
            val practiceIn = "Kannada"

            chooseLanguage(language)
            choosePracticeInLanguage(practiceIn)

            for (practiceType in practiceTypes) {
                when (practiceType) {
                    "Vowels" -> practiceVowels(practiceIn)
                    "Consonants" -> practiceConsonants(practiceIn)
                    "Signs" -> practiceSigns(practiceIn)
                    "Chillu" -> practiceChillu(practiceIn)
                    "Ligatures" -> practiceLigatures(practiceIn)
                    "Random Words" -> {
                        practiceRandomWords(practiceIn, true)
                        practiceRandomWords(practiceIn, false)
                    }
                }
            }
        }
    }

    @Test
    fun practiceInHindi() {
        runMainActivityTest {
            initialise()
            val practiceIn = "Hindi"

            chooseLanguage(language)
            choosePracticeInLanguage(practiceIn)

            for (practiceType in practiceTypes) {
                when (practiceType) {
                    "Vowels" -> practiceVowels(practiceIn)
                    "Consonants" -> practiceConsonants(practiceIn)
                    "Signs" -> practiceSigns(practiceIn)
                    "Chillu" -> practiceChillu(practiceIn)
                    "Ligatures" -> practiceLigatures(practiceIn)
                    "Random Words" -> {
                        practiceRandomWords(practiceIn, true)
                        practiceRandomWords(practiceIn, false)
                    }
                }
            }
        }
    }
}
