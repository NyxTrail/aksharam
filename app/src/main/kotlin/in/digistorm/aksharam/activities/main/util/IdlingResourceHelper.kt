package `in`.digistorm.aksharam.activities.main.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataScope
import androidx.lifecycle.liveData
import androidx.test.espresso.idling.CountingIdlingResource
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class IdlingResourceHelper {
    companion object {
        val countingIdlingResource = CountingIdlingResource("BackgroundCoroutineTracker")
    }
}

fun <T> trackedLiveData(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend LiveDataScope<T>.() -> Unit
): LiveData<T> {
    IdlingResourceHelper.countingIdlingResource.increment()
    try{
        return liveData(context) {
            block()
        }
    } finally {
        IdlingResourceHelper.countingIdlingResource.decrement()
    }
}
