package `in`.digistorm.aksharam

import android.util.Log
import android.view.View
import android.widget.Spinner
import android.widget.TextView
import androidx.test.espresso.AmbiguousViewMatcherException
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matcher

enum class DELAYS(
    val VERY_SHORT: Long = 10,
    val SHORT: Long = 100,
    val MEDIUM: Long = 200,
    val LONG: Long = 500
) {
    TYPING_DELAY,
    UI_WAIT_TIME;

    fun waitVeryShortDuration() {
        runBlocking { delay(SHORT) }
    }

    fun waitShortDuration() {
        runBlocking { delay(SHORT) }
    }

    fun waitMediumDuration() {
        runBlocking { delay(MEDIUM) }
    }

    fun waitLongDuration() {
        runBlocking { delay(LONG) }
    }
}

open class AksharamTestBaseExp {
    open val logTag: String = javaClass.simpleName

    /* Obtain text from a text view.
       https://stackoverflow.com/a/23467629
     */
    protected fun getText(matcher: Matcher<View?>?): String? {
        val stringHolder = arrayOf<String?>(null)
        Espresso.onView(matcher).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isAssignableFrom(TextView::class.java)
            }

            override fun getDescription(): String {
                return "get text from TextView"
            }

            override fun perform(uiController: UiController, view: View) {
                val tv = view as TextView
                stringHolder[0] = tv.text.toString()
            }
        })
        return stringHolder[0]
    }

    protected fun getSpinnerChoice(id: Int): String {
        onView(withId(id)).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isAssignableFrom(Spinner::class.java)
            }

            override fun getDescription(): String {
                return "get spinner data"
            }

            override fun perform(uiController: UiController?, view: View?) {
                TODO("Not yet implemented")
            }
        })
        return ""
    }

    protected fun onView(
        viewMatcher: Matcher<View>,
        retry: Boolean = true
    ): ViewInteraction {
        return try {
            Espresso.onView(viewMatcher)
        } catch (e: AmbiguousViewMatcherException) {
            if (retry) {
                Log.d(logTag, "AmbiguousViewMatcherException found: retrying after " +
                        "${DELAYS.UI_WAIT_TIME.SHORT}ms.")
                DELAYS.UI_WAIT_TIME.waitShortDuration()
                onView(viewMatcher, false)
            } else {
                throw e
            }
        }
    }
}
