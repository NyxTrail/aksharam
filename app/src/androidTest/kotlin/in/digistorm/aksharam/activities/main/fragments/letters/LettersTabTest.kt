package `in`.digistorm.aksharam.activities.main.fragments.letters

import android.util.Log
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.PositionAssertions.isBottomAlignedWith
import androidx.test.espresso.assertion.PositionAssertions.isTopAlignedWith
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import `in`.digistorm.aksharam.AksharamTestBaseExp
import `in`.digistorm.aksharam.DELAYS
import `in`.digistorm.aksharam.activities.main.MainActivity
import org.hamcrest.Matchers.*
import org.junit.Rule
import `in`.digistorm.aksharam.R
import org.junit.After
import org.junit.Before

enum class Visibility { VISIBLE, HIDDEN }

@LargeTest
open class LettersTabTest: AksharamTestBaseExp() {
    override val logTag: String = this.javaClass.simpleName

    @Before
    open fun initialise() {
        // Make sure we are on the letters tab.
        Log.d(logTag, "Selecting the letters tab.")
        onView(withText(R.string.letters_tab_header)).perform(click())
        DELAYS.UI_WAIT_TIME.waitShortDuration()
    }

    @get: Rule
    var mainActivityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    protected fun chooseLanguage(language: String): ViewInteraction {
        Log.d(logTag, "Choosing language: $language")
        onView(withId(R.id.language_selector)).perform(click())
        return onData(`is`(language))
            .inRoot(RootMatchers.isPlatformPopup())
            .perform(click())
            .also {
                DELAYS.UI_WAIT_TIME.waitShortDuration()
            }
    }

    protected fun chooseTransliterationLanguage(language: String) {
        onView(withId(R.id.convert_to_selector)).perform(click())
        onData(`is`(language))
            .inRoot(RootMatchers.isPlatformPopup())
            .perform(click())
            .also {
                DELAYS.UI_WAIT_TIME.waitShortDuration()
            }
    }

    protected fun checkLetter(firstLetter: String, secondLetter: String) {
        onView(
            allOf(
                instanceOf(LetterPairView::class.java),
                withTagValue(`is`(firstLetter))
            )
        )
            .perform(scrollTo())
            .perform(click())
            .check(matches(withText(secondLetter)))
    }

    protected fun checkLetterHidden(letter: String) {
	onView(
	    allOf(
		instanceOf(LetterPairView::class.java),
		withTagValue(`is`(letter))
	    )).check(matches(not(isDisplayed())))
    }
    
    protected fun scrollToCardAtPosition(position: Int) {
        onView(withId(R.id.letter_categories)).perform(
            RecyclerViewActions.scrollToPosition<LetterCategoryAdapter.LetterCategoryCardViewHolder>(
                position
            )
        )
        DELAYS.UI_WAIT_TIME.waitShortDuration()
    }

    protected fun clickCardCategory(position: Int, category: String, retrying: Boolean = false) {
        scrollToCardAtPosition(position)
        Log.d(logTag, "SCROLLED TO: $category ")
        onView(withText(category)).perform(scrollTo()).perform(click())
    }

    protected fun longClickLetter(letter: String): ViewInteraction {
        return onView(
            allOf(
                instanceOf(LetterPairView::class.java),
                withTagValue(`is`(letter))
            )
        )
            .perform(scrollTo())
            .perform(longClick())
    }

    protected fun checkLetterInfoHeadingAlignment() {
        onView(withId(R.id.words_heading)).check(isTopAlignedWith(withId(R.id.meaning_heading)))
        onView(withId(R.id.words_heading)).check(isBottomAlignedWith(withId(R.id.meaning_heading)))
    }

    protected fun checkLetterInfoHeading(heading: String, transliteratedHeading: String) {
        onView(withId(R.id.heading))
            .check(matches(withText(startsWith(heading))))
        onView(withId(R.id.transliterated_heading))
            .check(matches(withText(startsWith(transliteratedHeading))))
    }

    protected fun checkWordAndMeaningHeadingHidden() {
        onView(withId(R.id.words_heading)).check(matches(not(isDisplayed())))
        onView(withId(R.id.meaning_heading)).check(matches(not(isDisplayed())))
    }

