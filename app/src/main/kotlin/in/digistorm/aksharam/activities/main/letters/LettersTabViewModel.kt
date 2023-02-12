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
import `in`.digistorm.aksharam.activities.main.TabbedViewsDirections
import `in`.digistorm.aksharam.util.*

class LettersTabViewModel(application: Application): AndroidViewModel(application) {
    private val logTag = javaClass.simpleName

    private lateinit var aksharamViewModel: AksharamViewModel

    // The actual list of languages currently available to the app
    var downloadedLanguages: MutableLiveData<ArrayList<String>> = MutableLiveData()
        get() {
            if(field.value == null) {
                field.value  = getAllDownloadedLanguages()
            }
            return field
        }

    // A function to initialise the Convert To drop down.
    // This function can access the view which we can't do here in the view model.
    private lateinit var navigateToLanguageInfo: (NavDirections) -> Unit

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

    // The actual language data.
    var language: LiveData<Language> = languageSelected.map { newLanguage ->
        logDebug(logTag, "Fetching data for $newLanguage")
        val language: Language = getLanguage(newLanguage)
        aksharamViewModel.language.value = language
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
                field.value = targetLanguageList.value?.first() ?: "Oops!"
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
        val category = language.value?.getLetterDefinition(letter)?.type!!.replaceFirstChar {
            if(it.isLowerCase())
                it.titlecase()
            else
                it.toString()
        }
        return TabbedViewsDirections.actionTabbedViewsFragmentToLetterInfoFragment(
            letter = letter,
            category = category,
            targetLanguage = targetLanguageSelected.value!!,
        )
    }

    val languageInfoOnClick: OnClickListener = OnClickListener {
        if(targetLanguageSelected.value != null) {
            val directions = TabbedViewsDirections.actionTabbedViewsFragmentToLanguageInfoFragment(
                targetLanguageSelected.value!!
            )
            navigateToLanguageInfo(directions)
        }
    }

    fun initialise(
        activityViewModel: AksharamViewModel,
        navigateToLanguageInfo: (NavDirections) -> Unit,
    ) {
        logDebug(logTag, "Initialising...")
        this.aksharamViewModel = activityViewModel
        this.navigateToLanguageInfo = navigateToLanguageInfo
        logDebug(logTag, "Done initialising.")
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

    // TODO: This exists in PracticeTabViewModel as well. Move to a common utility collection.
    // Start Initialisation activity if we could not find any downloaded languages.
    private fun getAllDownloadedLanguages(): java.util.ArrayList<String> {
        val languages: java.util.ArrayList<String> = getDownloadedLanguages(getApplication())
        if (languages.size == 0) {
            // (requireActivity() as MainActivity).startInitialisationActivity()
            return java.util.ArrayList()
        }
        return languages
    }
}