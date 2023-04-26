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