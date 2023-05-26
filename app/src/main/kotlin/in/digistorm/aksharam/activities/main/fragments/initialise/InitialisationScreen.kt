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

package `in`.digistorm.aksharam.activities.main.fragments.initialise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.activities.main.ActivityViewModel
import `in`.digistorm.aksharam.activities.main.language.getDownloadedLanguages
import `in`.digistorm.aksharam.activities.main.util.IdlingResourceHelper
import `in`.digistorm.aksharam.activities.main.util.downloadFile
import `in`.digistorm.aksharam.activities.main.util.logDebug
import `in`.digistorm.aksharam.databinding.FragmentInitialisationScreenBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class InitialisationScreen : Fragment() {
    private val logTag = javaClass.simpleName

    private val viewModel: InitialisationScreenViewModel by viewModels()
    private val activityViewModel: ActivityViewModel by activityViewModels()
    private lateinit var binding: FragmentInitialisationScreenBinding

    private var alertDialog: AlertDialog? = null

    override fun onDestroy() {
        super.onDestroy()
        logDebug(logTag, "Destroying...")
        if(alertDialog?.isShowing == true) alertDialog?.dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInitialisationScreenBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logDebug(logTag, "onViewCreated")

        binding.viewModel = viewModel

        if(getDownloadedLanguages(requireContext()).isNotEmpty())
            findNavController().navigate(InitialisationScreenDirections.actionInitialisationScreenToTabbedViewsFragment())
        else {
            viewModel.onlineFiles.observe(viewLifecycleOwner) { list ->
                if(list == null) {
                    logDebug(logTag, "List is null")
                    viewModel.progressBarStatus.value = View.GONE
                    showFailedDialog {
                        viewModel.progressBarStatus.value = View.VISIBLE
                        viewModel.initialise()
                    }
                } else {
                    viewModel.progressBarStatus.value = View.GONE
                    binding.fileList.adapter = LanguageDataFileListAdapter(
                        dataFileList = list,
                        addLanguageToDownload = { languageFile ->
                            viewModel.languagesToDownload.add(languageFile)
                            binding.proceedButton.isEnabled = true
                        },
                        removeLanguageToDownload = { languageFile ->
                            viewModel.languagesToDownload.remove(languageFile)
                            if (viewModel.languagesToDownload.size < 1)
                                binding.proceedButton.isEnabled = false
                        }
                    )
                }
            }
        }

        binding.proceedButton.setOnClickListener {
            IdlingResourceHelper.countingIdlingResource.increment()
            viewModel.progressBarStatus.postValue(View.VISIBLE)
            binding.proceedButton.isEnabled = false
            downloadFiles()
        }

        val navController = findNavController()
        binding.toolbar.setupWithNavController(
            navController,
            AppBarConfiguration(setOf(R.id.initialisationScreen))
        )
    }

    private fun showFailedDialog(retryLogic: () -> Unit) {
        if(alertDialog?.isShowing == true) alertDialog?.dismiss()

        alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setNegativeButton(getString(R.string.exit)) { dialogInterface, _ ->
                dialogInterface.dismiss()
                requireActivity().finish()
            }
            .setPositiveButton(getString(R.string.try_again)) { dialogInterface, _ ->
                dialogInterface.dismiss()
                // Wait a few milliseconds before retrying
                runBlocking { delay(500) }
                retryLogic()
                // viewModel.initialise()
            }
            .setMessage(getString(R.string.could_not_connect))
            .create()
        alertDialog?.show()
    }

    private fun downloadFiles() {
        viewModel.progressBarStatus.postValue(View.VISIBLE)
        lifecycleScope.launch {
            try {
                if (viewModel.languagesToDownload.size > 0) {
                    for (languageFile in viewModel.languagesToDownload) {
                        downloadFile(languageFile, requireContext())
                    }
                }
                activityViewModel.availableLanguages.value =
                    getDownloadedLanguages(requireContext())
            }
            catch (e: Exception) {
                logDebug(logTag, "${e.stackTrace}")
                binding.progressBar.visibility = View.GONE
                showFailedDialog {
                    downloadFiles()
                }
            }
        }
    }
}