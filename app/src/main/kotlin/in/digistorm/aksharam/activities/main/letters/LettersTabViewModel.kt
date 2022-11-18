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
import `in`.digistorm.aksharam.util.LabelledArrayAdapter
import `in`.digistorm.aksharam.util.Language
import android.app.Application

import android.content.Context
import androidx.lifecycle.AndroidViewModel

class LettersTabViewModel(application: Application): AndroidViewModel(application) {
    private val logTag = LettersTabViewModel::class.simpleName

    // The target language string as displayed by lettersTabTransSpinner
    var targetLanguage: String = ""
    var adapter: LabelledArrayAdapter<String>? = null
    var transliterator: Transliterator = Transliterator(application)

    // Set the transliterator based on a specific language
    fun setTransliterator(language: String, context: Context) {
        if (transliterator.getLanguage().lowercase() != language.lowercase())
            transliterator = Transliterator(language, context)
    }

    fun getLanguage(): String {
        return transliterator.languageData.language
    }

    // A convenience method to obtain language data
    fun getLanguageData(): Language {
        return transliterator.languageData
    }
}