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