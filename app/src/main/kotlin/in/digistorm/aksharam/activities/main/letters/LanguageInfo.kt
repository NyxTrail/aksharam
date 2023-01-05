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
package `in`.digistorm.aksharam.activities.main.letters

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.google.accompanist.themeadapter.material3.Mdc3Theme

@Composable
fun LanguageInfo(info: String) {
    val htmlInfo = HtmlCompat.fromHtml(info, HtmlCompat.FROM_HTML_MODE_COMPACT)

    Mdc3Theme {
        Surface {
            AndroidView(
                factory = { context ->
                    TextView(context).apply {
                        movementMethod = LinkMovementMethod.getInstance()
                    }
                },
                update = { textView ->
                    textView.text = htmlInfo
                }
            )
        }
    }
}

@Preview
@Composable
fun LanguageInfoPreview() {
    LanguageInfo("This is some text in HTML<br/> used to test compose preview.<br>")
}