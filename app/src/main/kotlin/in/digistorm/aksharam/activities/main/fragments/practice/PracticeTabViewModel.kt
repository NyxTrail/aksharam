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
package `in`.digistorm.aksharam.activities.main.fragments.practice

import android.app.Application
import android.widget.ArrayAdapter
import androidx.lifecycle.*
import `in`.digistorm.aksharam.activities.main.language.Language
import `in`.digistorm.aksharam.activities.main.language.getDownloadedLanguages
import `in`.digistorm.aksharam.activities.main.language.getLanguageData
import `in`.digistorm.aksharam.activities.main.util.logDebug
import `in`.digistorm.aksharam.util.*
import java.util.*
import kotlin.collections.ArrayList

class PracticeTabViewModel(
    application: Application,
): AndroidViewModel(application) {
    private val logTag = javaClass.simpleName

    // The actual list of languages currently available to the app
    var downloadedLanguages: MutableLiveData<ArrayList<String>> = MutableLiveData(arrayListOf())

    // Adapter containing the list of languages displayed in the UI
    lateinit var languageAdapter: ArrayAdapter<String>
    // The currently selected language in the UI
    var languageSelected: MutableLiveData<String> = MutableLiveData()
        get() {
            if(field.value == null) {
                if((downloadedLanguages.value?.size ?: 0) > 0)
                    field.value = downloadedLanguages.value?.first()
            }
            return field
        }
        private set

    // The actual language data
    var language: LiveData<Language> = languageSelected.map { newLanguage ->
        logDebug(logTag, "Fetching data for $newLanguage")
        val language: Language = getLanguageData(newLanguage, getApplication())
        language
    }

    var practiceInLanguages: LiveData<ArrayList<String>> = language.map { language ->
        logDebug(logTag, "Transforming \"${language.language}\" to a live data of target languages")
        val targetList = language.supportedLanguagesForTransliteration
        practiceInSelected.value = targetList.first()
        targetList
    }
    lateinit var practiceInAdapter: ArrayAdapter<String>
    var practiceInSelected: MutableLiveData<String> = MutableLiveData()

    var practiceTypes: LiveData<ArrayList<String>> = language.map { language ->
        val types = arrayListOf<String>()
        val lettersCategoryWise = language.lettersCategoryWise
        for(category in lettersCategoryWise.keys) {
            types.add(category.replaceFirstChar { char ->
                if(char.isLowerCase())
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
        if(language.areLigaturesAutoGeneratable())
            types.add("Random Ligatures")
        types.add("Random Words")

        practiceTypeSelected.value = types.first()

        types
    }
    lateinit var practiceTypesAdapter: ArrayAdapter<String>
    var practiceTypeSelected: MutableLiveData<String> = MutableLiveData()
    get() {
        if(field.value == null) {
            if((practiceTypes.value?.size ?: 0) > 0)
                field.value = practiceTypes.value?.first()
        }
        return field
    }

    var practiceString: LiveData<String> = practiceTypeSelected.map { practiceType ->
        logDebug(logTag, "Practice string changes...")
        val _practiceString = generatePracticeString(this)
        transliteratedString.value = transliterate(_practiceString, practiceInSelected.value!!, language.value!!)
        practiceSuccessCheck.value = false
        _practiceString
    }
    var transliteratedString: MutableLiveData<String> = MutableLiveData()

    fun generateNewPracticeString() {
        practiceTypeSelected.postValue(practiceTypeSelected.value)
    }

    // Variable is true if user's transliteration is correct
    var practiceSuccessCheck: MutableLiveData<Boolean> = MutableLiveData(false)

    fun initialise() {
        logDebug(logTag, "Initialising.")
        downloadedLanguages.value = getDownloadedLanguages(getApplication())
        if(downloadedLanguages.value?.contains(languageSelected.value) != true) {
            if(downloadedLanguages.value?.isNotEmpty() == true) {
                languageSelected.value = downloadedLanguages.value?.first()
            }
        }
    }
}
