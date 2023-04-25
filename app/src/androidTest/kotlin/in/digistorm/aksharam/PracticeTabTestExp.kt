package `in`.digistorm.aksharam

import `in`.digistorm.aksharam.activities.main.language.Language
import `in`.digistorm.aksharam.activities.main.util.logDebug
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.action.ViewActions
import org.hamcrest.CoreMatchers
import org.junit.Assert

class PracticeTabTestExp(private val languages: LinkedHashMap<String, Language>): AksharamTestBaseExp() {
    fun startTest() {
        logDebug(logTag, "Test started for practice tab")
        Assert.assertNotNull(languages)
        for ((key, language) in languages) {
            val languageName = upperCaseFirstLetter(key)
            val practiceIn = language.supportedLanguagesForTransliteration
            logDebug(logTag, "Clicking the Language spinner")
            onView(withId(R.id.PracticeTabLangSpinner)).perform(ViewActions.click())
            logDebug(logTag, "Trying to find $languageName in Language spinner")
            onData(
                CoreMatchers.allOf(
                    CoreMatchers.`is`(
                        CoreMatchers.instanceOf<Any?>(
                            String::class.java
                        )
                    ), CoreMatchers.`is`(languageName)
                )
            ).perform(ViewActions.click())
            val lettersCategoryWise = language.lettersCategoryWise
            val practiceTypes = ArrayList<String>(lettersCategoryWise.keys)
            if (language.areLigaturesAutoGeneratable()) practiceTypes.add("Random Ligatures")
            practiceTypes.add("Random Words")
            val transliterator = Transliterator(
                languageName!!.lowercase(),
                ApplicationProvider.getApplicationContext()
            )
            for (practiceInLanguage in practiceIn) {
                logDebug(logTag, "Clicking the Practice In spinner")
                onView(withId(R.id.PracticeTabPracticeInSpinner))
                    .perform(ViewActions.click())
                logDebug(logTag, "Trying to find $practiceInLanguage in Practice In Spinner")
                onData(
                    CoreMatchers.allOf(
                        CoreMatchers.`is`(
                            CoreMatchers.instanceOf<Any>(
                                String::class.java
                            )
                        ), CoreMatchers.`is`(practiceInLanguage)
                    )
                ).perform(ViewActions.click())
                for (practiceType in practiceTypes) {
                    onView(withId(R.id.PracticeTabPracticeTypeSpinner))
                        .perform(ViewActions.click())
                    logDebug(logTag, "Trying to find practice type: $practiceType")
                    onData(
                        CoreMatchers.allOf(
                            CoreMatchers.`is`(
                                CoreMatchers.instanceOf<Any?>(
                                    String::class.java
                                )
                            ), CoreMatchers.`is`<String?>(upperCaseFirstLetter(practiceType))
                        )
                    ).perform(ViewActions.click())
                    val practiceText = getText(withId(R.id.PracticeTabPracticeTextTV))
                    logDebug(logTag, "Obtained practice text: $practiceText")
                    val transliteratedString =
                        transliterator.transliterate(practiceText!!, practiceInLanguage)
                    logDebug(logTag, "Expected transliteration: $transliteratedString")
                    onView(withId(R.id.PracticeTabInputTIET))
                        .perform(ViewActions.replaceText(transliteratedString))
                }
            }
        }
    }
}