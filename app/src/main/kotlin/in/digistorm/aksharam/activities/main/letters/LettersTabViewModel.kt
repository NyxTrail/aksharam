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
package `in`.digistorm.aksharam.activities.main.letters

import `in`.digistorm.aksharam.util.Transliterator
import `in`.digistorm.aksharam.util.Language
import `in`.digistorm.aksharam.util.logDebug
import android.app.Application

import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class LettersTabViewModel(application: Application): AndroidViewModel(application) {
    private val logTag = javaClass.simpleName

    var transliterator: Transliterator? = null

    // The current language displayed in letters tab
    var languageLiveData: MutableLiveData<String> = MutableLiveData()
    var language: String
        get() {
            return languageLiveData.value!!
        }
        set(value) {
            logDebug(logTag, "Language live data set to value: $value")
            languageLiveData.value = value
        }

    // The target language string as displayed by lettersTabTransSpinner
    var targetLanguageLiveData: MutableLiveData<String> = MutableLiveData()
    var targetLanguage: String
        get() {
            return targetLanguageLiveData.value!!
        }
        set(value) {
            targetLanguageLiveData.value = value
        }

    // Set the transliterator based on a specific language
    fun setTransliterator(language: String, context: Context) {
        if (transliterator!!.getLanguage().lowercase() != language.lowercase())
            transliterator = Transliterator(language, context)
    }

    // A convenience method to obtain language data
    fun getLanguageData(): Language {
        return transliterator!!.languageData
    }
}
