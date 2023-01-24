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
package `in`.digistorm.aksharam.activities.main.practice

import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.activities.main.MainActivity
import `in`.digistorm.aksharam.util.*

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*
import androidx.lifecycle.Observer

class PracticeTabFragment : Fragment() {
    private val logTag = javaClass.simpleName

    private val viewModel: PracticeTabViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.practice_tab_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        logDebug(logTag, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        initialisePracticeTabLangSpinner(view)

        /* When "Language" is updated:
           1. Reset the transliterator
           2. Disable input edit text
           3. Clear input edit text
           4. Re initialise Practice In spinner
           5. Re initialise Practice Type spinner
           6. Enable input edit text
         */
        viewModel.language.observe(viewLifecycleOwner) {
            logDebug(logTag, "viewModel.language changed to ${viewModel.language.value}")
            // re-initialise the "practice in" spinner
            viewModel.setTransliterator(viewModel.language.value!!, requireContext())
            requireActivity().findViewById<View>(R.id.PracticeTabInputTIET).isEnabled = false
            clearInput()
            initialisePracticeTabPracticeInSpinner()
            initialisePracticeTabPracticeTypeSpinner()
            requireActivity().findViewById<View>(R.id.PracticeTabInputTIET).isEnabled = true
            view.findViewById<ImageView>(R.id.PracticeTabSuccessCheck)
                .visibility = View.INVISIBLE
        }

        // When "Practice In" language is updated, update the corresponding hint in EditText
        viewModel.practiceIn.observe(viewLifecycleOwner) {
            logDebug(logTag, "viewModel.practiceIn changed to ${viewModel.practiceIn.value}")
            clearInput()
            (requireActivity().findViewById<View>(R.id.PracticeTabInputTIL) as TextInputLayout).hint =
                getString(R.string.practice_tab_practice_input_hint, viewModel.practiceIn.value)
            if(viewModel.practiceString.value != null) {
                viewModel.transliteratedString.value = viewModel.transliterator
                    .transliterate(viewModel.practiceString.value!!, viewModel.practiceIn.value!!)
                view.findViewById<ImageView>(R.id.PracticeTabSuccessCheck)
                    .visibility = View.INVISIBLE
            }
        }

        // When practice type is updated, create new practice string
        viewModel.practiceType.observe(viewLifecycleOwner) {
            logDebug(logTag, "viewModel.practiceType changed to ${viewModel.practiceType.value}")
            requireActivity().findViewById<View>(R.id.PracticeTabInputTIET).isEnabled = true
            view.findViewById<ImageView>(R.id.PracticeTabSuccessCheck)
                .visibility = View.INVISIBLE
            viewModel.practiceString.value = generatePracticeString(viewModel)
        }

        // When practice string is updated, display it and re-calculate the transliterated string
        val practiceStringTextViewUpdater = Observer<String> {
            logDebug(logTag, "viewModel.practiceString changed to ${viewModel.practiceString.value}")
            val practiceTabTextView: TextView = view.findViewById(R.id.PracticeTabPracticeTextTV)
            practiceTabTextView.text = it
            viewModel.transliteratedString.value = viewModel.transliterator.transliterate(it,
                viewModel.practiceIn.value!!)
        }
        viewModel.practiceString.observe(viewLifecycleOwner, practiceStringTextViewUpdater)

        // When transliterated string is updated, clear the input edit text
        viewModel.transliteratedString.observe(viewLifecycleOwner) {
            logDebug(logTag, "viewModel.transliteratedString changed to ${viewModel.transliteratedString.value}")
            clearInput()
        }

        // When refresh button is clicked, generate a new practice string
        view.findViewById<View>(R.id.PracticeTabRefreshButton).setOnClickListener {
            viewModel.practiceString.value = generatePracticeString(viewModel)
            view.findViewById<View>(R.id.PracticeTabInputTIET).isEnabled = true
            view.findViewById<ImageView>(R.id.PracticeTabSuccessCheck)
                .visibility = View.INVISIBLE
        }

        // Display a success message and a check mark when user input matches transliterated string
        viewModel.practiceSuccessCheck.observe(viewLifecycleOwner) {
            logDebug(logTag, "viewModel.practiceSuccessCheck changed to ${viewModel.practiceSuccessCheck.value}")
            val successCheck: Boolean = viewModel.practiceSuccessCheck.value ?: false
            if(successCheck) {
                view.findViewById<View>(R.id.PracticeTabInputTIET)
                    .isEnabled = false
                view.findViewById<ImageView>(R.id.PracticeTabSuccessCheck)
                    .visibility = View.VISIBLE
                Toast.makeText(
                    requireContext(),
                    R.string.practice_tab_correct_text_entered,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Connect the input EditText with its listener
        val textChangedListener = InputTextChangedListener(this)
        view.findViewById<TextInputEditText>(R.id.PracticeTabInputTIET)
            .addTextChangedListener(textChangedListener)
    }

    // Start Initialisation activity if we could not find any downloaded languages.
    private fun getAllDownloadedLanguages(): ArrayList<String> {
        val languages: ArrayList<String> = getDownloadedLanguages(requireContext())
        if (languages.size == 0) {
            (requireActivity() as MainActivity).startInitialisationActivity()
            return ArrayList()
        }
        return languages
    }

    private fun initialisePracticeTabLangSpinner(view: View) {
        logDebug(logTag, "Initialising PracticeTabLangSpinner")
        val practiceTabLangSpinner: Spinner = view.findViewById(R.id.PracticeTabLangSpinner)

        val adapter: LabelledArrayAdapter<String> = LabelledArrayAdapter<String>(
            requireContext(),
            R.layout.spinner_item,
            R.id.spinnerItemTV,
            getAllDownloadedLanguages(),
            R.id.spinnerLabelTV, getString(R.string.practice_tab_lang_hint)
        )
        adapter.setDropDownViewResource(R.layout.spinner_drop_down)
        adapter.setNotifyOnChange(true)
        practiceTabLangSpinner.adapter = adapter
        practiceTabLangSpinner.setSelection(0)

        GlobalSettings.instance?.addDataFileListChangedListener("PracticeTabFragmentListener",
            object: DataFileListChanged {
                override fun onDataFileListChanged() {
                    logDebug("PTFListener", "Change in data files detected. Updating adapter.")
                    if (context == null)
                        return
                    adapter.clear()
                    adapter.addAll(getAllDownloadedLanguages())
                    // While the spinner shows updated text, its (Spinner's) getSelectedView() was sometimes returning
                    // a non-existent item (say, if the item is deleted). Resetting the adapter was the only way I could
                    // think of to fix this
                    logDebug("PTFListener", "Resetting spinner adapter")
                    practiceTabLangSpinner.adapter = adapter
                }
            })

        logDebug(logTag, "Setting up item selected listener for the \"Language\" selectiion spinner")
        practiceTabLangSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                logDebug(logTag, "onItemSelected invoked by $parent")
                viewModel.language.value = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    fun clearInput() {
        logDebug(logTag, "Clearing input edit text")
        val textInputEditText =
            requireActivity().findViewById<TextInputEditText>(R.id.PracticeTabInputTIET)
                ?: return
        // TODO: validate removal of commented lines
        // textInputEditText.removeTextChangedListener(textChangedListener)
        textInputEditText.setText("")
        // textInputEditText.addTextChangedListener(textChangedListener)
    }

    private fun initialisePracticeTabPracticeInSpinner() {
        clearInput()

        logDebug(logTag, "Initialising \"Practice In\" spinner.")
        val practiceTabPracticeInSpinner = requireActivity().findViewById<Spinner>(
            R.id.PracticeTabPracticeInSpinner)
        val practiceInAdapter: LabelledArrayAdapter<String> = LabelledArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            R.id.spinnerItemTV,
            viewModel.transliterator.languageData.supportedLanguagesForTransliteration,
            R.id.spinnerLabelTV, getString(R.string.practice_tab_practice_in_hint)
        )
        practiceInAdapter.setDropDownViewResource(R.layout.spinner_drop_down)
        practiceTabPracticeInSpinner.adapter = practiceInAdapter
        practiceTabPracticeInSpinner.setSelection(0)
        practiceTabPracticeInSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    clearInput()
                    viewModel.practiceIn.value = parent?.getItemAtPosition(position).toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun initialisePracticeTabPracticeTypeSpinner() {

        logDebug(logTag, "Initialising \"Practice Type\" spinner.")
        val practiceTabPracticeTypeSpinner: Spinner = requireActivity().findViewById(
            R.id.PracticeTabPracticeTypeSpinner)

        val practiceTypes = ArrayList<String>()
        val categories: Set<String> =
            viewModel.transliterator.languageData.lettersCategoryWise.keys
        for (category in categories) {
            practiceTypes.add(
                category.substring(0, 1).uppercase()
                        + category.substring(1)
            )
        }

        // Additional practice types
        // Random ligatures work best in some languages like Kannada where each consonant can form
        // a unique conjunct with another consonant. Other languages like Malayalam or Hindi
        // have a few ligatures, yet this is true only for commonly occurring consonant combinations
        // Most of the combinations in these languages do not result in a meaningful ligature, or are
        // easily understood (as in the case of Hindi). So, we will add random ligatures only if the
        // language's data file says we should.
        if (viewModel.transliterator.languageData.areLigaturesAutoGeneratable()) practiceTypes.add(
            "Random Ligatures"
        )
        practiceTypes.add("Random Words")
        val practiceTypeAdapter: LabelledArrayAdapter<String> = LabelledArrayAdapter<String>(
            requireContext(),
            R.layout.spinner_item,
            R.id.spinnerItemTV,
            practiceTypes,
            R.id.spinnerLabelTV, getString(R.string.practice_tab_practice_type_hint)
        )
        practiceTypeAdapter.setDropDownViewResource(R.layout.spinner_drop_down)
        practiceTabPracticeTypeSpinner.adapter = practiceTypeAdapter
        practiceTabPracticeTypeSpinner.setSelection(0)
        practiceTabPracticeTypeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    clearInput()
                    viewModel.practiceType.value = parent?.getItemAtPosition(position).toString()
                    viewModel.practiceString.value = generatePracticeString(viewModel)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

}
