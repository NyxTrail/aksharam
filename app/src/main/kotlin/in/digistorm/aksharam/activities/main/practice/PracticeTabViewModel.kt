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

import `in`.digistorm.aksharam.util.Language
import `in`.digistorm.aksharam.util.Transliterator
import `in`.digistorm.aksharam.util.logDebug
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel

class PracticeTabViewModel(application: Application) : AndroidViewModel(application) {
    private val logTag = this.javaClass.simpleName

    var transliterator: Transliterator = Transliterator(application)
    var transLang: String = ""
    var practiceType: String = ""
    var practiceString: String = ""
    var transliteratedString: String = ""

    fun setTransliterator(language: String, context: Context) {
        if (transliterator.languageData.language.lowercase() != language.lowercase())
            transliterator = Transliterator(language, context)
    }

    fun getLanguage(): String {
        return transliterator.languageData.language
    }

    fun getLanguageData(): Language {
        return transliterator.languageData
    }
}