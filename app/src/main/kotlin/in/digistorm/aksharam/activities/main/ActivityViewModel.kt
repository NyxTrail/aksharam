package `in`.digistorm.aksharam.activities.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import `in`.digistorm.aksharam.activities.main.language.Language
import androidx.lifecycle.MutableLiveData
import `in`.digistorm.aksharam.activities.main.language.getDownloadedLanguages
import `in`.digistorm.aksharam.activities.main.util.logDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Activity scoped ViewModel
 */
class ActivityViewModel(application: Application): AndroidViewModel(application) {
    private val logTag = javaClass.simpleName

    // Currently used language. Used to persist current language in multiple fragments of
    // letters tab.
    val language: MutableLiveData<Language> = MutableLiveData()

    val availableLanguages: MutableLiveData<ArrayList<String>> = MutableLiveData()

    init {
        logDebug(logTag, "Getting downloaded languages.")
        CoroutineScope(Dispatchers.Default).launch {
            availableLanguages.postValue(getDownloadedLanguages(application))
        }
    }
}