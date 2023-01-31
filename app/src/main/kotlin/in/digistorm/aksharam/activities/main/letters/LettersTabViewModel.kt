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

import `in`.digistorm.aksharam.activities.main.models.AksharamViewModel
import `in`.digistorm.aksharam.util.Transliterator
import `in`.digistorm.aksharam.util.Language
import `in`.digistorm.aksharam.util.getLanguageData
import `in`.digistorm.aksharam.util.logDebug
import android.app.Application

import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LettersTabViewModel: ViewModel() {
    private val logTag = javaClass.simpleName

    // var transliterator: Transliterator? = null
    // private set

    var _languageSelected: MutableLiveData<String> = MutableLiveData()
        private set
    var languageSelected: String
        get() {
            return _languageSelected.value!!
        }
        set(value) {
            logDebug(logTag, "Language live data set to value: $value")
            _languageSelected.value = value
        }


    private var _language: MutableLiveData<Language> = MutableLiveData()

    fun getLanguage(): Language {
        return _language.value!!
    }

    fun setLanguage(language: Language, aksharamViewModel: AksharamViewModel) {
        _language.value = language
        aksharamViewModel.language.value = language
    }

    fun setLanguage(fileName: String, context: Context, aksharamViewModel: AksharamViewModel) {
        val language: Language = getLanguageData(fileName, context)!!
        _language.value = language
        aksharamViewModel.language.value = language
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
}