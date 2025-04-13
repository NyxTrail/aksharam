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
package `in`.digistorm.aksharam.activities.main.fragments.letters

import android.app.Application
import android.view.View.OnClickListener
import `in`.digistorm.aksharam.activities.main.ActivityViewModel

import androidx.lifecycle.*
import androidx.navigation.NavDirections
import `in`.digistorm.aksharam.activities.main.language.Category
import `in`.digistorm.aksharam.activities.main.language.Language
import `in`.digistorm.aksharam.activities.main.language.TransliteratedLetters
import `in`.digistorm.aksharam.activities.main.language.getDownloadedLanguages
import `in`.digistorm.aksharam.activities.main.language.getLanguageData
import `in`.digistorm.aksharam.activities.main.language.transliterate
import `in`.digistorm.aksharam.activities.main.util.CheckedMutableLiveData
import `in`.digistorm.aksharam.activities.main.util.logDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LettersViewModel(
    application: Application,
    // We persist some state information in the activityViewModel for Fragments reachable
    // from LettersTabFragment
    val activityViewModel: ActivityViewModel
): AndroidViewModel(application) {
    private val logTag = javaClass.simpleName

    // The actual list of languages currently available to the app
    val downloadedLanguages: CheckedMutableLiveData<ArrayList<String>> = CheckedMutableLiveData(arrayListOf())

    // A function to initialise the Convert To drop down.
    // This function can access the view which we can't do here in the view model.
    private lateinit var navigateToLanguageInfo: (NavDirections) -> Unit

    // The currently selected language in the UI
    val languageSelected: CheckedMutableLiveData<String> = CheckedMutableLiveData()

    // The actual language data.
    val language: LiveData<Language?> = languageSelected.switchMap { newLanguage ->
        liveData(Dispatchers.Default) {
            newLanguage?.let{
                logDebug(logTag, "Fetching data for $newLanguage")
                val language: Language? = getLanguageData(newLanguage, getApplication())
                activityViewModel.language.postValue(language)
                emit(language)
            }
        }
    }

    // The list of languages shown in the Convert To drop down.
    val targetLanguageList: LiveData<ArrayList<String>> = language.switchMap { language ->
        liveData(Dispatchers.Default) {
            logDebug(logTag, "Transforming \"${language?.language}\" to a live data of target languages")
            val targetLanguages = language?.supportedLanguagesForTransliteration ?: arrayListOf()
            targetLanguageSelected.postValueWithTrigger(targetLanguages.firstOrNull() ?: "")
            logDebug(logTag, "Selected target language: ${targetLanguageSelected.value}")
            emit(targetLanguages)
        }
    }
    val targetLanguageSelected: CheckedMutableLiveData<String> = CheckedMutableLiveData()

    val lettersCategoryWise: LiveData<TransliteratedLetters> = MediatorLiveData<TransliteratedLetters>().also {
        it.addSource(targetLanguageSelected) { newLanguage ->
            CoroutineScope(Dispatchers.Default).launch {
                languageSelected.value?.let { languageSelected ->
                    targetLanguageSelected.value?.let { targetLanguageSelected ->
                        if((it.value?.sourceLanguage != languageSelected.lowercase()) ||
                            (it.value?.targetLanguage != targetLanguageSelected.lowercase())) {
                                logDebug(
                                    logTag,
                                    "Generating letters category wise for language: ${language.value?.language}"
                                )
                                logDebug(logTag, "Conversion language is: $newLanguage")
                                val categories = ArrayList<Category>()
                                val transliteratedLetters = TransliteratedLetters(
                                    sourceLanguage = languageSelected.lowercase(),
                                    targetLanguage = targetLanguageSelected.lowercase(),
                                    categories = categories
                                )
                                language.value?.lettersCategoryWise?.forEach { (iCategory, letters) ->
                                    val transliteratedLetterPairs: ArrayList<Pair<String, String>> = ArrayList()
                                    letters.forEach { letter ->
                                        transliteratedLetterPairs.add(
                                            letter to transliterate(
                                                letter,
                                                newLanguage,
                                                language.value!!
                                            )
                                        )
                                    }
                                    val category = Category(
                                        name = iCategory,
                                        letterPairs = transliteratedLetterPairs
                                    )
                                    categories.add(category)
                                }
                                logDebug(logTag, "Category list created: $transliteratedLetters")
                                it.postValue(transliteratedLetters)
                            }
                    }
                }
            }
        }
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
        return LettersFragmentDirections.actionLettersFragmentToLetterInfoFragment(
            letter = letter,
            category = category,
            targetLanguage = targetLanguageSelected.value!!,
        )
    }

    val languageInfoOnClick: OnClickListener = OnClickListener {
        if(targetLanguageSelected.value != null) {
            val directions = LettersFragmentDirections.actionLettersFragmentToLanguageInfoFragment(
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

        CoroutineScope(Dispatchers.Default).launch {
            val mDownloadedLanguages = getDownloadedLanguages(getApplication())

            // If there is a change in downloaded languages...
            if (mDownloadedLanguages != downloadedLanguages.value && mDownloadedLanguages.size > 0) {
                downloadedLanguages.postValue(mDownloadedLanguages)
                logDebug(logTag, "Downloaded languages set to: $mDownloadedLanguages")

                // If currently selected language is no longer available, update the view model with
                // the first available language
                if (!mDownloadedLanguages.contains(languageSelected.value)) {

                    mDownloadedLanguages.firstOrNull()?.let {
                        languageSelected.postValue(it)
                    }
                }
            }
        }
        logDebug(logTag, "Coroutine launched to find downloaded languages.")
    }
}
