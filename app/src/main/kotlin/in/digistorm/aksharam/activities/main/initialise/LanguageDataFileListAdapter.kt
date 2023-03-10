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
package `in`.digistorm.aksharam.activities.main.initialise

import `in`.digistorm.aksharam.util.logDebug

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.CheckedTextView
import `in`.digistorm.aksharam.activities.main.util.LanguageFile
import `in`.digistorm.aksharam.databinding.LanguageDataFileListItemBinding
import org.json.JSONException
import kotlin.Throws

class LanguageDataFileListAdapter(
    private val dataFileList: List<LanguageFile>,
    val addLanguageToDownload: (LanguageFile) -> Unit,
    val removeLanguageToDownload: (LanguageFile) -> Unit
) :
    RecyclerView.Adapter<LanguageDataFileListAdapter.ViewHolder>() {
    // apparently log tag can at most be 23 characters
    private val logTag = javaClass.simpleName

    inner class ViewHolder(
        private val binding: LanguageDataFileListItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        val checkBox: CheckedTextView
            get() = binding.checkbox

        var languageFile: LanguageFile? = null
            set(value) {
                if(value != null) {
                    checkBox.text = value.displayName
                    field = value
                }
            }
        init {
            binding.checkbox.setOnClickListener {
                binding.checkbox.isChecked = !binding.checkbox.isChecked
                if(binding.checkbox.isChecked)
                    addLanguageToDownload(languageFile!!)
                else
                    removeLanguageToDownload(languageFile!!)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        logDebug(logTag, "Initialising adapter for language data file list...")
        val binding = LanguageDataFileListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        logDebug(logTag, "Binding CheckedTextView at position: $position")

        holder.languageFile = dataFileList[position]
    }

    @Throws(JSONException::class)
    fun getItem(position: Int): LanguageFile {
        return dataFileList[position]
    }

    override fun getItemCount(): Int {
        return dataFileList.size
    }

}
