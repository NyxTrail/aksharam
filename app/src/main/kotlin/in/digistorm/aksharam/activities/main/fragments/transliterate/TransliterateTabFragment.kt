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
package `in`.digistorm.aksharam.activities.main.fragments.transliterate

import `in`.digistorm.aksharam.util.*

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.text.TextWatcher
import android.text.Editable
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.activities.main.util.logDebug
import `in`.digistorm.aksharam.databinding.FragmentTransliterateBinding

class TransliterateTabFragment : Fragment() {
    private val logTag = javaClass.simpleName

    private lateinit var binding: FragmentTransliterateBinding

    private val viewModel: TransliterateTabViewModel by viewModels {
        object: ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return (TransliterateTabViewModel(
                    application = requireActivity().application,
                ) as? T) ?: throw Exception("ViewModelProvider.Factory: TransliterateTabViewModel could not be created.")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        logDebug(logTag, "onCreateView")
        binding = FragmentTransliterateBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logDebug(logTag, "onViewCreated")

        viewModel.initialise()
        binding.viewModel = viewModel

        viewModel.selectableLanguages.observe(viewLifecycleOwner) { value ->
            if (value.isNotEmpty()) {
                binding.languageSpinner.isEnabled = true
                binding.languageSpinner.setHint(R.string.transliterate_tab_trans_hint)
            } else {
                if((viewModel.currentInput.value?.length ?: 0) > 0)
                    binding.languageSpinner.setHint(R.string.could_not_detect_language)
                else
                    binding.languageSpinner.setHint(R.string.transliterate_tab_info_default)
                binding.languageSpinner.editText?.text = null
                binding.languageSpinner.isEnabled = false
            }
        }

        // Live transliteration as each new character is entered into the input text box
        (binding.inputTextField).addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    viewModel.currentInput.value = s.toString()
                }
        })
    }
}