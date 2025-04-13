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
package `in`.digistorm.aksharam.activities.main.fragments.initialise

import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import `in`.digistorm.aksharam.AksharamTestBaseExp
import `in`.digistorm.aksharam.DELAYS
import `in`.digistorm.aksharam.activities.main.MainActivity
import `in`.digistorm.aksharam.activities.main.util.deleteFile
import `in`.digistorm.aksharam.activities.main.util.getLocalFiles
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.activities.main.util.IdlingResourceHelper
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not

class InitialisationTest: AksharamTestBaseExp() {
    override val logTag: String = this. javaClass.simpleName

    @Before
    fun deleteAllFiles() {
        runMainActivityTest {
            runBlocking {
                val downloadedLanguages = getLocalFiles(ApplicationProvider.getApplicationContext())

                downloadedLanguages.forEach {
                    log("Deleting file: $it")
                    deleteFile(it, ApplicationProvider.getApplicationContext())
                }
            }
        }
    }

    @Test
    fun initialisationScreenTest() {
        runActivityTest(MainActivity::class.java) {
            log("Testing initialisation screen.")
            it.onActivity {  activity ->
                log("Registering idling resource")
                IdlingRegistry.getInstance().register(activity.getIdlingResource())
            }

            val languages = listOf("Kannada", "Hindi", "Malayalam")
            DELAYS.UI_WAIT_TIME.waitLongDuration()
            verifyLanguagesExistInInitialisationScreen(languages)
            checkProceedButtonDisabled()
            clickProceedButton()

            for (language in languages) {
                clickLanguage(language)
            }

            checkProceedButtonEnabled()
            clickProceedButton()

            for (language in languages) {
                verifyLanguageExistsInLettersTab(language)
            }
        }
    }

    private fun verifyLanguageExistsInLettersTab(language: String) {
        Log.d(logTag, "Choosing language: $language in letters tab")
        onView(withId(R.id.language_selector)).perform(click())
        onData(`is`(language))
            .inRoot(RootMatchers.isPlatformPopup())
            .perform(click())
            .also {
                DELAYS.UI_WAIT_TIME.waitShortDuration()
            }
    }

    private fun checkProceedButtonDisabled() {
        onView(withId(R.id.proceed_button)).check(matches(
            not(isEnabled())
        ))
    }

    private fun checkProceedButtonEnabled() {
        onView(withId(R.id.proceed_button)).check(matches(
            isEnabled()
        ))
    }

    private fun clickProceedButton() {
        onView(withId(R.id.proceed_button)).perform(click())
    }

    private fun verifyLanguagesExistInInitialisationScreen(languages: List<String>) {
        for(language in languages) {
            onView(withId(R.id.file_list)).check(matches(
                hasDescendant(withText(language))
            ))
        }
    }

    private fun clickLanguage(language: String) {
        onView(withText(language)).perform(click())
    }
}
