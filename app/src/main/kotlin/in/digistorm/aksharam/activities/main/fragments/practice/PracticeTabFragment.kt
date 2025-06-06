/*
 * Copyright (c) 2022-2025 Alan M Varghese <alan@digistorm.in>
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
package `in`.digistorm.aksharam.activities.main.fragments.practice

import android.content.res.ColorStateList
import android.graphics.Color
import `in`.digistorm.aksharam.R

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import `in`.digistorm.aksharam.activities.main.util.logDebug
import `in`.digistorm.aksharam.databinding.FragmentPracticeTabBinding

class PracticeTabFragment : Fragment() {
    private val logTag = javaClass.simpleName

    private lateinit var binding: FragmentPracticeTabBinding
    private val viewModel: PracticeTabViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPracticeTabBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logDebug(logTag, "onViewCreated")

        viewModel.initialise()

        // Assign the data binding's version of the view model
        binding.viewModel = viewModel

        // When refresh button is clicked, generate a new practice string
        binding.refreshButton.setOnClickListener {
            logDebug(logTag, "Refresh button clicked!")
            binding.practiceInputEditText.setText("")
            binding.practiceInputEditText.isEnabled = true
            viewModel.generateNewPracticeString()
        }

        // Display a success message and a check mark when user input matches transliterated string
        viewModel.practiceSuccessCheck.observe(viewLifecycleOwner) {
            val successCheck: Boolean? = viewModel.practiceSuccessCheck.value
            if(successCheck == true) {
                logDebug(logTag, "Showing results and disabling text input.")
                binding.practiceInputEditText.isEnabled = false
                binding.practiceTextInputLayout.setEndIconDrawable(R.drawable.ic_baseline_check_green_24)
                binding.practiceTextInputLayout.setEndIconTintList(ColorStateList.valueOf(Color.GREEN))
                Snackbar.make(
                    binding.practiceTextInputLayout,
                    R.string.practice_tab_correct_text_entered,
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                logDebug(logTag, "Hiding results and enabling text input.")
                binding.practiceInputEditText.isEnabled = true
                binding.practiceTextInputLayout.endIconDrawable = null
            }
        }

        // Connect the input EditText with its listener
        val textChangedListener = InputTextChangedListener(this, viewModel)
        binding.practiceInputEditText.addTextChangedListener(textChangedListener)
    }
}
