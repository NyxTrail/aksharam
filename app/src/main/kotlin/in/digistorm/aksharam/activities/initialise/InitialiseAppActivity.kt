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
import `in`.digistorm.aksharam.activities.main.MainActivity
import `in`.digistorm.aksharam.util.*

import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Intent
import android.widget.Toast
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import java.io.IOException
import java.lang.Exception

class InitialiseAppActivity : AppCompatActivity() {
    private val logTag = InitialiseAppActivity::class.simpleName
    private val languageDataDownloader: LanguageDataDownloader = LanguageDataDownloader()
    private var adapter: LanguageDataFileListAdapter? = null
    private fun showLanguageDataSelectionList(languageDataFiles: JSONArray) {
        val self = this

        // This is called from the background thread; views can be updated only in the threads that
        // created them
        runOnUiThread {
            logDebug(logTag, "Updating RecyclerView in UI thread")
            // First, hide the progress bar
            findViewById<View>(R.id.InitialiseAppProgressBar).visibility = View.INVISIBLE
            // Set the appropriate hint
            (findViewById<View>(R.id.InitialiseAppHintTV) as TextView).setText(R.string.initialisation_choice_hint)
            val languageListRV = findViewById<RecyclerView>(R.id.InitialiseAppLangListRV)
            adapter = LanguageDataFileListAdapter(languageDataFiles)
            languageListRV.layoutManager = LinearLayoutManager(self)
            languageListRV.adapter = adapter
            (findViewById<View>(R.id.InitialiseAppProceedButton) as Button).isEnabled = true
        }
    }

    private fun startMainActivity(): Boolean {
        logDebug(logTag, "Attempting to start main activity...")
        // First check if we have data files available
        if (getDownloadedLanguages(this).isEmpty()) {
            logDebug(logTag, "No data files found!! Continuing initialisation activity")
            // we cannot start the main activity
            return false
        }
        logDebug(logTag, "Starting MainActivity...")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        // Main activity is started
        return true
    }

    private fun proceed(v: View) {
        logDebug(logTag, "Proceed button clicked!")
        if (adapter == null || adapter!!.itemCount <= 0) return
        val dataFileList = adapter?.dataFileList
        if(dataFileList == null || dataFileList.length() == 0) return

        for (i in 0 until dataFileList.length()) {
            // If at least one file is selected, continue
            if (dataFileList.optJSONObject(i).optBoolean("selected", false))
                break
            // if we have gone through all items in the list and not found a single item marked "selected"
            if (i == dataFileList.length() - 1) {
                // show a message
                (findViewById<View>(R.id.InitialiseAppHintTV) as TextView)
                        .setText(R.string.initialisation_no_file_selected)
                // and return
                return
            }
        }
        v.isEnabled = false
        findViewById<View>(R.id.InitialiseAppProgressBar).visibility = View.VISIBLE
        languageDataDownloader.download(adapter?.dataFileList!!,
            this,
            object : OnRequestCompleted {
                override fun onDownloadCompleted() {
                    logDebug(logTag, "Download completed; starting MainActivity...")
                    startMainActivity()
                    logDebug(logTag, "InitialiseAppActivity finishing...")
                    finish()
                }

                override fun onDownloadFailed(e: Exception?) {
                    v.isEnabled = true
                    findViewById<View>(R.id.InitialiseAppProgressBar).visibility = View.INVISIBLE
                    Toast.makeText(applicationContext, R.string.could_not_download_file,
                        Toast.LENGTH_LONG).show()
                    logDebug(logTag, "Download failed due to exception $e")
                    e?.printStackTrace()
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        logDebug(logTag, "Activity destroying...")
    }

    private fun showNoInternetDialog() {
        logDebug("NoInternetDialog", "Showing NoInternetDialog.")
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(R.string.could_not_download_file)
            .setTitle(R.string.no_internet)
            .setCancelable(false)
            .setNegativeButton(R.string.exit) { dialog: DialogInterface, _: Int ->
                logDebug("NoInternetDialog", "Exit button was clicked")
                dialog.dismiss()
                finish()
            }
            .setPositiveButton(R.string.retry_download) { _: DialogInterface?, _: Int -> setUpActivity() }
            .setOnDismissListener {
                logDebug(
                    "NoInternetDialog",
                    "Dialog dismissed!"
                )
            }
        val dialog1 = dialogBuilder.create()
        dialog1.show()
    }

    private fun setUpActivity() {
        // Attempt to start main activity
        val mainActivityStarted = startMainActivity()
        if (!mainActivityStarted) {
            // if we are not able to start the main activity...
            // continue setting up the initialisation activity (current activity)
            setContentView(R.layout.initialise_app_activity)
            GlobalSettings.instance?.threadPoolExecutor?.execute {
                try {
                    showLanguageDataSelectionList(languageDataDownloader.languageDataFiles)
                } catch (ie: IOException) {
                    logDebug(logTag, "IOException caught while downloading language list")
                    runOnUiThread { showNoInternetDialog() }
                }
            }
            findViewById<View>(R.id.InitialiseAppProceedButton).setOnClickListener { v: View -> proceed(v) }
        } else finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logDebug(logTag, "Starting app...")
        GlobalSettings.createInstance(this)
        if (GlobalSettings.instance?.darkMode == true) {
            // which mode did the activity start in?
            val nightMode = AppCompatDelegate.getDefaultNightMode()
            // If dark mode is enabled, this causes activity to restart
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            // continue activity initialisation only if activity started in night mode
            if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) setUpActivity()
        } else { // if light mode; nothing special, it should be light mode by default
            setUpActivity()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}