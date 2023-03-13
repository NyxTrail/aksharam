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

import android.app.Application
import android.view.View.OnClickListener
import `in`.digistorm.aksharam.activities.main.models.AksharamViewModel

import androidx.lifecycle.*
import androidx.navigation.NavDirections
import `in`.digistorm.aksharam.activities.main.TabbedViewsFragmentDirections
import `in`.digistorm.aksharam.util.*

class LettersTabViewModel(
    application: Application,
    // We persist some state information in the activityViewModel for Fragments reachable from LettersTabFragment
    val activityViewModel: AksharamViewModel
): AndroidViewModel(application) {
    private val logTag = javaClass.simpleName

    // The actual list of languages currently available to the app
    var downloadedLanguages: MutableLiveData<ArrayList<String>> = MutableLiveData(arrayListOf())

    // A function to initialise the Convert To drop down.
    // This function can access the view which we can't do here in the view model.
    private lateinit var navigateToLanguageInfo: (NavDirections) -> Unit

    // The currently selected language in the UI
    var languageSelected: MutableLiveData<String> = MutableLiveData()
        get() {
            logDebug(logTag, "languageSelected")
            if(field.value == null) {
                if((downloadedLanguages.value?.size ?: 0) > 0)
                    field.value = downloadedLanguages.value?.first()
            }
            return field
        }
        private set

    // The actual language data.
    var language: LiveData<Language> = languageSelected.map { newLanguage ->
        logDebug(logTag, "Fetching data for $newLanguage")
        val language: Language = getLanguage(newLanguage)
        activityViewModel.language.value = language
        language
    }

    // The list of languages shown in the Convert To drop down.
    var targetLanguageList: LiveData<ArrayList<String>> = language.map { language ->
        logDebug(logTag, "Transforming \"${language.language}\" to a live data of target languages")
        val targetLanguages = language.supportedLanguagesForTransliteration
        targetLanguageSelected.value = targetLanguages.first()
        targetLanguages
    }

    var targetLanguageSelected: MutableLiveData<String> = MutableLiveData()
        get() {
            if(field.value == null) {
                logDebug(logTag, "targetLanguageSelected backing field is null")
                field.value = targetLanguageList.value?.first()
                if(field.value == null)
                    logDebug(logTag, "targetLanguageSelected is still null!")
            }
            return field
        }

    val lettersCategoryWise: LiveData<List<Map<String, ArrayList<Pair<String, String>>>>> = targetLanguageSelected.map { newLanguage ->
        logDebug(logTag, "Generating letters category wise for language: ${language.value?.language}")
        logDebug(logTag, "Conversion language is: $newLanguage")
        val categories = mutableListOf<Map<String, ArrayList<Pair<String, String>>>>()
        // [{"vowels": ["a", "e",...]}, {"consonants":: ["b", "c", "d",...]}, ...]
        language.value?.lettersCategoryWise?.forEach { (category, letters) ->
            val transliteratedLetterPairs: ArrayList<Pair<String, String>> = ArrayList()
            letters.forEach { letter ->
                transliteratedLetterPairs.add(letter to transliterate(letter, newLanguage, language.value!!))
            }
            categories.add(mapOf(category to transliteratedLetterPairs))
        }
        logDebug(logTag, "Category list created: $categories")
        categories
    }

    var categoryListAdapter: LetterCategoryAdapter = LetterCategoryAdapter(::letterOnLongClickAction)

    private fun letterOnLongClickAction(letter: String): NavDirections {
        logDebug(logTag, "$letter long clicked.")
        val category = language.value?.getLetterDefinition(letter)?.type!!.replaceFirstChar {
            if(it.isLowerCase())
                it.titlecase()
            else
                it.toString()
        }
        return TabbedViewsFragmentDirections.actionTabbedViewsFragmentToLetterInfoFragment(
            letter = letter,
            category = category,
            targetLanguage = targetLanguageSelected.value!!,
        )
    }

    val languageInfoOnClick: OnClickListener = OnClickListener {
        if(targetLanguageSelected.value != null) {
            val directions = TabbedViewsFragmentDirections.actionTabbedViewsFragmentToLanguageInfoFragment(
                targetLanguageSelected.value!!
            )
            navigateToLanguageInfo(directions)
        }
    }

    fun initialise(
        navigateToLanguageInfo: (NavDirections) -> Unit,
    ) {
        logDebug(logTag, "Initialising.")
        this.navigateToLanguageInfo = navigateToLanguageInfo
        downloadedLanguages.value = getDownloadedLanguages(getApplication())
        if(downloadedLanguages.value?.contains(languageSelected.value) != true) {
            if(downloadedLanguages.value?.isNotEmpty() == true) {
                languageSelected.value = downloadedLanguages.value?.first()
            }
        }
    }

    private fun getLanguage(file: String): Language {
        val languageData: Language? = getLanguageData(file, getApplication())
        if(languageData != null)
            return languageData
        else {
            // TODO: How to handle this?
            logDebug(logTag, "Null encounter while trying to load language: \"$file\"")
            return languageData!!  // Dummy return which should just throw a NullPointer Exception
        }
    }

    init {
        logDebug(logTag, "aksharamViewModel available languages: ${downloadedLanguages.value}")
        logDebug(logTag, "downloaded languages: ${downloadedLanguages.value}")
    }
}