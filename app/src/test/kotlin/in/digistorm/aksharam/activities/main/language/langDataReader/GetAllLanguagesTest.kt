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
import `in`.digistorm.aksharam.activities.main.language.getAllLanguages
import `in`.digistorm.aksharam.util.AksharamTestBase
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GetAllLanguagesTest: AksharamTestBase() {
    @Test
    fun getLanguagesWithData() {
        try {
            val languages = getAllLanguages(ApplicationProvider.getApplicationContext())
            val languagesWeKnow = arrayListOf("Kannada", "Malayalam", "Hindi")
            for ((languageName, language) in languages) {
                if (languageName !in languagesWeKnow) {
                    fail("Unknown language found: $languageName")
                } else {
                    when (languageName) {
                        "Kannada" -> assertEquals("ka", language.code)
                        "Malayalam" -> assertEquals("ml", language.code)
                        "Hindi" -> assertEquals("hi", language.code)
                    }
                }
            }
        } catch (e: Exception) {
            fail("Exception caught: ${e.message}")
        }
    }
}
