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