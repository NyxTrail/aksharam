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

import `in`.digistorm.aksharam.activities.main.models.AksharamViewModel
import `in`.digistorm.aksharam.databinding.FragmentLettersTabBinding
import `in`.digistorm.aksharam.util.*

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class LettersTabFragment: Fragment() {
    private val logTag = javaClass.simpleName

    private lateinit var binding: FragmentLettersTabBinding
    private val viewModel: LettersTabViewModel by viewModels()
    private val activityViewModel: AksharamViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLettersTabBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logDebug(logTag, "onViewCreated")

        if(activityViewModel.availableLanguages.value?.isEmpty() == true) {
            logDebug(logTag, "No downloaded files found!")
        }

        viewModel.initialise(
            activityViewModel = activityViewModel,
            navigateToLanguageInfo = { action ->
                findNavController().navigate(action)
            }
        )

        binding.viewModel = viewModel
    }
}