    protected fun checkWordAndMeaningDisplayed(
        word: String,
        transliteratedWord: String,
        meaning: String
    ) {
        onView(withId(R.id.letter_info_container)).check(
            matches(hasDescendant(
                allOf(withText(startsWith(word)), withId(R.id.word))
            ))
        )
        onView(allOf(withId(R.id.word), withText(startsWith(word)))).perform(scrollTo())
        onView(withId(R.id.letter_info_container)).check(
            matches(hasDescendant(
                allOf(withText(startsWith(transliteratedWord)), withId(R.id.transliteration))
            ))
        )
        onView(allOf(withId(R.id.transliteration), withText(startsWith(transliteratedWord)))).perform(scrollTo())
        onView(withId(R.id.letter_info_container)).check(
            matches(hasDescendant(
                allOf(withText(startsWith(meaning)), withId(R.id.meaning))
            ))
        )
        onView(allOf(withId(R.id.meaning), withText(startsWith(meaning)))).perform(scrollTo())
    }

    protected fun checkDiacriticHintsHidden() {
        onView(withId(R.id.diacritic_hint)).check(matches(not(isDisplayed())))
        onView(withId(R.id.diacritic_examples_container)).check(matches(not(isDisplayed())))
    }

    protected fun checkLigaturesWithLetterAsPrefixHidden() {
        onView(withId(R.id.ligatures_with_letter_as_prefix_tv))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        onView(withId(R.id.ligatures_with_letter_as_prefix_gl))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    protected fun checkLigaturesWithLetterAsSuffixHidden() {
        onView(withId(R.id.ligatures_with_letter_as_suffix_tv))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        onView(withId(R.id.ligatures_with_letter_as_suffix_gl))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    protected fun checkInfoHidden() {
        onView(withId(R.id.info)).check(matches(not(isDisplayed())))
    }

    protected fun checkInfoDisplayed(subString: String) {
        onView(withId(R.id.info)).check(matches(withText(containsString(subString))))
    }

    protected fun checkDiacriticHint(subString: String) {
        onView(withId(R.id.diacritic_hint)).check(matches(withText(startsWith(subString))))
    }

    protected fun checkCombineSignWithConsonants(sign: String, consonants: List<String>) {
        // According to the docs this is not recommended:
        // https://developer.android.com/training/testing/espresso/basics#checking-view-assertions
        for (consonant in consonants) {
            onView(withText(startsWith(consonant + sign))).perform(scrollTo())
                .check(matches(isDisplayed()))
        }
    }

    protected fun checkCombineConsonantWithVowelSigns(consonant: String, vowelSigns: List<String>) {
        onView(withId(R.id.diacritic_hint)).check(matches(withText(startsWith(consonant))))
        for (sign in vowelSigns) {
            onView(withId(R.id.diacritic_examples_container))
                .check(matches(withChild(withText(consonant + sign))))
        }
    }

    protected fun checkConsonantAsPrefix(
        consonant: String,
        virama: String,
        combineWith: List<String>
    ) {
        onView(withId(R.id.ligatures_with_letter_as_prefix_tv))
            .check(matches(withText(endsWith("($consonant + $virama + x):"))))
        for(suffix in combineWith) {
            onView(withId(R.id.ligatures_with_letter_as_prefix_gl))
                .check(matches(withChild(withText(consonant + virama + suffix))))
        }
    }

    protected fun checkConsonantAsSuffix(
        consonant: String,
        virama: String,
        combineWith: List<String>
    ) {
        onView(withId(R.id.ligatures_with_letter_as_suffix_tv))
            .check(matches(withText(endsWith("(x + $virama + $consonant):"))))
        for(prefix in combineWith) {
            onView(withId(R.id.ligatures_with_letter_as_suffix_gl))
                .check(matches(withChild(withText(prefix + virama + consonant))))
        }
    }

    fun checkCharsAtCardPosition(
        position: Int,
        chars: List<String>,
        visibility: Visibility
    ) {
        scrollToCardAtPosition(position)
        for(char in chars) {
            if(visibility == Visibility.VISIBLE)
                onView(allOf(
                    instanceOf(LetterPairView::class.java), withTagValue(`is`(char))
                )).perform(scrollTo())

            onView(withId(R.id.letter_categories)).check(matches(
                hasDescendant(
                    allOf(
                        withTagValue(`is`(char)),
                        when(visibility) {
                            Visibility.HIDDEN -> not(isDisplayed())
                            Visibility.VISIBLE -> isDisplayed()
                        }
                    )
                )
            ))
        }
    }
}
