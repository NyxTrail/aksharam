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
import `in`.digistorm.aksharam.util.*

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import java.io.IOException
import java.util.ArrayList

class SettingsActivity : AppCompatActivity() {
    private val logTag = SettingsActivity::class.simpleName

    private fun populateLanguageList() {
        val manageLanguagesRV = findViewById<RecyclerView>(R.id.SettingsActivityManageLanguagesRV)

        // get downloaded files
        val filesList: ArrayList<String> = getDownloadedFiles(applicationContext)
        logDebug(logTag, "List of files available: $filesList")
        val self = this

        // get files online
        GlobalSettings.instance?.threadPoolExecutor?.execute {
            val languageDataDownloader = LanguageDataDownloader()
            val onlineFiles: JSONArray
            try {
                onlineFiles = languageDataDownloader.languageDataFiles
                runOnUiThread {
                    /* TODO: Verify, onlineFiles cannot really be null anymore? Ever? Ever, ever? */
                    if (onlineFiles == null) {
                        // show Toast saying we could not download the files online
                        Toast.makeText(
                            self,
                            R.string.could_not_download_file_list,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    // hide the progress bar
                    findViewById<View>(R.id.SettingsActivityManageLanguagesPB).visibility =
                        View.GONE
                    manageLanguagesRV.visibility = View.VISIBLE
                    logDebug(logTag, "Setting adapter for language list")
                    manageLanguagesRV.layoutManager = LinearLayoutManager(self)
                    val manageLanguageListAdapter =
                        SettingsLanguageListAdapter(filesList, onlineFiles, this)
                    manageLanguagesRV.adapter = manageLanguageListAdapter
                }
            } catch (ie: IOException) {
                runOnUiThread {
                    Toast.makeText(self, R.string.could_not_download_file_list, Toast.LENGTH_LONG)
                        .show()
                    // hide the progress bar
                    findViewById<View>(R.id.SettingsActivityManageLanguagesPB).visibility =
                        View.GONE
                    manageLanguagesRV.visibility = View.VISIBLE
                    logDebug(logTag, "Setting adapter for language list")
                    manageLanguagesRV.layoutManager = LinearLayoutManager(self)
                    val manageLanguageListAdapter =
                        SettingsLanguageListAdapter(filesList, null, this)
                    manageLanguagesRV.adapter = manageLanguageListAdapter
                }
            }
    } ?: logDebug(logTag, "test")
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logDebug(logTag, "Setting up the settings activity...")
        setContentView(R.layout.activity_settings)
        populateLanguageList()
    }

    override fun onDestroy() {
        super.onDestroy()
        logDebug(logTag, "Clearing listeners before being destroyed.")
        if (getDownloadedLanguages(this).isEmpty())
            GlobalSettings.instance!!.clearDataFileListChangedListeners()
    }
}