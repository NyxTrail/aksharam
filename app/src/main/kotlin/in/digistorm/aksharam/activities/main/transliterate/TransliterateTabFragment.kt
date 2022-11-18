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

package `in`.digistorm.aksharam.activities.main.transliterate

import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.activities.main.letters.LettersTabViewModel
import `in`.digistorm.aksharam.util.*

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.widget.EditText
import android.text.TextWatcher
import android.text.Editable
import android.widget.TextView
import android.app.Activity
import android.text.Html
import android.view.View
import android.widget.Spinner
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class TransliterateTabFragment : Fragment() {
    private val logTag = javaClass.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.transliteration_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logDebug(logTag, "onViewCreated")

        val viewModel: TransliterateTabViewModel = ViewModelProvider(requireActivity())[
                TransliterateTabViewModel::class.java]

        initialiseSpinner()

        // Live transliteration as each new character is entered into the input text box
        (view.findViewById<View>(R.id.TransliterateTabInputTextField) as EditText)
            .addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    transliterate(s.toString(), view, viewModel)
                }
            })

        GlobalSettings.instance?.addDataFileListChangedListener(
            "TransliterateTabFragmentListener",
            object: DataFileListChanged {
                override fun onDataFileListChanged() {
                    logDebug(logTag, "Re-initialising spinners")
                    initialiseSpinner()
                }
            })
    }

    private fun transliterate(input: String, view: View?, viewModel: TransliterateTabViewModel) {
        logDebug(logTag, "Transliterating $input to ${viewModel.targetLanguage}")
        if (input.isEmpty() || view == null) return

        val inputLanguage = detectLanguage(input)
        logDebug(logTag, "Language detected: $inputLanguage")

        if (inputLanguage != null && !viewModel.transliterator.isTransliteratingLanguage(inputLanguage)) {
            viewModel.transliterator.setInputLanguage(inputLanguage, requireContext())
            // re-initialise the spinner so that only languages that can be transliterated to is listed
            initialiseSpinner()
        }

        // Now we are ready to transliterate
        logDebug(logTag, "Transliterating to $viewModel.targetLanguage.")
        val outputString = viewModel.transliterator.transliterate(input, viewModel.targetLanguage)
        setText(view, R.id.TransliterateTabOutputTextView, outputString)
    }

    // v: view to run findViewById on
    // id: id of TextView to find
    // text: text to set
    private fun setText(v: View?, id: Int, text: CharSequence) {
        if (v == null) return
        val tv = v.findViewById<TextView>(id) ?: return
        tv.text = text
    }

    // v: view to run findViewById on
    // id: id of TextView to find
    // text: text to set
    private fun setText(activity: Activity?, id: Int, text: CharSequence) {
        if (activity == null) return
        val tv = activity.findViewById<TextView>(id) ?: return
        tv.text = text
    }

    private fun detectLanguage(inputString: String): String? {
        val lang: String? = LanguageDetector(context).detectLanguage(inputString, context)
        logDebug(logTag, "Detected language: $lang")
        if (lang == null) {
            logDebug(logTag, getString(R.string.lang_could_not_detect))
            if (activity == null) return null
            setText(
                activity, R.id.TransliterateTabInfoTV,
                Html.fromHtml(getText(R.string.lang_could_not_detect).toString())
            )
            return null
        }
        setText(
            activity,
            R.id.TransliterateTabInfoTV,
            getText(R.string.transliterate_tab_info_default)
        )
        return lang
    }

    private fun setTargetLanguage(viewModel: TransliterateTabViewModel,
                                  languageSelectionSpinner: Spinner,
                                  adapter: LabelledArrayAdapter<String>) {
        if(viewModel.targetLanguage.isEmpty()) {
            logDebug(logTag, "Target language not set in view model. Selecting item 0...")
            languageSelectionSpinner.setSelection(0)
            viewModel.targetLanguage = languageSelectionSpinner.selectedItem.toString()
            logDebug(logTag, "Item 0 selected was ${viewModel.targetLanguage}")
        } else { // TODO: When will this branch be entered
            if (adapter.getPosition(viewModel.targetLanguage) == -1) {
                logDebug(logTag, "Target language not found in adapter. Selecting item 0...")
                languageSelectionSpinner.setSelection(0)
                viewModel.targetLanguage = languageSelectionSpinner.selectedItem.toString()
                logDebug(logTag, "Item 0 selected was ${viewModel.targetLanguage}")
            }
            else
                languageSelectionSpinner.setSelection(adapter.getPosition(viewModel.targetLanguage))
        }
    }

    fun initialiseSpinner() {
        logDebug(logTag, "Transliterate tab spinner initialising...")
        val languageSelectionSpinner: Spinner = requireView().findViewById(R.id.LanguageSelectionSpinner)

        val viewModel: TransliterateTabViewModel = ViewModelProvider(requireActivity())[
                TransliterateTabViewModel::class.java]

        val adapter: LabelledArrayAdapter<String> = LabelledArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            R.id.spinnerItemTV,
            viewModel.getLanguageData().supportedLanguagesForTransliteration,
            R.id.spinnerLabelTV, getString(R.string.transliterate_tab_trans_hint)
        )
        adapter.setDropDownViewResource(R.layout.spinner_drop_down)
        languageSelectionSpinner.adapter = adapter

        setTargetLanguage(viewModel, languageSelectionSpinner, adapter)

        languageSelectionSpinner.onItemSelectedListener =
            object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.targetLanguage = parent?.getItemAtPosition(position).toString()
                    val editText =
                        requireActivity().findViewById<EditText>(R.id.TransliterateTabInputTextField)
                            ?: return
                    transliterate(
                        editText.text.toString(),
                        requireActivity().findViewById(R.id.TransliterateTabOutputTextView),
                        viewModel)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }
}