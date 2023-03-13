package `in`.digistorm.aksharam.activities.main.fragments.initialise

import android.app.Application
import android.view.View
import androidx.lifecycle.*
import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.activities.main.util.LanguageFile
import `in`.digistorm.aksharam.activities.main.util.Network
import `in`.digistorm.aksharam.activities.main.util.logDebug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InitialisationScreenViewModel(application: Application): AndroidViewModel(application) {
    private val logTag = javaClass.simpleName

    var initialisationStatus: MutableLiveData<String> = MutableLiveData(
        application.getString(R.string.initialisation_checking_data))

    var onlineFiles: MutableLiveData<List<LanguageFile>> = MutableLiveData(listOf())

    var progressBarStatus: LiveData<Int> = onlineFiles.map { list ->
        when(list.size) {
            0 -> View.VISIBLE
            else -> View.GONE
        }
    }

    var languagesToDownload: MutableSet<LanguageFile> = mutableSetOf()

    var proceedButtonEnabled: MutableLiveData<Boolean> = MutableLiveData()

    init {
        viewModelScope.launch {
            logDebug(logTag, "Fetching data files available online...")
            val languageFiles = withContext(Dispatchers.IO) {
                Network.onlineFiles.getContents()
            }
            onlineFiles.value = languageFiles
            if(onlineFiles.value?.isNotEmpty() == true) {
                initialisationStatus.postValue(application.getString(R.string.initialisation_choice_hint))
            }
        }
    }
}