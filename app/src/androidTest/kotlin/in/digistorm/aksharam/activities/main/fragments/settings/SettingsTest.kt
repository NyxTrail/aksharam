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
package `in`.digistorm.aksharam.activities.main.fragments.settings

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import `in`.digistorm.aksharam.AksharamTestBaseExp
import `in`.digistorm.aksharam.DELAYS
import org.junit.Test

import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.activities.main.MainActivity
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Before

open class SettingsTest: AksharamTestBaseExp() {
    override val logTag: String = this.javaClass.simpleName

    protected var files = arrayListOf<String>()

    @Before
    fun downloadMissingFiles() {
        downloadLanguageDataBeforeTest()
    }

    @Test
    fun navigateToSettingsTabAndBack() {
        runMainActivityTest {

            val allLanguages = listOf("Hindi", "Kannada", "Malayalam")
            ActivityScenario.launch(MainActivity::class.java).use {
                log("Testing settings screen.")
                it.onActivity { activity ->
                    log("Registering idling resource")
                    IdlingRegistry.getInstance().register(activity.getIdlingResource())
                }

                // Verify all Kannada language exists
                goToLettersTab()
                verifyLanguageExistsInLettersTab("Kannada")
                checkLetter("ಅ")

                goToSettingsFragment()
                DELAYS.UI_WAIT_TIME.waitMediumDuration()

                checkLanguagesInSettings(allLanguages)

                deleteLanguage("Kannada")
                onView(withId(R.id.language_list)).perform(pressBack())
                DELAYS.UI_WAIT_TIME.waitLongDuration()
                verifyLanguageNotExistsInLettersTab("Kannada", "Malayalam")

                verifyLanguageExistsInLettersTab("Malayalam")
                verifyLanguageExistsInLettersTab("Hindi")

                goToSettingsFragment()
                DELAYS.UI_WAIT_TIME.waitMediumDuration()

                checkLanguagesInSettings(allLanguages)

                deleteLanguage("Malayalam")
                onView(withId(R.id.language_list)).perform(pressBack())
                DELAYS.UI_WAIT_TIME.waitShortDuration()
                verifyLanguageNotExistsInLettersTab("Malayalam", "Hindi")

                verifyLanguageExistsInLettersTab("Hindi")

                goToSettingsFragment()
                DELAYS.UI_WAIT_TIME.waitShortDuration()

                checkLanguagesInSettings(allLanguages)

                deleteLanguage("Hindi")
                onView(withId(R.id.language_list)).perform(pressBack())

                checkInInitialisationFragment()
                downloadAllLanguagesFromInitialisationScreen()

                goToSettingsFragment()
                checkLanguagesInSettings(allLanguages)
                allLanguages.forEach { language ->
                    deleteLanguage(language)
                    downloadLanguageFromSettingsFragment(language)
                }

                onView(withId(R.id.language_list)).perform(pressBack())
                allLanguages.forEach { language ->
                    verifyLanguageExistsInLettersTab(language)
                }
            }
        }
    }

    private fun verifyLanguageExistsInLettersTab(language: String) {
        log("Choosing language: $language in letters tab")

        onView(withId(R.id.language_selector)).perform(click())
        onData(`is`(language))
            .inRoot(RootMatchers.isPlatformPopup())
            .perform(click())
            .also {
                DELAYS.UI_WAIT_TIME.waitShortDuration()
            }
    }

    private fun verifyLanguageNotExistsInLettersTab(notExistingLanguage: String, existingLanguage: String) {
        log("Checking language: $notExistingLanguage does not exist in letters tab")
        onView(withId(R.id.language_selector)).perform(click())

        onView(withChild(withText(existingLanguage)))
            .inRoot(RootMatchers.isPlatformPopup())
            .check(matches(not(hasDescendant(withText(notExistingLanguage)))))
            .perform(click()) // Click to close the language selection pop up
    }

    private fun checkLetter(letter: String) {
        onView(withTagValue(`is`("vowels")))
            .perform(scrollTo())
            .check(matches(hasDescendant(withText(letter))))
    }

    private fun goToLettersTab() {
        log("Selecting the letters tab")
        onView(withText(R.string.letters_tab_header)).perform(click())
        DELAYS.UI_WAIT_TIME.waitShortDuration()
    }

    private fun goToSettingsFragment() {
        onView(withId(R.id.more_options)).perform(click())
        onView(withText("Settings"))
            .inRoot(RootMatchers.isPlatformPopup())
            .perform(click())
    }

    private fun checkLanguagesInSettings(list: List<String>) {
        for(language in list) {
            onView(withId(R.id.language_list))
                .check(matches(hasDescendant(withText(language))))
        }
    }

    private fun deleteLanguage(language: String) {
        log("Deleting language: $language")
        onView(allOf(withId(R.id.delete_language), hasSibling(withText(language))))
            .perform(click())
    }

    private fun checkInInitialisationFragment() {
        onView(withId(R.id.file_list))
            .check(matches(allOf(
                hasDescendant(withText("Kannada")),
                hasDescendant(withText("Malayalam")),
                hasDescendant(withText("Hindi")),
            )))
    }

    private fun downloadAllLanguagesFromInitialisationScreen() {
        onView(withText("Kannada")).perform(click())
        onView(withText("Hindi")).perform(click())
        onView(withText("Malayalam")).perform(click())

        onView(withId(R.id.proceed_button)).perform(click())
        DELAYS.UI_WAIT_TIME.waitLongDuration()
    }

    private fun downloadLanguageFromSettingsFragment(language: String) {
        onView(allOf(
            withId(R.id.download_language),
            hasSibling(withText(language))
        )).perform(click())
        DELAYS.UI_WAIT_TIME.waitLongDuration()
    }
}
