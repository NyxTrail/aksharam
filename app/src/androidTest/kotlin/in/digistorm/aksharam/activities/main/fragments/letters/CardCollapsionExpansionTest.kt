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

class CardCollapsionExpansionTest: LettersTabTest() {

    @Test
    fun checkKannadaCategoryCards() {
        runMainActivityTest {
            initialise()

            chooseLanguage("Kannada")
            val categories = listOf("Vowels", "Consonants", "Signs", "Ligatures")
            val charsToTest = listOf(
                listOf("ಅ", "ಏ", "ಅಂ"),
                listOf("ಕ", "ಜ", "ಶ", "ಹ"),
                listOf("ಾ", "ೃ", "ೈ", "ೌ", "್"),
                listOf("ಕ್ತ", "ಸ್ನ", "ರ್")
            )

            checkCharsAtCardPosition(0, charsToTest[0], Visibility.VISIBLE)

            // Collapse category 0 - Vowels
            clickCardCategory(0, categories[0])
            checkCharsAtCardPosition(0, charsToTest[0], Visibility.HIDDEN)

            checkCharsAtCardPosition(3, charsToTest[3], Visibility.VISIBLE)
            // Collapse category 3 - Ligatures
            clickCardCategory(3, categories[3])
            checkCharsAtCardPosition(3, charsToTest[3], Visibility.HIDDEN)

            // Expand category 0 - Vowels
            clickCardCategory(0, categories[0])
            checkCharsAtCardPosition(0, charsToTest[0], Visibility.VISIBLE)

            // Collapse all categories
            clickCardCategory(0, categories[0]) // Collapse Vowels
            clickCardCategory(1, categories[1]) // Collapse Consonants
            clickCardCategory(2, categories[2]) // Collapse Signs
            for ((i, charsOfCategory) in charsToTest.withIndex()) {
                checkCharsAtCardPosition(i, charsOfCategory, Visibility.HIDDEN)
            }

            // Expand all categories
            clickCardCategory(0, categories[0])
            clickCardCategory(1, categories[1])
            clickCardCategory(2, categories[2])
            clickCardCategory(3, categories[3])
            for ((i, charsOfCategory) in charsToTest.withIndex()) {
                checkCharsAtCardPosition(i, charsOfCategory, Visibility.VISIBLE)
            }
        }
    }

    @Test
    fun checkMalayalamCategoryCards() {
        runMainActivityTest {
            initialise()

            chooseLanguage("Malayalam")
            val categories = listOf("Vowels", "Consonants", "Signs", "Chillu", "Ligatures")
            val charsToTest = listOf(
                listOf("ആ", "ഈ", "അം", "അഃ"),
                listOf("ച", "ങ", "ഗ", "ഷ", "ഴ"),
                listOf("ാ", "ു", "ൃ", "ൈ", "ൊ", "ൌ"),
                listOf("ൺ", "ൻ", "ൽ", "ൾ", "ർ"),
                listOf("ട്ട", "ള്ള", "ണ്ണ", "ഞ്ച", "റ്റ", "ന്റ")
            )

            // Collapse category 0 - Vowels
            clickCardCategory(0, categories[0])
            checkCharsAtCardPosition(0, charsToTest[0], Visibility.HIDDEN)

            // Collapse category 3 - Chillu
            checkCharsAtCardPosition(3, charsToTest[3], Visibility.VISIBLE)
            clickCardCategory(3, categories[3])
            checkCharsAtCardPosition(3, charsToTest[3], Visibility.HIDDEN)

            // Expand category 0 - Vowels
            clickCardCategory(0, categories[0])
            checkCharsAtCardPosition(0, charsToTest[0], Visibility.VISIBLE)

            // Collapse category 4 - Ligatures
            clickCardCategory(4, categories[4])
            checkCharsAtCardPosition(4, charsToTest[4], Visibility.HIDDEN)

            // Collapse all categories
            clickCardCategory(0, categories[0]) // Vowels
            clickCardCategory(1, categories[1]) // Consonants
            clickCardCategory(2, categories[2]) // Signs
            for ((i, charsOfCategory) in charsToTest.withIndex()) {
                checkCharsAtCardPosition(i, charsOfCategory, Visibility.HIDDEN)
            }

            // Expand all categories
            clickCardCategory(0, categories[0])
            clickCardCategory(1, categories[1])
            clickCardCategory(2, categories[2])
            clickCardCategory(3, categories[3])
            clickCardCategory(4, categories[4])
            for ((i, charsOfCategory) in charsToTest.withIndex()) {
                checkCharsAtCardPosition(i, charsOfCategory, Visibility.VISIBLE)
            }

            clickCardCategory(0, categories[0])
        }
    }

    @Test
    fun checkHindiCategoryCards() {
        runMainActivityTest {
            initialise()

            chooseLanguage("Hindi")
            val categories = listOf("Vowels", "Consonants", "Signs", "Ligatures")
            val charsToTest = listOf(
                listOf("अ", "औ", "ऋ", "ई", "अं"),
                listOf("ङ", "य", "न", "ड", "फ", "ञ"),
                listOf("ा", "ि", "ू", "ो", "ं", "ः", "्"),
                listOf("क्ष", "ग़", "ज्ञ", "य़", "त्र", "श्र")
            )

            // Collapse category 0 - Vowels
            clickCardCategory(0, categories[0])
            checkCharsAtCardPosition(0, charsToTest[0], Visibility.HIDDEN)

            // Collapse category 2 - Signs
            clickCardCategory(2, categories[2])
            checkCharsAtCardPosition(2, charsToTest[2], Visibility.HIDDEN)

            // Collapse category 3 - Ligatures
            clickCardCategory(3, categories[3])
            checkCharsAtCardPosition(3, charsToTest[3], Visibility.HIDDEN)

            // Expand category 0 - Vowels
            clickCardCategory(0, categories[0])
            checkCharsAtCardPosition(0, charsToTest[0], Visibility.VISIBLE)

            // Collapse all categories
            clickCardCategory(0, categories[0])
            clickCardCategory(1, categories[1])
            for ((i, charsOfCategory) in charsToTest.withIndex()) {
                checkCharsAtCardPosition(i, charsOfCategory, Visibility.HIDDEN)
            }

            // Expand all categories
            clickCardCategory(0, categories[0])
            clickCardCategory(1, categories[1])
            clickCardCategory(2, categories[2])
            clickCardCategory(3, categories[3])
            for ((i, charsOfCategory) in charsToTest.withIndex()) {
                checkCharsAtCardPosition(i, charsOfCategory, Visibility.VISIBLE)
            }
        }
    }
}