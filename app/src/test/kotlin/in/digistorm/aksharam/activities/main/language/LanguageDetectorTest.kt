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

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import `in`.digistorm.aksharam.util.AksharamTestBase
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LanguageDetectorTest: AksharamTestBase() {
    @Test
    fun `is malayalam detection correct`() {
        val testStrings = listOf(
            "എന്റെ പേര് അക്ഷരം.",
            "1 2 ക ഖ ഗ ഘ",
            "ಕ ಖ യ ര ല വ"
        )

        val languageDetector = LanguageDetector(ApplicationProvider.getApplicationContext())
        for(testString in testStrings) {
            assertEquals(
                "Malayalam",
                languageDetector.detectLanguage(testString)?.language
            )
        }
    }

    @Test
    fun `is hindi detection correct`() {
        val testStrings = listOf(
            "मेरा नाम अक्षरं.",
            "1 2 क ख ग घ",
            "ಕ ಖ य र ल व"
        )

        val languageDetector = LanguageDetector(ApplicationProvider.getApplicationContext())
        for(testString in testStrings) {
            assertEquals(
                "Hindi",
                languageDetector.detectLanguage(testString)?.language
            )
        }
    }

    @Test
    fun `is kannada detection correct`() {
        val testStrings = listOf(
            "ನನ್ನ ಹೆಸರು ಅಲನ್.",
            "1 2 ಕ ಖ ಗ ಘ",
            "क ख ಯ ರ ಲ ವ"
        )

        val languageDetector = LanguageDetector(ApplicationProvider.getApplicationContext())
        for(testString in testStrings) {
            assertEquals(
                "Kannada",
                languageDetector.detectLanguage(testString)?.language
            )
        }
    }

    @Test
    fun `when input language is unknown`() {
        val testStrings = listOf(
            "1 2 3 4",
            "abc def ghi",
            "abc? def! ghi!!",
        )

        val languageDetector = LanguageDetector(ApplicationProvider.getApplicationContext())
        for(testString in testStrings) {
            assertNull(
                testString,
                languageDetector.detectLanguage(testString)?.language
            )
        }
    }
}