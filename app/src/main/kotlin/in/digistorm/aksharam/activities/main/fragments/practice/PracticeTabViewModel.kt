/*
 * Copyright (c) 2022-2025 Alan M Varghese <alan@digistorm.in>
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
package `in`.digistorm.aksharam.activities.main.fragments.practice

import android.app.Application
import androidx.lifecycle.*
import `in`.digistorm.aksharam.activities.main.language.Language
import `in`.digistorm.aksharam.activities.main.language.getDownloadedLanguages
import `in`.digistorm.aksharam.activities.main.language.getLanguageData
import `in`.digistorm.aksharam.activities.main.language.transliterate
import `in`.digistorm.aksharam.activities.main.util.CheckedMutableLiveData
import `in`.digistorm.aksharam.activities.main.util.logDebug
import kotlinx.coroutines.Dispatchers
import java.util.*
import kotlin.collections.ArrayList

class PracticeTabViewModel(
    application: Application,
): AndroidViewModel(application) {
    private val logTag = javaClass.simpleName

    // The actual list of languages currently available to the app
    val downloadedLanguages: CheckedMutableLiveData<ArrayList<String>> = CheckedMutableLiveData(arrayListOf())

    // The currently selected language in the UI
    val languageSelected: CheckedMutableLiveData<String> = CheckedMutableLiveData()

    // The actual language data
    val language: LiveData<Language?> = languageSelected.switchMap { newLanguage ->
        liveData(Dispatchers.Default) {
            logDebug(logTag, "Fetching data for $newLanguage")
            getLanguageData(newLanguage, getApplication())?.let {
                emit(it)
            }
        }
    }

    val practiceInLanguages: LiveData<ArrayList<String>> = language.switchMap { language ->
        liveData(Dispatchers.Default) {
            logDebug(logTag, "Transforming \"${language?.language}\" to a live data of target languages")
            language?.supportedLanguagesForTransliteration?.let {
                practiceInSelected.postValue(it.firstOrNull() ?: "")
                logDebug(logTag, "Practice In language selected: ${practiceInSelected.value}")
                emit(it)
            }
        }
    }
    val practiceInSelected: CheckedMutableLiveData<String> = CheckedMutableLiveData()

    val practiceTypes: LiveData<ArrayList<String>> = language.switchMap { language ->
        liveData(Dispatchers.Default) {
            val types = arrayListOf<String>()
            language?.lettersCategoryWise?.let { lettersCategoryWise ->
                for (category in lettersCategoryWise.keys) {
                    types.add(category.replaceFirstChar { char ->
                        if (char.isLowerCase())
                            char.titlecase(Locale.getDefault())
                        else
                            char.toString()
                    })
                }
                // Additional practice types
                // Random ligatures work best in some languages like Kannada where each consonant can form
                // a unique conjunct with another consonant. Other languages like Malayalam or Hindi
                // have a few ligatures, yet this is true only for commonly occurring consonant combinations
                // Most of the combinations in these languages do not result in a meaningful ligature, or are
                // easily understood (as in the case of Hindi). So, we will add random ligatures only if the
                // language's data file says we should.
                if (language.areLigaturesAutoGeneratable())
                    types.add("Random Ligatures")
                types.add("Random Words")

                logDebug(logTag, "Generated practice types for ${languageSelected.value}: $types")

                practiceTypeSelected.postValueWithTrigger(value = types.firstOrNull() ?: "")
                logDebug(logTag, "Practice Type selected: ${practiceTypeSelected.value}")
                emit(types)
            }
        }
    }
    val practiceTypeSelected = CheckedMutableLiveData<String>()

    val practiceString: LiveData<String> = practiceTypeSelected.switchMap { practiceType ->
        liveData(Dispatchers.Default) {
            language.value?.let { language ->
                with(generatePracticeString(language, practiceType)) {
                    transliteratedString.postValue(transliterate(this, practiceInSelected.value!!, language))
                    practiceSuccessCheck.postValue(false)
                    logDebug(logTag, "Practice string changed to: $this")
                    logDebug(logTag, "Transliterated string: ${transliteratedString.value}")
                    emit(this)
                }
            }
        }
    }
    val transliteratedString: MutableLiveData<String> = MutableLiveData()

    fun generateNewPracticeString() {
        practiceTypeSelected.trigger()
    }

    // Variable is true if user's transliteration is correct
    val practiceSuccessCheck: MutableLiveData<Boolean> = MutableLiveData(false)

    fun initialise() {
        logDebug(logTag, "Initialising.")

        val mDownloadedLanguages = getDownloadedLanguages(getApplication())

        if(mDownloadedLanguages != downloadedLanguages.value) {
            downloadedLanguages.setValueIfDifferent(mDownloadedLanguages)
            logDebug(logTag, "Downloaded languages set to: ${downloadedLanguages.value}")

            // If currently selected language is no longer available, update the view model with
            // the first available language
            if(!mDownloadedLanguages.contains(languageSelected.value)) {
                mDownloadedLanguages.firstOrNull()?.let {
                    languageSelected.postValue(it)
                }
            }
        }
    }
}
