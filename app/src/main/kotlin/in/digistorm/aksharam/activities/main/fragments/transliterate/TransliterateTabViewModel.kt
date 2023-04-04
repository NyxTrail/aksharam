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

    private val downloadedLanguages: MutableLiveData<List<String>> = MutableLiveData(arrayListOf())

    val currentInput: MutableLiveData<String> = MutableLiveData()

    val detectedLanguage: LiveData<String?> = currentInput.map { input ->
        LanguageDetector(application).detectLanguage(input)
    }

    var language: LiveData<Language?> = detectedLanguage.map { languageName ->
        if(languageName != null)
            getLanguageData(languageName, application)!!
        else
            null
    }

    var selectableLanguages: LiveData<List<String>> = detectedLanguage.map { languageName ->
        (downloadedLanguages.value as? Collection<String> ?: listOf()).filter { currentString ->
            return@filter currentString != languageName
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
