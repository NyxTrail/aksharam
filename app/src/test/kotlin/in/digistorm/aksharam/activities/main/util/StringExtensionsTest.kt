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