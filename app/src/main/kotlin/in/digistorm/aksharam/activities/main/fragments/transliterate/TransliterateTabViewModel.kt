package `in`.digistorm.aksharam.activities.main.fragments.transliterate

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.activities.main.language.*
import `in`.digistorm.aksharam.activities.main.util.logDebug
import `in`.digistorm.aksharam.util.transliterate

// View model for the 'Transliterate' tab fragment
class TransliterateTabViewModel(
    application: Application,
): AndroidViewModel(application) {
    /* TODO: show error messages in the language selection spinner */
    private val logTag = javaClass.simpleName

    private val downloadedLanguages: MutableLiveData<List<String>> = MutableLiveData(arrayListOf())

    val currentInput: MutableLiveData<String> = MutableLiveData()

    val detectedLanguage: LiveData<String> = currentInput.map { input ->
        LanguageDetector(application).detectLanguage(input) ?: ""
    }

    var language: LiveData<Language?> = detectedLanguage.distinctUntilChanged().map { languageName ->
        logDebug(logTag, "Fetching language data")
        getLanguageData(languageName, application)
    }

    var selectableLanguages: LiveData<List<String>> = detectedLanguage.map { languageName ->
        if(languageName.isEmpty())
            listOf()
        else {
            (downloadedLanguages.value ?: listOf()).filter { currentString ->
                return@filter currentString != languageName
            }
        }
    }

    /*
       The target language to transliterate a string to. This is set by a spinner in the
       Fragment.
     */
    var targetLanguageSelected: MutableLiveData<String> = MutableLiveData()

    var transliteratedString: MutableLiveData<String?> = MediatorLiveData<String>().apply {
        addSource(currentInput) { currentInput ->
            if(detectedLanguage.value == null) {
                value = this@TransliterateTabViewModel.getApplication<Application>()
                    .getString(R.string.lang_could_not_detect)
            }
            targetLanguageSelected.value?.let { targetLanguage ->
                language.value?.let { languageValue ->
                    value = transliterate(currentInput, targetLanguage, languageValue)
                }
            }
        }

        addSource(language) { language ->
            if(language != null && targetLanguageSelected.value != null)
                value = transliterate(currentInput.value!!, targetLanguageSelected.value!!, language)
        }

        addSource(targetLanguageSelected) { newLanguageSelected ->
            currentInput.value?.let { currentInput ->
                language.value?.let { languageValue ->
                    value = transliterate(currentInput, newLanguageSelected, languageValue)
                }
            }
        }
    }

    fun initialise() {
        logDebug(logTag, "Initialising.")
        downloadedLanguages.value = getDownloadedLanguages(getApplication())
    }
}
