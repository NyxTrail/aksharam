package `in`.digistorm.aksharam.activities.main.fragments.transliterate

import `in`.digistorm.aksharam.activities.main.language.Language
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import `in`.digistorm.aksharam.activities.main.ActivityViewModel

// View model for the 'Transliterate' tab fragment
class TransliterateTabViewModel(
    application: Application,
    private val activityViewModel: ActivityViewModel
): AndroidViewModel(application) {
    private val logTag = javaClass.simpleName

    /*
       The target language to transliterate a string to. This is set by a spinner in the
       Fragment.
     */
    var targetLanguage: String = ""

    fun getLanguageData(): Language {
        return activityViewModel.language.value!!
    }
}
