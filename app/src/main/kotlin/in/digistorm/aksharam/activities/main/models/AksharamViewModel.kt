package `in`.digistorm.aksharam.activities.main.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import `in`.digistorm.aksharam.util.Language
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import `in`.digistorm.aksharam.util.getDownloadedLanguages

/**
 * Activity scoped ViewModel
 */
class AksharamViewModel(application: Application): AndroidViewModel(application) {
    private val logTag = javaClass.simpleName

    // Currently used language. Used to persist current language in multiple fragments of
    // letters tab.
    val language: MutableLiveData<Language> = MutableLiveData()

    val availableLanguages: MutableLiveData<ArrayList<String>> = MutableLiveData(
        getDownloadedLanguages(application)
    )
}