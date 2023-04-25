package `in`.digistorm.aksharam.activities.main.language.langDataReader

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import `in`.digistorm.aksharam.activities.main.language.getDownloadedLanguages
import `in`.digistorm.aksharam.util.AksharamTestBase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GetDownloadedLanguagesTest: AksharamTestBase() {
    @Test
    fun getLanguages() {
        val languageSet = setOf<String>()
            .plus(getDownloadedLanguages(ApplicationProvider.getApplicationContext()))
        val languagesWeKnow = setOf<String>()
            .plus(arrayListOf("Kannada", "Malayalam", "Hindi"))
        assertEquals("Language collection does not match with the ones defined in test.",
            languagesWeKnow,
            languageSet)
    }
}