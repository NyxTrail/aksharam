package `in`.digistorm.aksharam.activities.main.fragments.practice

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import `in`.digistorm.aksharam.activities.main.language.getLanguageData
import `in`.digistorm.aksharam.activities.main.language.transliterate
import `in`.digistorm.aksharam.util.containsOneOf
import org.junit.Test

class MalayalamTest: PracticeTabTest() {
    override val logTag: String = this.javaClass.simpleName
    private val language: String = "Malayalam"
    private val practiceInLanguages = listOf(
        "Hindi", "Kannada"
    )
    private val practiceTypes = listOf(
        "Vowels", "Consonants", "Signs", "Ligatures", "Random Words"
    )

    @Test
    fun practiceRandomWordsInHindi() {
        chooseLanguage(language)
        choosePracticeInLanguage(practiceInLanguages[0])
        choosePracticeType(practiceTypes.last())

        val malayalamData = getLanguageData(
            "malayalam.json",
            ApplicationProvider.getApplicationContext()
        )!!
        val targetLanguage = "Hindi"

        // Test a practice string that contains a ചില്ല് (pure consonant)
        var counter = 1
        var practiceText: String
        do {
            clickRefreshButton()
            practiceText = getPracticeText()!!
            counter++
        } while(!practiceText.containsOneOf(malayalamData.chillu))
        Log.d(logTag, "Found string with chillu: $practiceText after $counter tries.")
        val transliteratedString = transliterate(practiceText, targetLanguage, malayalamData)
        inputText(transliteratedString)
        checkTransliterationSuccess()
    }
}