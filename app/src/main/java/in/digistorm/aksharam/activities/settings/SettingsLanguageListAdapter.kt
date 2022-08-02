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
package `in`.digistorm.aksharam.activities.settings

import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.util.GlobalSettings
import `in`.digistorm.aksharam.util.OnRequestCompleted
import `in`.digistorm.aksharam.util.LanguageDataDownloader
import `in`.digistorm.aksharam.util.logDebug

import org.json.JSONArray
import android.app.Activity
import android.widget.Toast
import org.json.JSONException
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import java.lang.Exception
import java.util.ArrayList
import androidx.recyclerview.widget.RecyclerView
import androidx.constraintlayout.widget.ConstraintLayout

class SettingsLanguageListAdapter(
    private val fileList: ArrayList<String>?,
    private val onlineFiles: JSONArray?,
    private val activity: Activity
) : RecyclerView.Adapter<SettingsLanguageListAdapter.ViewHolder>() {
    private val logTag = SettingsLanguageListAdapter::class.simpleName

    private inner class DownloadClickListener(
        private val holder: ViewHolder,
        private val adapter: SettingsLanguageListAdapter
    ) : View.OnClickListener {
        override fun onClick(v: View) {
            try {
                val fileName = onlineFiles!!.getJSONObject(holder.adapterPosition).getString("name")
                val url =
                    onlineFiles.getJSONObject(holder.adapterPosition).getString("download_url")
                logDebug(logTag, "Downloading file $fileName")
                val dataDownloader = LanguageDataDownloader()
                dataDownloader.download(fileName, url, activity, object : OnRequestCompleted {
                    override fun onDownloadCompleted() {
                        logDebug(logTag, "Download completed for file: $fileName")
                        v.visibility = View.GONE
                        val deleteIV =
                            holder.parentConstraintLayout.findViewById<ImageView>(R.id.ManageLanguageDeleteIV)
                        deleteIV.visibility = View.VISIBLE
                        val deleteClickListener = DeleteClickListener(holder, true, adapter)
                        deleteIV.setOnClickListener(deleteClickListener)
                        // transliterator.getLangDataReader().getAvailableSourceLanguages(activity);
                        GlobalSettings.instance!!.invokeDataFileListChangedListeners()
                    }

                    override fun onDownloadFailed(e: Exception?) {
                        Toast.makeText(
                            activity,
                            R.string.could_not_download_file,
                            Toast.LENGTH_LONG
                        ).show()
                        logDebug(
                            logTag,
                            "Download failed for file " + fileName + " due to exception: " + e?.message
                        )
                        e?.printStackTrace()
                    }
                })
            } catch (je: JSONException) {
                logDebug("DownloadClickListener", "JSONException while unpacking onlineFiles.")
                je.printStackTrace()
            }
        }
    }

    private inner class DeleteClickListener(
        private val holder: ViewHolder,
        private val onlineFilesAvailable: Boolean,
        private val adapter: SettingsLanguageListAdapter
    ) : View.OnClickListener {
        override fun onClick(v: View) {
            if (!onlineFilesAvailable) {
                val pos = holder.adapterPosition
                val fileName = fileList!![pos]
                logDebug(logTag, "Deleting file $fileName")
                activity.deleteFile(fileName)
                fileList.remove(fileName)
                adapter.notifyItemRemoved(pos)
                GlobalSettings.instance!!.invokeDataFileListChangedListeners()
                return
            }
            try {
                val fileName = onlineFiles!!.getJSONObject(holder.adapterPosition).getString("name")
                logDebug(logTag, "Deleting file $fileName")
                activity.deleteFile(fileName)
                v.visibility = View.GONE
                val downloadIV =
                    holder.parentConstraintLayout.findViewById<ImageView>(R.id.ManageLanguageDownloadIV)
                downloadIV.visibility = View.VISIBLE
                val downloadClickListener = DownloadClickListener(holder, adapter)
                downloadIV.setOnClickListener(downloadClickListener)
                GlobalSettings.instance!!.invokeDataFileListChangedListeners()
            } catch (je: JSONException) {
                logDebug(
                    "DeleteClickListener",
                    "JSONException when fetching file from online files list"
                )
                je.printStackTrace()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        logDebug(logTag, "Initialising adapter for managing language data file list...")
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.settings_language_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val langItemTV = holder.parentConstraintLayout.findViewById<TextView>(R.id.ManageLanguageTV)
        try {
            if (onlineFiles != null) {
                logDebug(logTag, "Data file list from repo is available.")
                val fileName = onlineFiles.getJSONObject(position).getString("name")
                // if we have downloaded this file, show the delete icon
                if (fileList!!.contains(fileName)) {
                    logDebug(logTag, "Found file $fileName in the local download directory.")
                    val deleteIV =
                        holder.parentConstraintLayout.findViewById<ImageView>(R.id.ManageLanguageDeleteIV)
                    deleteIV.visibility = View.VISIBLE
                    val deleteClickListener = DeleteClickListener(holder, true, this)
                    deleteIV.setOnClickListener(deleteClickListener)
                } else {
                    logDebug(logTag, "Could not find file $fileName in the local download directory.")
                    val downloadIV =
                        holder.parentConstraintLayout.findViewById<ImageView>(R.id.ManageLanguageDownloadIV)
                    downloadIV.visibility = View.VISIBLE
                    val downloadClickListener = DownloadClickListener(holder, this)
                    downloadIV.setOnClickListener(downloadClickListener)
                }
                val text = fileName.substring(0, 1).uppercase() + fileName.substring(
                    1,
                    fileName.length - 5
                )
                langItemTV.text = text
            } else if (fileList != null) {
                logDebug(logTag, "Could not get online files. Displaying local files only.")
                // if online files are not available, at least display the local files
                var text = fileList[position]
                text = text.substring(0, 1).uppercase() + text.substring(1, text.length - 5)
                langItemTV.text = text
                val deleteIV =
                    holder.parentConstraintLayout.findViewById<ImageView>(R.id.ManageLanguageDeleteIV)
                deleteIV.visibility = View.VISIBLE
                val deleteClickListener = DeleteClickListener(holder, false, this)
                deleteIV.setOnClickListener(deleteClickListener)
            }
            // else... what happens if we did not get file online and there are no files in device?
            // that should usually take us back to the initialisation activity (not tested)
        } catch (je: JSONException) {
            logDebug(
                logTag,
                "JSONException caught while populating language item at position $position"
            )
            je.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return onlineFiles?.length() ?: fileList!!.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val parentConstraintLayout: ConstraintLayout

        init {
            parentConstraintLayout = view.findViewById(R.id.ManageLanguageListItemCL)
            // TODO: set the click listener
        }
    }
}