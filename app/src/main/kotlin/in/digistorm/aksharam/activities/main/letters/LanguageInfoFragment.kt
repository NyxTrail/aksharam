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

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.widget.TextView
import android.text.Html
import android.view.View
import androidx.fragment.app.Fragment

class LanguageInfoFragment : Fragment() {
    private val logTag = LanguageInfoFragment::class.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        return inflater.inflate(R.layout.language_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var info: String? = null
        if (arguments != null) info = requireArguments().getString("info")
        if (info == null) logDebug(logTag, "Info is null")
        (view.findViewById<View>(R.id.languageInfoTV) as TextView).text = Html.fromHtml(info)
    }

    companion object {
        fun newInstance(info: String?): LanguageInfoFragment {
            val lif = LanguageInfoFragment()
            val args = Bundle()
            args.putString("info", info)
            lif.arguments = args
            return lif
        }
    }
}