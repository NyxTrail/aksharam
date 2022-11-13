/*
 * Copyright (c) 2022 Alan M Varghese <alan@digistorm.in>
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
package `in`.digistorm.aksharam.util

import android.content.Context
import java.util.*
import java.util.concurrent.*

class GlobalSettings private constructor(context: Context) {
    private val logTag = javaClass.simpleName

    // Dark/Light mode
    var darkMode: Boolean
        private set

    // Remember, URLs must end in a '/' or Retrofit rebels
    private val numberOfCores = Runtime.getRuntime().availableProcessors()
    private val workQueue: BlockingDeque<Runnable> = LinkedBlockingDeque()

    // max idle time a thread is kept alive
    private val keepAliveTime = 20
    private val keepAliveTimeUnit = TimeUnit.SECONDS
    private val threadFactoryName = "GlobalThreadFactory"
    val threadPoolExecutor: ThreadPoolExecutor = ThreadPoolExecutor(
        numberOfCores,
        numberOfCores,
        keepAliveTime.toLong(),
        keepAliveTimeUnit,
        workQueue
    )

    private inner class SimpleThreadFactory() : ThreadFactory {
        private val name: String = threadFactoryName
        override fun newThread(r: Runnable): Thread {
            return Thread(r, name)
        }

    }

    // We will keep the listeners in a named map so that their owners can
    // replace existing listeners (not so straight forward if this is an ArrayList)
    private val dataFileListChangedListeners: MutableMap<String, DataFileListChanged>

    // this must be called at the point where there is a change in the data file list
    // usually, the Settings activity
    fun invokeDataFileListChangedListeners() {
        for (key in dataFileListChangedListeners.keys) {
            logDebug(logTag, "Invoking $key")
            dataFileListChangedListeners[key]?.onDataFileListChanged()
        }
    }

    fun addDataFileListChangedListener(name: String, l: DataFileListChanged) {
        dataFileListChangedListeners[name] = l
    }

    fun clearDataFileListChangedListeners() {
        dataFileListChangedListeners.clear()
    }

    fun setDarkMode(v: Boolean, context: Context) {
        darkMode = v
        context.getSharedPreferences("dark_mode", Context.MODE_PRIVATE).edit()
            .putBoolean("dark_mode", v).apply()
    }

    companion object {
        /* TODO: how to avoid null checks on GlobalSettings */
        var instance: GlobalSettings? = null
            private set

        // this method should be called only once in the entire project
        fun createInstance(context: Context): GlobalSettings? {
            instance = GlobalSettings(context)
            return instance
        }
    }

    init {
        threadPoolExecutor.threadFactory = SimpleThreadFactory()
        dataFileListChangedListeners = HashMap()
        darkMode = context.getSharedPreferences("dark_mode", Context.MODE_PRIVATE)
            .getBoolean("dark_mode", false)
    }
}