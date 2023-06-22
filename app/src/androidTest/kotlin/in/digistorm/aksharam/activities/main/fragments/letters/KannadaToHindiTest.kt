/*
 * Copyright (c) 2023 Alan M Varghese <alan@digistorm.in>
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
package `in`.digistorm.aksharam.activities.main.fragments.letters

import org.junit.Test
import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import `in`.digistorm.aksharam.R
import org.junit.Before

class KannadaToHindiTest: LettersTabTest() {
    override val logTag: String = javaClass.simpleName
    private val categories = listOf("Vowels", "Consonants", "Signs", "Ligatures")

    override fun initialise() {
        super.initialise()
        Log.d(logTag, "Selecting Kannada as the source language.")
        chooseLanguage("Kannada")

        chooseTransliterationLanguage("Hindi")
    }

    // Exposed so sibling classes can run these tests easily.
    private fun singleClickTest() {
        initialise()

        // Vowels
        scrollToCardAtPosition(position = 0)
        checkLetter("ಅ", "अ")
        checkLetter("ಅಂ", "अं")
        checkLetter("ಅಃ", "अः")

        // Consonants
        scrollToCardAtPosition(position = 1)
        checkLetter("ಕ", "क")
        checkLetter("ಳ", "ळ")

        // Signs
        scrollToCardAtPosition(position = 2)
        checkLetter("್", "्")
        checkLetter("ೃ", "ृ")

        // Ligatures
        scrollToCardAtPosition(position = 3)
        checkLetter("ಕ್ತ", "क्त")
        checkLetter("ರ್", "र्")
    }

    @Test
    fun singleClickTransliterations() {
        runMainActivityTest {
            singleClickTest()
        }
    }

    @Test
    fun longClickInfo() {
        runMainActivityTest {
            initialise()

            // Check vowels
            scrollToCardAtPosition(0)
            longClickLetter("ಐ")
            onView(withId(R.id.heading)).check(matches(withText("ಐ")))
            onView(withId(R.id.transliterated_heading)).check(matches(withText("ऐ")))
            checkLetterInfoHeadingAlignment()
            checkWordAndMeaningDisplayed("ಐರಾವತ", "ऐरावत", "ऐरावत")
            onView(withId(R.id.letter_info_container)).perform(pressBack())

            // Check vowel signs
            scrollToCardAtPosition(2)
            longClickLetter("ೇ")
            checkLetterInfoHeading("ೇ", "े")
            checkLetterInfoHeadingAlignment()
            checkWordAndMeaningHeadingHidden()
            checkInfoDisplayed("ಏ")
            checkDiacriticHint("ೇ with consonants and ligatures")
            checkCombineSignWithConsonants("ೇ", listOf("ರ", "ಮ", "ಕ್ತ", "ನ್ಯ", "ಜ"))
            onView(withId(R.id.letter_info_container)).perform(pressBack())

            // Check consonants
            scrollToCardAtPosition(1)
            longClickLetter("ನ")
            checkLetterInfoHeading("ನ", "न")
            checkLetterInfoHeadingAlignment()
            checkWordAndMeaningDisplayed("ನಾಲ್ಕು", "नाल्कु", "चार")
            checkInfoHidden()
            checkCombineConsonantWithVowelSigns("ನ", listOf("ಿ", "ೀ", "ೋ"))
            checkConsonantAsPrefix("ನ", "್", listOf("ಕ", "ಮ", "ದ", "ನ"))
            checkConsonantAsSuffix("ನ", "್", listOf("ನ", "ಮ", "ಗ", "ಣ"))
            onView(withId(R.id.letter_info_container)).perform(pressBack())

            // Check ligatures
            scrollToCardAtPosition(3)
            longClickLetter("ರ್")
            checkLetterInfoHeading("ರ್", "र्")
            checkLetterInfoHeadingAlignment()
            checkWordAndMeaningDisplayed("ಪೂರ್ವ", "पूर्व", "पूर्व")
            checkInfoDisplayed("Examples:")
            checkDiacriticHintsHidden()
            checkLigaturesWithLetterAsPrefixHidden()
            checkLigaturesWithLetterAsSuffixHidden()
        }
    }

    @Test
    fun singleClickTestAfterCollapsionExpansion() {
        runMainActivityTest {
            initialise()

            clickCardCategory(0, categories[0]) // Collapse Vowels
            clickCardCategory(2, categories[2]) // Collapse Signs
            clickCardCategory(2, categories[2]) // Expand Signs
            clickCardCategory(0, categories[0]) // Expand Vowels

            checkLetter("ಇ", "इ")
            scrollToCardAtPosition(1)
            checkLetter("ನ", "न")
            scrollToCardAtPosition(2)
            checkLetter("ೀ", "ी")
            // Collapse Everything
            clickCardCategory(0, categories[0])
            clickCardCategory(1, categories[1])
            clickCardCategory(2, categories[2])
            clickCardCategory(3, categories[3])

            clickCardCategory(1, categories[1]) // Expand consonants
            checkLetter("ಙ", "ङ")
            checkLetter("ಲ", "ल")

            clickCardCategory(1, categories[1]) // Collapse consonants
            clickCardCategory(3, categories[3]) // Expand ligatures
            checkLetter("ನ್ಯ", "न्य")
            checkLetter("ತ್ಸ", "त्स")

            chooseTransliterationLanguage("Malayalam")
            // Expand everything
            clickCardCategory(0, categories[0])
            clickCardCategory(1, categories[1])
            clickCardCategory(2, categories[2])
            chooseTransliterationLanguage("Hindi")
            singleClickTest()
        }
    }
}