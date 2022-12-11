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
package `in`.digistorm.aksharam.activities.initialise

import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.util.logDebug

import org.json.JSONArray
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckedTextView
import org.json.JSONException
import kotlin.Throws
import org.json.JSONObject

class LanguageDataFileListAdapter(val dataFileList: JSONArray) :
    RecyclerView.Adapter<LanguageDataFileListAdapter.ViewHolder>() {
    // apparently log tag can at most be 23 characters
    private val logTag = javaClass.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        logDebug(logTag, "Initialising adapter for language data file list...")
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.language_data_file_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        logDebug(logTag, "Binding CheckedTextView at position: $position")
        try {
            var language = dataFileList.getJSONObject(position).getString("name")
            language = (language.substring(0, 1).uppercase()
                    + language.substring(1, language.length - ".json".length))
            holder.checkedTextView.text = language
            holder.checkedTextView.setOnClickListener {
                val checkedTextView = holder.checkedTextView
                checkedTextView.isChecked = !checkedTextView.isChecked
                try {
                    dataFileList.getJSONObject(position).put("selected", checkedTextView.isChecked)
                } catch (je: JSONException) {
                    logDebug(
                        logTag,
                        "JSONException caught while inserting selection status into data file list"
                    )
                    je.printStackTrace()
                }
            }
        } catch (e: JSONException) {
            logDebug(
                logTag,
                "JSONException caught while processing data file list obtained from server."
            )
            e.printStackTrace()
        }
    }

    @Throws(JSONException::class)
    fun getItem(position: Int): JSONObject {
        return dataFileList.getJSONObject(position)
    }

    override fun getItemCount(): Int {
        return dataFileList.length()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkedTextView: CheckedTextView

        init {
            checkedTextView = view.findViewById(R.id.LanguageDataFileListCTV)
            checkedTextView.setOnClickListener {
                checkedTextView.isChecked = !checkedTextView.isChecked
            }
        }
    }
}
