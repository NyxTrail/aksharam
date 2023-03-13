package `in`.digistorm.aksharam.activities.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import `in`.digistorm.aksharam.activities.main.language.Language
import androidx.lifecycle.MutableLiveData
import `in`.digistorm.aksharam.activities.main.language.getDownloadedLanguages

/**
 * Activity scoped ViewModel
 */
class ActivityViewModel(application: Application): AndroidViewModel(application) {
    private val logTag = javaClass.simpleName

    // Currently used language. Used to persist current language in multiple fragments of
    // letters tab.
    val language: MutableLiveData<Language> = MutableLiveData()

    val availableLanguages: MutableLiveData<ArrayList<String>> = MutableLiveData(
        getDownloadedLanguages(application)
    )
}