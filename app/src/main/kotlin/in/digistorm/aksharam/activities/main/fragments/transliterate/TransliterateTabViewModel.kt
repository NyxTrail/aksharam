/*
 * Copyright (c) 2023-2025 Alan M Varghese <alan@digistorm.in>
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
package `in`.digistorm.aksharam.activities.main.fragments.transliterate

import android.app.Application
import androidx.lifecycle.*
import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.activities.main.language.*
import `in`.digistorm.aksharam.activities.main.util.logDebug
import `in`.digistorm.aksharam.activities.main.language.transliterate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// View model for the 'Transliterate' tab fragment
class TransliterateTabViewModel(
    application: Application,
): AndroidViewModel(application) {
    private val logTag = javaClass.simpleName

    private var languageDetector: LanguageDetector? = null

    val currentInput: MutableLiveData<String> = MutableLiveData()

    val language: LiveData<Language?> = currentInput.switchMap {
        liveData(Dispatchers.IO) {
            if (languageDetector == null)
                languageDetector = LanguageDetector(application)
            emit(languageDetector!!.detectLanguage(it))
        }
    }

    val selectableLanguages: LiveData<List<String>> = language.switchMap { language ->
        liveData(Dispatchers.IO) {
            emit(language?.supportedLanguagesForTransliteration ?: listOf())
        }
    }

    /*
       The target language to transliterate a string to. This is set by a spinner in the
       Fragment.
     */
    val targetLanguageSelected: MutableLiveData<String> = MutableLiveData()

    val transliteratedString: MutableLiveData<String?> = MediatorLiveData<String>().apply {
        addSource(language) { language ->
            viewModelScope.launch(Dispatchers.IO) {
                logDebug(logTag, "Change detected in language data. Generating transliterated string.")
                if (language != null && targetLanguageSelected.value != null)
                    postValue(transliterate(currentInput.value!!, targetLanguageSelected.value!!, language))
                else if (language == null)
                    postValue("")
            }
        }

        addSource(targetLanguageSelected) { newLanguageSelected ->
            logDebug(logTag, "Change detected in selected target language. Generating transliterated string.")
            currentInput.value?.let { currentInput ->
                language.value?.let { languageValue ->
                    viewModelScope.launch(Dispatchers.IO) {
                        postValue(transliterate(currentInput, newLanguageSelected, languageValue))
                    }
                }
            }
        }
    }

    fun resetLanguageDetector() {
        languageDetector = null
    }
}
