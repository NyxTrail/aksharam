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
package `in`.digistorm.aksharam.activities.main.fragments.letters

import android.app.Application
import android.view.View.OnClickListener
import `in`.digistorm.aksharam.activities.main.ActivityViewModel

import androidx.lifecycle.*
import androidx.navigation.NavDirections
import `in`.digistorm.aksharam.activities.main.fragments.TabbedViewsFragmentDirections
import `in`.digistorm.aksharam.activities.main.language.Language
import `in`.digistorm.aksharam.activities.main.language.getDownloadedLanguages
import `in`.digistorm.aksharam.activities.main.language.getLanguageData
import `in`.digistorm.aksharam.activities.main.util.CheckedMutableLiveData
import `in`.digistorm.aksharam.activities.main.util.logDebug
import `in`.digistorm.aksharam.util.*

class LettersTabViewModel(
    application: Application,
    // We persist some state information in the activityViewModel for Fragments reachable
    // from LettersTabFragment
    val activityViewModel: ActivityViewModel
): AndroidViewModel(application) {
    private val logTag = javaClass.simpleName

    // The actual list of languages currently available to the app
    var downloadedLanguages: CheckedMutableLiveData<ArrayList<String>> = CheckedMutableLiveData(arrayListOf())

    // A function to initialise the Convert To drop down.
    // This function can access the view which we can't do here in the view model.
    private lateinit var navigateToLanguageInfo: (NavDirections) -> Unit

    // The currently selected language in the UI
    var languageSelected: CheckedMutableLiveData<String> = CheckedMutableLiveData()
        private set

    // The actual language data.
    var language: LiveData<Language> = languageSelected.map { newLanguage ->
        logDebug(logTag, "Fetching data for $newLanguage")
        val language: Language = getLanguageData(newLanguage, getApplication())!!
        activityViewModel.language.value = language
        language
    }

    // The list of languages shown in the Convert To drop down.
    var targetLanguageList: LiveData<ArrayList<String>> = language.map { language ->
        logDebug(logTag, "Transforming \"${language.language}\" to a live data of target languages")
        val targetLanguages = language.supportedLanguagesForTransliteration
        targetLanguageSelected.value = targetLanguages.first()
        logDebug(logTag, "Selected target language: ${targetLanguageSelected.value}")
        targetLanguages
    }
    var targetLanguageSelected: CheckedMutableLiveData<String> = CheckedMutableLiveData()

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
                languageSelected.value!!,
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

        val mDownloadedLanguages = getDownloadedLanguages(getApplication())

        // If there is a change in downloaded languages...
        if(mDownloadedLanguages != downloadedLanguages.value) {
            downloadedLanguages.value = mDownloadedLanguages
            logDebug(logTag, "Downloaded languages set to: ${downloadedLanguages.value}")

            // If currently selected language is no longer available, update the view model with
            // the first available language
            if (downloadedLanguages.value?.contains(languageSelected.value) != true) {
                if (downloadedLanguages.value?.isNotEmpty() == true) {
                    languageSelected.setValueIfDifferent(downloadedLanguages.value!!.first())
                }
            }
        }
    }
}