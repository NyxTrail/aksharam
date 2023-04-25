package `in`.digistorm.aksharam.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun <T> LiveData<T>.waitAndGetValue(): T {
    val delayTime: Long = 1 * 1000
    val waitTime: Long = 2 * delayTime

    var mValue: T? = null

    val observer: Observer<T> = object : Observer<T> {
        val self: Observer<T> = this

        override fun onChanged(value: T) {
            mValue = value
            runBlocking {
                delay(delayTime)
                this@waitAndGetValue.removeObserver(self)
            }
        }
    }

    observeForever(observer)
    runBlocking {
        delay(waitTime)
    }

    @Suppress("UNCHECKED_CAST")
    return mValue as T
}