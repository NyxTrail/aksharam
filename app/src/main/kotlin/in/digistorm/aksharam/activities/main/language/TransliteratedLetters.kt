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
package `in`.digistorm.aksharam.activities.main.language

data class TransliteratedLetters(
    // Language the letters are transliterated from
    val sourceLanguage: String,
    // Language the letters are transliterated to
    val targetLanguage: String,
    // Categories of letters: Vowels, Consonants, Signs, Ligatures, etc
    val categories: ArrayList<Category>
)

data class Category(
    // Name of the category: Vowels, Consonants, Signs, Ligatures, etc
    val name: String,
    val letterPairs: ArrayList<Pair<String, String>>
)