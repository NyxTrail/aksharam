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
package `in`.digistorm.aksharam.activities.main.language.langDataReader

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import `in`.digistorm.aksharam.activities.main.language.getLanguageData
import `in`.digistorm.aksharam.util.AksharamTestBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GetLanguageDataTest: AksharamTestBase() {
    @Test
    fun getHindiData() {
        val language = getLanguageData("hindi", ApplicationProvider.getApplicationContext())!!
        assertEquals("Hindi", language.language)
        assertEquals("hi", language.code)
        assertEquals("क", language.consonants.first())
    }

    @Test
    fun getMalayalamData() {
        val language = getLanguageData("malayalam", ApplicationProvider.getApplicationContext())!!
        assertEquals("Malayalam", language.language)
        assertEquals("ml", language.code)
        assertEquals("ക", language.consonants.first())
    }

    @Test
    fun getKannadaData() {
        val language = getLanguageData("kannada", ApplicationProvider.getApplicationContext())!!
        assertEquals("Kannada", language.language)
        assertEquals("ka", language.code)
        assertEquals("ಕ", language.consonants.first())
    }

    @Test
    fun `file name with json extension`() {
        val language = getLanguageData("kannada.json", ApplicationProvider.getApplicationContext())!!
        assertEquals("Kannada", language.language)
        assertEquals("ka", language.code)
        assertEquals("ಕ", language.consonants.first())
    }

    @Test
    fun `file name capitalized`() {
        val language = getLanguageData("KANNADA.json", ApplicationProvider.getApplicationContext())!!
        assertEquals("Kannada", language.language)
        assertEquals("ka", language.code)
        assertEquals("ಕ", language.consonants.first())
    }

    @Test
    fun `invalid file name`() {
        val language = getLanguageData("kannada.txt", ApplicationProvider.getApplicationContext())
        assertNull(language)
    }
}
