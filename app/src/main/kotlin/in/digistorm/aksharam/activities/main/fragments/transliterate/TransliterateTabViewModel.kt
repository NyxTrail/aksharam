package `in`.digistorm.aksharam.activities.main.fragments.transliterate

import android.app.Application
import androidx.lifecycle.*
import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.activities.main.language.*
import `in`.digistorm.aksharam.activities.main.util.logDebug
import `in`.digistorm.aksharam.util.transliterate

// View model for the 'Transliterate' tab fragment
class TransliterateTabViewModel(
    application: Application,
): AndroidViewModel(application) {
    private val logTag = javaClass.simpleName

    private val downloadedLanguages: MutableLiveData<ArrayList<String>> = MutableLiveData(arrayListOf())

    val currentInput: MutableLiveData<String> = MutableLiveData()

    val detectedLanguage: MutableLiveData<String?> = MutableLiveData()

    var language: Language? = null
    get() {
        if(field == null || field?.language?.firstCharCapitalised() != detectedLanguage.value) {
            field = detectedLanguage.value?.let {
                getLanguageData(it, getApplication())
            }
        }
        return field
    }

    val selectableLanguages: MutableLiveData<ArrayList<String>> = MutableLiveData()

    private fun String.firstCharCapitalised(): String {
        return replaceFirstChar {
            if(it.isLowerCase())
                it.uppercase()
            else
                it.toString()
        }
    }

    private fun setSelectableLanguages() {
        val tempList = arrayListOf<String>()
        if(selectableLanguages.value?.contains(detectedLanguage.value) != false) {
            downloadedLanguages.value?.forEach { language ->
                if (language != detectedLanguage.value)
                    tempList.add(language)
            } ?: logDebug(logTag, "Downloaded languages was not populated. How did this happen?")
            targetLanguageSelected.value = tempList.getOrNull(0)
            logDebug(logTag, "Languages enabled for transliteration: $tempList")
            logDebug(logTag, "Target Language selected: ${targetLanguageSelected.value}")
            selectableLanguages.value = tempList
        }
    }

    /*
       The target language to transliterate a string to. This is set by a spinner in the
       Fragment.
     */
    var targetLanguageSelected: MutableLiveData<String> = MutableLiveData()

    var transliteratedString: MutableLiveData<String?> = MediatorLiveData<String>().apply {
        addSource(currentInput) { currentInput ->
            detectedLanguage.value = LanguageDetector(application).detectLanguage(currentInput)
            logDebug(logTag, "Detected ${detectedLanguage.value} in $currentInput.")
            if(detectedLanguage.value == null) {
                value = this@TransliterateTabViewModel.getApplication<Application>().getString(R.string.lang_could_not_detect)
            }
            setSelectableLanguages()
            targetLanguageSelected.value?.let { targetLanguage ->
                language?.let { language ->
                    value = transliterate(currentInput, targetLanguage, language)
                }
            }
        }

        addSource(targetLanguageSelected) { newLanguageSelected ->
            currentInput.value?.let { currentInput ->
                language?.let { language ->
                    value = transliterate(currentInput, newLanguageSelected, language)
                }
            }
        }
    }

    fun initialise() {
        logDebug(logTag, "Initialising.")
        downloadedLanguages.value = getDownloadedLanguages(getApplication())
        setSelectableLanguages()
    }
}
