package `in`.digistorm.aksharam

import `in`.digistorm.aksharam.activities.main.initialise.InitialiseAppActivity
import `in`.digistorm.aksharam.activities.main.MainActivity
import `in`.digistorm.aksharam.activities.main.language.Language
import `in`.digistorm.aksharam.util.getAllLanguages
import `in`.digistorm.aksharam.util.logDebug
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.hamcrest.CoreMatchers
import org.junit.Rule
import org.junit.Test

class MainActivityTestExp: AksharamTestBaseExp() {
    @get: Rule
    var initActivityRule: ActivityScenarioRule<InitialiseAppActivity> =
        ActivityScenarioRule(InitialiseAppActivity::class.java)

    @get: Rule
    var mainActivityRule: ActivityScenarioRule<MainActivity> = ActivityScenarioRule(MainActivity::class.java)

    private var languages: LinkedHashMap<String, Language>? = null

    init {
        languages = getAllLanguages(ApplicationProvider.getApplicationContext())
    }

    private fun openTab(resourceId: Int) {
        // Tab headings are assigned automatically by the system and does not have an ID
        // Use this to narrow the match, since these words may be used elsewhere in the system
        onView(
            CoreMatchers.allOf(
                ViewMatchers.withText(resourceId),
                withId(-1)
            )
        ).perform(ViewActions.click())
    }

    private fun sleep() {
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /* This test is flaky. Swipe actions are not supported for ViewPager2.
       I couldn't get the adapter for the pager work with onData either.
       This test must be started on the Letters Tab.
     */
    private fun swipeTest() {
        // Swipe tests
        onView(withId(R.id.lettersTabCL)).perform(ViewActions.swipeLeft())
        sleep()
        onView(withId(R.id.transliterateTabCL)).perform(ViewActions.swipeLeft())
        sleep()
        // This doesn't do anything
        onView(withId(R.id.practiceTabNSV)).perform(ViewActions.swipeLeft())

        // Come back to letters tab
        sleep()
        onView(withId(R.id.practiceTabNSV)).perform(ViewActions.swipeRight())
        sleep()
        onView(withId(R.id.transliterateTabCL)).perform(ViewActions.swipeRight())
    }

    @Test
    fun mainActivityTest() {
        logDebug(logTag, "Main activity test")

        // This is flaky and slow
        // swipeTest();

        // Click on the tab headings
        openTab(R.string.letters_tab_header)
        openTab(R.string.practice_tab_header)
        openTab(R.string.transliterate_tab_header)
        openTab(R.string.letters_tab_header)
        LettersTabTestExp().startTest()
        openTab(R.string.transliterate_tab_header)
        TransliterateTabTestExp().startTest()
        openTab(R.string.practice_tab_header)
        PracticeTabTestExp(languages!!).startTest()
    }
}