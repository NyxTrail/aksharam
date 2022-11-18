package `in`.digistorm.aksharam

import `in`.digistorm.aksharam.util.logDebug
import android.content.Context
import android.text.Html
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.*
import org.junit.Assert
import java.io.BufferedReader
import java.io.IOException
import java.util.*

class TransliterateTabTestExp: AksharamTestBaseExp() {
    fun getData(context: Context): Map<String, List<Map<String, String>>> {
        val reader: BufferedReader = context.resources.openRawResource(
            `in`.digistorm.aksharam.test.R.raw.transliterate_tab_test_data).bufferedReader()
        val mapper: ObjectMapper = jacksonObjectMapper()
        return mapper.readValue(reader)
    }

    fun startTest() {
        logDebug(logTag, "Test started for Transliterate Tab")
        try {
            val testData = getData(
                // This is the test application's context
                InstrumentationRegistry.getInstrumentation().context)
            for ((language, languageData) in testData) {
                for (testCase in languageData) {
                    val src = testCase["src"] ?: continue
                    for (targetLanguage in testCase.keys) {
                        // ignore "src"
                        if (targetLanguage.lowercase() == "src") continue
                        logDebug(logTag, "Testing $language to $targetLanguage transliteration.")
                        logDebug(logTag, "Source text: $src")
                        val storedTransliteration = testCase[targetLanguage]
                        logDebug(logTag, "Stored transliteration is: $storedTransliteration")

                        // Enter the test string into transliteration input
                        onView(withId(R.id.TransliterateTabInputTextField))
                            .perform(ViewActions.replaceText(src))

                        // Actual transliteration output in the app
                        val transliteration = getText(withId(R.id.TransliterateTabOutputTextView))

                        /* If transliteration is equal to the source, app was unable to transliterate the source text.
                           This happens if all data files are not downloaded in the app.
                        */
                        if (transliteration == src) {
                            logDebug(
                                logTag,
                                "Transliteration result is same as input text. Data file is not available for: "
                                        + language
                            )
                            logDebug(logTag, "Checking if the app displayed its failure message.")
                            val errorText = Html.fromHtml(
                                ApplicationProvider.getApplicationContext<Context>()
                                    .getText(R.string.lang_could_not_detect).toString()
                            ).toString()
                            onView(withId(R.id.TransliterateTabInfoTV))
                                .check(matches(ViewMatchers.withText(errorText)))
                        } else {
                            onView(withId(R.id.LanguageSelectionSpinner)).check(matches(isEnabled()))
                            onView(withId(R.id.LanguageSelectionSpinner))
                                .perform(ViewActions.click())
                            onData(allOf(
                                    `is`(instanceOf<Any?>(String::class.java)),
                                    `is`(upperCaseFirstLetter(targetLanguage))
                                )).perform(ViewActions.click())
                            onView(withId(R.id.TransliterateTabOutputTextView)).check(
                                matches(ViewMatchers.withText(storedTransliteration)))
                        }
                    }
                }
            }
        } catch (e: IOException) {
            logDebug(logTag, "Error reading test data from JSON File")
            e.printStackTrace()
        }
    }
}