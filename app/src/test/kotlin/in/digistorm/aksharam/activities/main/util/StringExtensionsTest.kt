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
package `in`.digistorm.aksharam.activities.main.util

import `in`.digistorm.aksharam.activities.main.helpers.upperCaseFirstLetter
import junit.framework.TestCase.assertEquals
import org.junit.Test

class UpperCaseFirstLetterTest {
    @Test
    fun wordWithLowerCaseFirstLetter() {
        assertEquals("Malayalam", "malayalam".upperCaseFirstLetter())
    }

    @Test
    fun wordWithFileType() {
        assertEquals("Malayalam.json", "malayalam.json".upperCaseFirstLetter())
    }

    @Test
    fun wordWithUpperCaseFirstLetter() {
        assertEquals("Malayalam", "malayalam".upperCaseFirstLetter())
    }

    @Test
    fun wordWithSpaceFirstLetter() {
        assertEquals(" malayalam", " malayalam".upperCaseFirstLetter())
    }

    @Test
    fun wordWithSymbolFirstLetter() {
        assertEquals("!malayalam", "!malayalam".upperCaseFirstLetter())
    }

    @Test
    fun wordWithNonEnglishFirstLetter() {
        assertEquals("ಮಲಯಾಳಂ", "ಮಲಯಾಳಂ".upperCaseFirstLetter())
    }
}