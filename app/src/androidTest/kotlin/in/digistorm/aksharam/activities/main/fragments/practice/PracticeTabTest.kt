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
package `in`.digistorm.aksharam.activities.main.fragments.practice

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import `in`.digistorm.aksharam.AksharamTestBaseExp
import org.junit.Before
import `in`.digistorm.aksharam.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not

// Delay in milliseconds before updating EdiText with new string.
private const val TYPING_DELAY: Long = 10
// Delay in milliseconds to wait UI update before continuing tests.
private const val UI_WAIT_TIME: Long = 500

open class PracticeTabTest: AksharamTestBaseExp() {
    override val logTag: String = this.javaClass.simpleName

    @Before
    open fun beforeTest() {
        downloadLanguageDataBeforeTest()
    }

    protected open fun initialise() {
        log("Selecting the practice tab.")
        onView(withText(R.string.practice_tab_header)).perform(click())
        // Without this wait, during second test the click on language_selector (which is the next action
        // in most tests) does not happen.
        runBlocking { delay(UI_WAIT_TIME) }
    }

    protected fun chooseLanguage(language: String): ViewInteraction {
        log("Choosing language: $language")
        onView(withId(R.id.language_selector)).perform(click())
        return onData(`is`(language)).inRoot(RootMatchers.isPlatformPopup()).perform(click())
    }

    protected fun choosePracticeInLanguage(language: String) {
        onView(withId(R.id.practice_in_selector)).perform(click())
        onData(`is`(language)).inRoot(RootMatchers.isPlatformPopup()).perform(click())
    }

    protected fun choosePracticeType(practiceType: String) {
        onView(withId(R.id.practice_type_selector)).perform(click())
        onData(`is`(practiceType)).inRoot(RootMatchers.isPlatformPopup()).perform(click())
    }

    protected fun clickRefreshButton() {
        onView(withId(R.id.refresh_button)).perform(click())
    }

    protected fun getPracticeText(): String? {
        return getText(withId(R.id.practice_text))
    }

    protected fun inputText(text: String) {
        // Simulate typing by replacing the string character by character with
        // a small delay before a new character is input into the edit text.
        for(i in 1 .. text.length) {
            onView(withId(R.id.practice_input_edit_text))
                .perform(replaceText(text.substring(0, i)))
            runBlocking { delay(TYPING_DELAY) }
        }
        runBlocking { delay(UI_WAIT_TIME) }
    }

    protected fun checkTransliterationSuccess() {
        onView(withId(R.id.practice_input_edit_text)).check(matches(not(isEnabled())))
    }
}
