package `in`.digistorm.aksharam.activities.main.fragments.transliterate

import android.app.Application
import androidx.lifecycle.*
import `in`.digistorm.aksharam.activities.main.language.*
import `in`.digistorm.aksharam.activities.main.util.logDebug
import `in`.digistorm.aksharam.activities.main.language.transliterate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

// View model for the 'Transliterate' tab fragment
class TransliterateTabViewModel(
    application: Application,
): AndroidViewModel(application) {
    private val logTag = javaClass.simpleName

    private var languageDetector: LanguageDetector? = null

    val currentInput: MutableLiveData<String> = MutableLiveData()

    var language: LiveData<Language?> = MediatorLiveData<Language>().apply {
        addSource(currentInput) {
            logDebug(logTag, "Starting async task at: ${System.currentTimeMillis()}")
            viewModelScope.async(Dispatchers.IO) {
                if (languageDetector == null)
                    languageDetector = LanguageDetector(application)
                postValue(languageDetector!!.detectLanguage(it))
                logDebug(logTag, "Async task completed at: ${System.currentTimeMillis()}")
            }
            logDebug(
                logTag,
                "Finished and continuing after starting async task at: ${System.currentTimeMillis()}"
            )
        }
    }

    var selectableLanguages: LiveData<List<String>> = language.map { language ->
        language?.supportedLanguagesForTransliteration ?: listOf()
    }

    /*
       The target language to transliterate a string to. This is set by a spinner in the
       Fragment.
     */
    var targetLanguageSelected: MutableLiveData<String> = MutableLiveData()

    var transliteratedString: MutableLiveData<String?> = MediatorLiveData<String>().apply {
        addSource(currentInput) { currentInput ->
            logDebug(logTag, "Change detected in currentInput")
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

    fun resetLanguageDetector() {
        languageDetector = null
    }
}
