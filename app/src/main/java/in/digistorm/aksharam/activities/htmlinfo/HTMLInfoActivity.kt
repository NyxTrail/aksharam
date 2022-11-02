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
package `in`.digistorm.aksharam.activities.htmlinfo

import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.util.logDebug

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.text.method.LinkMovementMethod
import android.text.Html

/* A full screen TextView activity to display information in html for
   help, privacy policy etc
 */
class HTMLInfoActivity : AppCompatActivity() {
    private val logTag = HTMLInfoActivity::class.simpleName

    companion object {
        var EXTRA_NAME = "HTMLINFO_EXTRA"
    }

    enum class ExtraValues {
        HELP, PRIVACY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logDebug(logTag, "Starting HTMLInfoActivity...")
        setContentView(R.layout.activity_help)
        val extra = intent.extras!![EXTRA_NAME]
        val htmlInfoActivityTV = findViewById<TextView>(R.id.htmlinfo_activity_tv)
        htmlInfoActivityTV.movementMethod = LinkMovementMethod.getInstance()
        if (extra === ExtraValues.HELP) {
            logDebug(logTag, "Displaying help")
            htmlInfoActivityTV.text = Html.fromHtml(getString(R.string.help_text), Html.FROM_HTML_MODE_LEGACY)
        } else if (extra === ExtraValues.PRIVACY) {
            logDebug(logTag, "Displaying privacy")
            htmlInfoActivityTV.text = Html.fromHtml(getString(R.string.privacy_text), Html.FROM_HTML_MODE_LEGACY)
        }
    }
}