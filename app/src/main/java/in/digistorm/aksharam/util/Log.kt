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

import `in`.digistorm.aksharam.BuildConfig
import android.util.Log

private val LOG = BuildConfig.DEBUG

fun logInfo(tag: String?, string: String?) {
    if (LOG) Log.i(tag, string!!)
}

fun logError(tag: String?, string: String?) {
    if (LOG) Log.e(tag, string!!)
}

fun logDebug(tag: String?, string: String?) {
    if (LOG) Log.d(tag, string!!)
}

fun logVerbose(tag: String?, string: String?) {
    if (LOG) Log.v(tag, string!!)
}

fun logWarn(tag: String?, string: String?) {
    if (LOG) Log.w(tag, string!!)
}