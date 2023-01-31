package `in`.digistorm.aksharam.activities.main.models

import `in`.digistorm.aksharam.activities.main.AksharamFile
import `in`.digistorm.aksharam.util.Language
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Activity scoped ViewModel
 */
class AksharamViewModel: ViewModel() {
    private val logTag = javaClass.simpleName

    val language: MutableLiveData<Language> = MutableLiveData()
}