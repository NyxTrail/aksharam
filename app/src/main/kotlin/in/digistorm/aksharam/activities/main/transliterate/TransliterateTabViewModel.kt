package `in`.digistorm.aksharam.activities.main.transliterate

import `in`.digistorm.aksharam.util.Language
import `in`.digistorm.aksharam.util.Transliterator
import android.app.Application
import androidx.lifecycle.AndroidViewModel

// View model for the 'Transliterate' tab fragment
class TransliterateTabViewModel(application: Application): AndroidViewModel(application) {
    private val logTag = javaClass.simpleName

    /*
       The target language to transliterate a string to. This is set by a spinner in the
       Fragment.
     */
    var targetLanguage: String = ""

    // The Transliterator instance used to perform the transliteration.
    var transliterator: Transliterator = Transliterator.create(application)

    fun getLanguageData(): Language {
        return transliterator.languageData
    }
}
