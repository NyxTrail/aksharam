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

import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.util.logDebug
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.core.view.marginStart
import androidx.core.widget.TextViewCompat
import com.google.accompanist.themeadapter.material3.Mdc3Theme

@Composable
fun LanguageInfo(info: String) {
    val htmlInfo = HtmlCompat.fromHtml(info, HtmlCompat.FROM_HTML_MODE_COMPACT)
    val modifier: Modifier = Modifier
        .padding(dimensionResource(id = R.dimen.html_margin_horizontal_major))
        .verticalScroll(rememberScrollState(), true)

    Mdc3Theme {
        AndroidView(
            factory = { context ->
                AppCompatTextView(context).apply {
                    TextViewCompat.setTextAppearance(this, R.style.LanguageInfo)
                    scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
                    movementMethod = LinkMovementMethod.getInstance()
                }
            },
            modifier = modifier,
            update = { textView ->
                textView.text = htmlInfo
            }
        )
    }
}

@Preview
@Composable
fun LanguageInfoPreview() {
    Mdc3Theme {
        LanguageInfo("This is some text in HTML<br/> used to test compose preview.<br>")
    }
}