/*
 * Copyright (c) 2022 Alan M Varghese <alan@digistorm.in>
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

package `in`.digistorm.aksharam.activities.main.models

import `in`.digistorm.aksharam.activities.main.AksharamFile
import `in`.digistorm.aksharam.activities.main.LanguageListAdapter
import `in`.digistorm.aksharam.activities.main.util.Network
import `in`.digistorm.aksharam.activities.main.util.getLocalFiles
import `in`.digistorm.aksharam.databinding.FragmentSettingsBinding
import `in`.digistorm.aksharam.util.logDebug
import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

class SettingsViewModel: ViewModel() {
    private val logTag = javaClass.simpleName

    private val _languageFiles: MutableLiveData<List<AksharamFile>> = MutableLiveData()
    val languageFiles: LiveData<List<AksharamFile>>
        get() = _languageFiles

    fun fetchLanguageFiles(
        context: Context
    ): Job {
        val job = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val onlineFiles = Network.onlineFiles.getContents()
                logDebug(logTag, "Files available online: $onlineFiles")
                val localFiles = getLocalFiles(context)
                logDebug(logTag, "File available locally: $localFiles")
                val files = mutableListOf<AksharamFile>()
                for (file in onlineFiles) {
                    if (localFiles.contains(file.name)) {
                        files.add(AksharamFile(onlineLanguageFile = file, localFileName = file.name, isDownloaded = true))
                        localFiles.remove(file.name)
                    } else
                        files.add(AksharamFile(onlineLanguageFile = file, localFileName = null, isDownloaded = false))
                }
                // Local files that are not available online
                for (file in localFiles) {
                    files.add(AksharamFile(localFileName = file, isDownloaded = true))
                }
                _languageFiles.postValue(files)
            }
        }
        return job
    }
}