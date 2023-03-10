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

package `in`.digistorm.aksharam.activities.main

import `in`.digistorm.aksharam.activities.main.models.AksharamViewModel
import `in`.digistorm.aksharam.activities.main.models.SettingsViewModel
import `in`.digistorm.aksharam.databinding.FragmentSettingsBinding
import `in`.digistorm.aksharam.util.logDebug
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import `in`.digistorm.aksharam.R
import kotlinx.coroutines.*

class SettingsFragment: Fragment() {
    private val logTag = javaClass.simpleName
    private lateinit var binding: FragmentSettingsBinding
    private val aksharamViewModel: AksharamViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.swipeRefreshLayout.isRefreshing = true
        binding.languageList.adapter = LanguageListAdapter()

        binding.swipeRefreshLayout.setOnRefreshListener {
            settingsViewModel.fetchLanguageFiles(requireContext()).invokeOnCompletion(updateViewsAfterFileListChanged())
        }

        settingsViewModel.fetchLanguageFiles(requireContext()).invokeOnCompletion(updateViewsAfterFileListChanged())
        settingsViewModel.languageFiles.observe(viewLifecycleOwner) {
            (binding.languageList.adapter as? LanguageListAdapter)?.apply {
                submitList(it)
            }
        }

        val navController = findNavController()
        binding.toolbar.setupWithNavController(
            navController,
            AppBarConfiguration(setOf(R.id.tabbedViewsFragment, R.id.initialisationScreen))
        )
    }

    private fun updateViewsAfterFileListChanged(): CompletionHandler {
        return {
            logDebug(logTag, "Language list:\n ${settingsViewModel.languageFiles.value!!}")
            when(it) {
                null -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.languageList.visibility = View.VISIBLE
                }
                is CancellationException -> {
                    logDebug(logTag, "Language file list generation cancelled!")
                }
                else -> {
                    throw it
                }
            }
        }
    }
}