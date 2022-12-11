package `in`.digistorm.aksharam

import android.view.View
import android.widget.Spinner
import android.widget.TextView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.hamcrest.Matcher

open class AksharamTestBaseExp {
    protected val logTag: String = javaClass.simpleName

    protected fun upperCaseFirstLetter(s: String): String? {
        return s.substring(0, 1).uppercase() + s.substring(1)
    }

    /* Obtain text from a text view.
       https://stackoverflow.com/a/23467629
     */
    protected fun getText(matcher: Matcher<View?>?): String? {
        val stringHolder = arrayOf<String?>(null)
        onView(matcher).perform(object : ViewAction {
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
}
