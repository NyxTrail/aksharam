package `in`.digistorm.aksharam.activities.main.models

import `in`.digistorm.aksharam.activities.main.AksharamFile
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel for the main activity.
 */
class AksharamViewModel: ViewModel() {
    private val logTag = javaClass.simpleName

    private var languageFiles = MutableLiveData<MutableList<AksharamFile>>()
}