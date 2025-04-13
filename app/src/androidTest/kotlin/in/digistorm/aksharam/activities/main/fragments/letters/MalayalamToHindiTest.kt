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
package `in`.digistorm.aksharam.activities.main.fragments.letters

import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.junit.Before
import org.junit.Test
import `in`.digistorm.aksharam.R

class MalayalamToHindiTest: LettersTabTest() {
    override val logTag: String = javaClass.simpleName
    private val categories = listOf("Vowels", "Consonants", "Signs", "Chillu", "Ligatures")

    override fun initialise() {
        super.initialise()
        Log.d(logTag, "Selecting Malayalam as the source language.")
        chooseLanguage("Malayalam")

        chooseTransliterationLanguage("Hindi")
    }

    // Exposed so sibling classes can run these tests easily.
    private fun singleClickTest() {
        // Vowels
        scrollToCardAtPosition(position = 0)
        checkLetter("ഊ", "ऊ")
        checkLetter("ഋ", "ऋ")
        checkLetter("ഔ", "औ")
        checkLetter("അഃ", "अः")

        // Consonants
        scrollToCardAtPosition(position = 1)
        checkLetter("ണ", "ण")
        checkLetter("ഘ", "घ")
        checkLetter("റ", "ऱ")
        checkLetter("ള", "ळ")
        checkLetter("ഡ", "ड")

        // Signs
        scrollToCardAtPosition(position = 2)
        checkLetter("ി", "ि")
        checkLetter("ം", "ं")
        checkLetter("്", "्")
        checkLetter("ൃ", "ृ")


        // Chillu
        scrollToCardAtPosition(position = 3)
        checkLetter("ൻ", "न्")
        checkLetter("ൺ", "ण्")
        checkLetter("ൾ", "ळ्")
        checkLetter("ൽ", "ल्")
        checkLetter("ർ", "र्")

        // Ligatures
        scrollToCardAtPosition(position = 4)
        checkLetter("ക്ഷ", "क्ष")
        checkLetter("പ്പ", "प्प")
        checkLetter("ങ്ക", "ङ्क")
        checkLetter("ക്വ", "क्व")
        checkLetter("മ്മ", "म्म")
    }

    @Test
    fun singleClickTransliterations() {
        runMainActivityTest {
            initialise()

            singleClickTest()
        }
    }

    @Test
    fun longClickInfo() {
        runMainActivityTest {
            initialise()

            // Check vowels
            scrollToCardAtPosition(0)
            longClickLetter("ഇ")
            onView(withId(R.id.heading)).check(matches(withText("ഇ")))
            onView(withId(R.id.transliterated_heading)).check(matches(withText("इ")))
            checkLetterInfoHeadingAlignment()
            checkWordAndMeaningDisplayed("ഇല", "इल", "पत्ता")
            onView(withId(R.id.letter_info_container)).perform(pressBack())

            // Check vowel signs
            scrollToCardAtPosition(2)
            longClickLetter("ു")
            checkLetterInfoHeading("ു", "ु")
            checkLetterInfoHeadingAlignment()
            checkWordAndMeaningHeadingHidden()
            checkInfoDisplayed("ഉ")
            checkDiacriticHint("ു with consonants and ligatures")
            checkCombineSignWithConsonants("ു", listOf("ന", "റ്റ", "ഞ", "ന്ത", "ഗ"))
            onView(withId(R.id.letter_info_container)).perform(pressBack())

            // Check consonants
            scrollToCardAtPosition(1)
            longClickLetter("ധ")
            checkLetterInfoHeading("ധ", "ध")
            checkLetterInfoHeadingAlignment()
            checkWordAndMeaningDisplayed("ധനം", "धनं", "धन")
            checkInfoHidden()
            checkCombineConsonantWithVowelSigns("ധ", listOf("ാ", "ൈ", "ീ"))
            checkLigaturesWithLetterAsPrefixHidden()
            checkLigaturesWithLetterAsSuffixHidden()
            onView(withId(R.id.letter_info_container)).perform(pressBack())

            // Check ligatures
            scrollToCardAtPosition(4)
            longClickLetter("ത്ത")
            checkLetterInfoHeading("ത്ത", "त्त")
            checkLetterInfoHeadingAlignment()
            checkWordAndMeaningDisplayed("തത്ത", "तत्त", "तोता")
            checkInfoDisplayed("ത്ത = ത് + ത")
            checkCombineConsonantWithVowelSigns("ത്ത", listOf("ോ", "െ", "്", "ൃ"))
            checkLigaturesWithLetterAsPrefixHidden()
            checkLigaturesWithLetterAsSuffixHidden()
            onView(withId(R.id.letter_info_container)).perform(pressBack())

            // Check chillu
            scrollToCardAtPosition(3)
            longClickLetter("ൾ")
            checkLetterInfoHeading("ൾ", "ळ्")
            checkLetterInfoHeadingAlignment()
            checkWordAndMeaningDisplayed("ഇവൾ", "इवळ्", "यह स्त्री या लड़की")
            checkInfoDisplayed("ൾ is the chillu for the pure consonant ള്")
        }
    }

    @Test
    fun singleClickTestAfterCollapsionExpansion() {
        runMainActivityTest {
            initialise()

            clickCardCategory(0, categories[0]) // Collapse Vowels
            clickCardCategory(2, categories[2]) // Collapse Signs
            clickCardCategory(3, categories[3]) // Collapse Chillu
            clickCardCategory(2, categories[2]) // Expand Signs
            clickCardCategory(0, categories[0]) // Expand Vowels

            // Check a Vowel
            scrollToCardAtPosition(0)
            checkLetter("ഊ", "ऊ")
            // Check a consonant
            scrollToCardAtPosition(1)
            checkLetter("ബ", "ब")
            // Check a vowel sign
            scrollToCardAtPosition(2)
            checkLetter("േ", "े")
            // Check a chillu
            scrollToCardAtPosition(3)
            checkLetterHidden("ൺ")

            // Collapse Everything
            clickCardCategory(0, categories[0])
            clickCardCategory(1, categories[1])
            clickCardCategory(2, categories[2])
            clickCardCategory(4, categories[4])

            clickCardCategory(1, categories[1]) // Expand consonants
            checkLetter("ഖ", "ख")
            checkLetter("ഴ", "ऴ")

            clickCardCategory(1, categories[1]) // Collapse consonants
            clickCardCategory(3, categories[3]) // Expand chillu
            checkLetter("ൺ", "ण्")
            checkLetter("ൽ", "ल्")

            chooseTransliterationLanguage("Kannada")
            // Expand everything
            clickCardCategory(0, categories[0])
            clickCardCategory(1, categories[1])
            clickCardCategory(2, categories[2])
            clickCardCategory(4, categories[4])

            chooseTransliterationLanguage("Hindi")
            singleClickTest()
        }
    }
}
