/*
 * Copyright (c) 2023 Alan M Varghese <alan@digistorm.in>
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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import `in`.digistorm.aksharam.activities.main.ActivityViewModel
import `in`.digistorm.aksharam.activities.main.util.logDebug
import `in`.digistorm.aksharam.databinding.FragmentLettersBinding

class LettersFragment: Fragment() {
    private val logTag = javaClass.simpleName

    private lateinit var binding: FragmentLettersBinding
    private val activityViewModel: ActivityViewModel by activityViewModels()
    private val viewModel: LettersViewModel by activityViewModels {
        object: ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return (LettersViewModel(
                    application = requireActivity().application,
                    activityViewModel = activityViewModel
                ) as? T) ?: throw Exception("ViewModelProvider.Factory: LettersTabViewModel could not be created.")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLettersBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        logDebug(logTag, "onViewCreated")

        postponeEnterTransition()

        viewModel.initialise(
            navigateToLanguageInfo = { action ->
                 findNavController().navigate(action)
            }
        )

        binding.viewModel = viewModel

        binding.letterCategories.doOnPreDraw {
            logDebug(logTag, "Running pre-draw")
            logDebug(logTag, "Starting postponed enter transition")
            startPostponedEnterTransition()
            logDebug(logTag, "Removing preDraw listener")
        }
    }
}