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

class HindiToMalayalamTest: LettersTabTest() {
    override val logTag: String = javaClass.simpleName
    private val categories = listOf("Vowels", "Consonants", "Signs", "Ligatures")


    override fun initialise() {
        super.initialise()
        Log.d(logTag, "Selecting Hindi as the source language.")
        chooseLanguage("Hindi")

        chooseTransliterationLanguage("Malayalam")
    }


    // Exposed so sibling classes can run these tests easily.
    private fun singleClickTest() {
        // Vowels
        scrollToCardAtPosition(position = 0)
        checkLetter("ऊ", "ഊ")
        checkLetter("ऋ", "ഋ")
        checkLetter("औ", "ഔ")
        checkLetter("अः", "അഃ")

        // Consonants
        scrollToCardAtPosition(position = 1)
        checkLetter("ण", "ണ")
        checkLetter("घ", "ഘ")
        checkLetter("र", "ര")
        checkLetter("ल", "ല")
        checkLetter("ड", "ഡ")

        // Signs
        scrollToCardAtPosition(position = 2)
        checkLetter("ि", "ി")
        checkLetter("ं", "ം")
        checkLetter("्", "്")
        checkLetter("ृ", "ൃ")


        // Ligatures
        scrollToCardAtPosition(position = 3)
        checkLetter("क्ष", "ക്ഷ")
        checkLetter("क़", "ക")
        checkLetter("ड़", "ഡ")
        checkLetter("ज्ञ", "ജ്ഞ")
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
            longClickLetter("इ")
            onView(withId(R.id.heading)).check(matches(withText("इ")))
            onView(withId(R.id.transliterated_heading)).check(matches(withText("ഇ")))
            checkLetterInfoHeadingAlignment()
            checkWordAndMeaningDisplayed("इधर", "ഇധര", "ഇവിടെ")
            onView(withId(R.id.letter_info_container)).perform(pressBack())

            // Check vowel signs
            scrollToCardAtPosition(2)
            longClickLetter("ु")
            checkLetterInfoHeading("ु", "ു")
            checkLetterInfoHeadingAlignment()
            checkWordAndMeaningHeadingHidden()
            checkInfoDisplayed("उ")
            checkDiacriticHint("ु with consonants and ligatures")
            checkCombineSignWithConsonants("ु", listOf("न", "र", "ञ", "त्र", "ब"))
            onView(withId(R.id.letter_info_container)).perform(pressBack())

            // Check consonants
            scrollToCardAtPosition(1)
            longClickLetter("ध")
            checkLetterInfoHeading("ध", "ധ")
            checkLetterInfoHeadingAlignment()
            checkWordAndMeaningDisplayed("धरती", "ധരതീ", "ഭൂമി")
            checkInfoHidden()
            checkCombineConsonantWithVowelSigns("ध", listOf("ा", "ै", "ी"))
            checkConsonantAsSuffix("ध", "्", listOf("ध", "द", "ण", "न"))
            checkConsonantAsPrefix("ध", "्", listOf("ध", "द", "ण", "न"))
            onView(withId(R.id.letter_info_container)).perform(pressBack())

            // Check ligatures
            scrollToCardAtPosition(3)
            longClickLetter("श्र")
            checkLetterInfoHeading("श्र", "ശ്ര")
            checkLetterInfoHeadingAlignment()
            checkWordAndMeaningDisplayed("विश्राम", "വിശ്രാമ", "വിശ്രമം")
            checkInfoDisplayed("श्र = श् + र")
            checkCombineConsonantWithVowelSigns("श्र", listOf("ो", "े", "्", "ृ"))
            checkLigaturesWithLetterAsPrefixHidden()
            checkLigaturesWithLetterAsSuffixHidden()
            onView(withId(R.id.letter_info_container)).perform(pressBack())
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

            // Check a Vowel
            scrollToCardAtPosition(0)
            checkLetter("ऊ", "ഊ")
            // Check a consonant
            scrollToCardAtPosition(1)
            checkLetter("ब", "ബ")
            // Check a vowel sign
            scrollToCardAtPosition(2)
            checkLetter("े", "േ")
            checkLetter("ू", "ൂ")

            // Collapse Everything
            clickCardCategory(0, categories[0])
            clickCardCategory(1, categories[1])
            clickCardCategory(2, categories[2])
            clickCardCategory(3, categories[3])

            clickCardCategory(1, categories[1]) // Expand consonants
            checkLetter("ख", "ഖ")
            checkLetter("भ", "ഭ")

            clickCardCategory(1, categories[1]) // Collapse consonants
            clickCardCategory(3, categories[3]) // Expand ligatures

            chooseTransliterationLanguage("Kannada")
            // Expand everything
            clickCardCategory(0, categories[0])
            clickCardCategory(1, categories[1])
            clickCardCategory(2, categories[2])
            chooseTransliterationLanguage("Malayalam")
            singleClickTest()
        }
    }
}
