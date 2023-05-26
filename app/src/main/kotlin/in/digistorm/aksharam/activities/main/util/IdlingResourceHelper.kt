package `in`.digistorm.aksharam.activities.main.util

import androidx.test.espresso.idling.CountingIdlingResource

class IdlingResourceHelper {
    companion object {
        val countingIdlingResource = CountingIdlingResource("DownloadTracker")
    }
}