package `in`.digistorm.aksharam

import `in`.digistorm.aksharam.util.*
import android.widget.ExpandableListView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.matcher.ViewMatchers.*
import `in`.digistorm.aksharam.activities.main.language.Language
import `in`.digistorm.aksharam.activities.main.language.getDownloadedLanguages
import `in`.digistorm.aksharam.activities.main.language.getLanguageData
import `in`.digistorm.aksharam.activities.main.util.logDebug
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.*
import org.junit.Assert
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class LettersTabTestExp: AksharamTestBaseExp() {
    private val random: Random = Random

    private fun clickLanguageInfo() {
        onView(withId(R.id.lettersTabInfoButton)).perform(click())
        onView(withId(R.id.languageInfoCL)).perform(pressBack())
    }

    private fun clickLetters(language: String) {
        val transliterator = Transliterator(language, ApplicationProvider.getApplicationContext())
        val languageData: Language? = getLanguageData(language, ApplicationProvider
            .getApplicationContext())

        val categories: HashMap<String, ArrayList<String>> = languageData?.lettersCategoryWise
            ?: HashMap()
        if(categories.isEmpty()) {
            assert(false) { "Failed to get letters category wise for language $language. " +
                    "Exiting test." }
            return
        }
        logDebug(logTag, "Letters category wise: $categories")

        val transLanguages: ArrayList<String> = languageData?.supportedLanguagesForTransliteration
            ?: ArrayList()
        if(transLanguages.isEmpty()) {
            assert(false) {
                "Failed to get supported languages for transliteration for $language"
            }
            return
        }

        // track what we have clicked so far
        val clickedLetters = ArrayList<String>()
        for (transLang in transLanguages) {
            // Click the target language selection spinner
            logDebug(logTag, "Clicking $transLang in the transliteration target spinner")
            onView(withId(R.id.lettersTabTransSpinner)).perform(click())
            onData(
                CoreMatchers.allOf(
                    CoreMatchers.`is`(
                        CoreMatchers.instanceOf<Any>(
                            String::class.java
                        )
                    ), CoreMatchers.`is`(transLang)
                )
            ).perform(click())

            // click Language info button
            clickLanguageInfo()
            for ((category, contents)  in categories) {
                var pos = random.nextInt(contents.size)
                // for each category, touch a couple of random letters
                for (i in 0..6) {
                    val letter = contents[pos]
                    val transliteratedLetter = transliterator.transliterate(letter, transLang)
                    if (!clickedLetters.contains(letter)) clickedLetters.add(letter)
                    val longClick = random.nextBoolean()

                    logDebug(logTag, "Loading data for $category in the ExpandableListView")
                    // onData(withId(R.id.LetterGrid))
                    logDebug(logTag, "Clicking: $letter long click: $longClick")
                    onData(allOf(
                        `is`(instanceOf(ArrayList::class.java)),
                        `is`(categories[category])
                    )).inAdapterView(`is`(instanceOf(ExpandableListView::class.java)))
                        .onChildView(withTagValue(`is`(letter)))
                        .perform(if (longClick) ViewActions.longClick() else click())

                    // long click opens a new fragment, return to previous fragment
                    if (longClick) {
                        logDebug(logTag, "Pressing back from info screen for letter: $letter")
                        onView(withId(R.id.letterInfoCL)).perform(pressBack())
                    }
                    // 20% of the time, we choose the previous letter
                    pos = if (random.nextInt(100) < 20) pos else random.nextInt(contents.size)
                }
            }
        }
    }

    fun startTest() {
        logDebug(logTag, "Test started for Letters tab")
        val languages: java.util.ArrayList<String> = getDownloadedLanguages(ApplicationProvider
            .getApplicationContext())
        // Verify each available source language
        Assert.assertNotNull(languages)

        // test each available language
        for (language in languages) { // Click on each language
            // Click the language selection spinner
            onView(withId(R.id.lettersTabLangSpinner)).perform(click())
            logDebug(logTag, "Trying to find $language in lettersTabLangSpinner")
            onData(
                CoreMatchers.allOf(
                    CoreMatchers.`is`(
                        CoreMatchers.instanceOf<Any>(
                            String::class.java
                        )
                    ), CoreMatchers.`is`(language)
                )
            ).perform(click())
            clickLetters(language)
        }
    }
}