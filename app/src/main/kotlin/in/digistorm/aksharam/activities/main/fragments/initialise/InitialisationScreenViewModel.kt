/*
 * Copyright (c) 2023 Alan M Varghese <alan@digistorm.in>
 *
 * This files is part of Aksharam, a script teaching app for Indic
 * languages.
 *
 * Aksharam is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aksharam is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even teh implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package `in`.digistorm.aksharam.activities.main.fragments.initialise

import android.app.Application
import android.view.View
import androidx.lifecycle.*
import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.activities.main.util.IdlingResourceHelper
import `in`.digistorm.aksharam.activities.main.util.LanguageFile
import `in`.digistorm.aksharam.activities.main.util.Network
import `in`.digistorm.aksharam.activities.main.util.logDebug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class InitialisationScreenViewModel(application: Application): AndroidViewModel(application) {
    private val logTag = javaClass.simpleName

    val initialisationStatus: MutableLiveData<String> = MutableLiveData(
        application.getString(R.string.initialisation_checking_data))

    val onlineFiles: MutableLiveData<List<LanguageFile>?> = MutableLiveData(listOf())

    val progressBarStatus: MutableLiveData<Int> = MutableLiveData(View.VISIBLE)

    var languagesToDownload: MutableSet<LanguageFile> = mutableSetOf()

    var proceedButtonEnabled: MutableLiveData<Boolean> = MutableLiveData()

    init {
        initialise()
    }

    fun initialise() {
        viewModelScope.launch {
            progressBarStatus.postValue(View.VISIBLE)
            logDebug(logTag, "Fetching data files available online...")
            onlineFiles.value = withContext(Dispatchers.IO) {
                try {
                    IdlingResourceHelper.countingIdlingResource.increment()
                    Network.onlineFiles.getContents()
                }
                catch (e: Exception) {
                    null
                } finally {
                    if(!IdlingResourceHelper.countingIdlingResource.isIdleNow)
                        IdlingResourceHelper.countingIdlingResource.decrement()
                }
            }
            if(onlineFiles.value?.isNotEmpty() == true) {
                initialisationStatus.postValue(getApplication<Application>().getString(R.string.initialisation_choice_hint))
            }
        }
    }
}