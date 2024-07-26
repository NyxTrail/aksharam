/*
 * Copyright (c) 2023-2024 Alan M Varghese <alan@digistorm.in>
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
package `in`.digistorm.aksharam.activities.main.fragments

import `in`.digistorm.aksharam.R
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textview.MaterialTextView

class PrivacyFragment: Fragment() {
    private lateinit var privacyText: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PrivacyScreen()
            }
        }
    }

    @Preview(name = "PrivacyScreen", device = "id:pixel_2")
    @Composable
    fun PrivacyScreen() {
        Column(modifier = Modifier.padding(12.dp)) {
            AndroidView(
                factory = {
                    privacyText = it.getString(R.string.privacy_text)
                    val mtv = MaterialTextView(it).apply {
                        linksClickable = true
                        movementMethod = LinkMovementMethod.getInstance()
                        setTextIsSelectable(true)
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                    }
                    mtv
                },
                update = {
                    it.text = HtmlCompat.fromHtml(
                        privacyText, HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                }
            )
        }
    }
}