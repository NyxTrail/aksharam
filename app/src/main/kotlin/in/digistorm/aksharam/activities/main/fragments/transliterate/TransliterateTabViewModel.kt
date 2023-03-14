package `in`.digistorm.aksharam.activities.main.fragments.transliterate

import `in`.digistorm.aksharam.activities.main.language.Language
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import `in`.digistorm.aksharam.activities.main.language.getDownloadedLanguages
import `in`.digistorm.aksharam.activities.main.language.getLanguageData
import `in`.digistorm.aksharam.activities.main.util.logDebug

// View model for the 'Transliterate' tab fragment
class TransliterateTabViewModel(
    application: Application,
): AndroidViewModel(application) {
    private val logTag = javaClass.simpleName

    val detectedLanguage: MutableLiveData<String> = MutableLiveData("")

    val downloadedLanguages: MutableLiveData<ArrayList<String>> = MutableLiveData(arrayListOf())

    val language: LiveData<Language> = detectedLanguage.map { newLanguage ->
        logDebug(logTag, "Fetching data for $newLanguage")
        getLanguageData(newLanguage, getApplication())
    }

    val selectableLanguages: LiveData<ArrayList<String>> = detectedLanguage.map { detectedLanguage ->
        val tempList = arrayListOf<String>()
        downloadedLanguages.value?.forEach { language ->
            if(language != detectedLanguage)
                tempList.add(language)
        }
        targetLanguageSelected.value = tempList.first()
        tempList
    }

    /*
       The target language to transliterate a string to. This is set by a spinner in the
       Fragment.
     */
    var targetLanguageSelected: MutableLiveData<String> = MutableLiveData()

    fun initialise() {
        logDebug(logTag, "Initialising.")
        downloadedLanguages.value = getDownloadedLanguages(getApplication())
    }
}
