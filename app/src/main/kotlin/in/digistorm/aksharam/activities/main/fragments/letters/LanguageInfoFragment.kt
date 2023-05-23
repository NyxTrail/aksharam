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
package `in`.digistorm.aksharam.activities.main.fragments.letters

import `in`.digistorm.aksharam.activities.main.ActivityViewModel
import `in`.digistorm.aksharam.activities.main.util.logDebug

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import `in`.digistorm.aksharam.databinding.FragmentLanguageInfoBinding

class LanguageInfoFragment : Fragment() {
    private val logTag = javaClass.simpleName

    private lateinit var binding: FragmentLanguageInfoBinding
    private val args: LanguageInfoFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentLanguageInfoBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activityViewModel: ActivityViewModel by activityViewModels()

        val languageData = activityViewModel.language.value
        logDebug(logTag, "Info: ${languageData?.info}")
        logDebug(logTag, "TargetLanguage: ${args.targetLanguage}")
        val stringToDisplay: String =
            "" + (languageData?.info?.get("general")?.get("en") ?: "") +
                    (languageData?.info?.get(args.targetLanguage.lowercase())?.get("en") ?: "")
        logDebug(logTag, "Info to display: $stringToDisplay")
        if(stringToDisplay.isEmpty()) {
            logDebug(logTag, "No info to display. Returning to Letters Tab.")
            findNavController().popBackStack()
        }

        binding.languageInfoTV.text =
            HtmlCompat.fromHtml(stringToDisplay, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
}
