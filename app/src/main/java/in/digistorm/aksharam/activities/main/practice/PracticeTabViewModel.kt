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
package `in`.digistorm.aksharam.activities.main.practice

import `in`.digistorm.aksharam.util.Transliterator
import `in`.digistorm.aksharam.util.logDebug
import android.content.Context
import androidx.lifecycle.ViewModel

class PracticeTabViewModel : ViewModel() {
    private val logTag = PracticeTabViewModel::class.simpleName

    var transliterator: Transliterator? = null
        private set
    var transLang: String? = null
    var practiceType: String? = null
    fun getTransliterator(context: Context?): Transliterator {
        if (transliterator == null) transliterator = Transliterator(
            context!!
        )
        return transliterator!!
    }

    fun resetTransliterator(context: Context?) {
        if (transliterator == null) {
            logDebug(logTag, "Transliterator is null. Initialising...")
            transliterator = Transliterator(context!!)
        }
    }

    fun setTransliterator(language: String, context: Context?) {
        if (transliterator != null) {
            if (transliterator!!.language!!.language.lowercase() != language.lowercase()) transliterator =
                Transliterator(language, context!!)
        }
        transliterator = Transliterator(language, context!!)
    }

    val language: String
        get() = transliterator!!.language!!.language
}