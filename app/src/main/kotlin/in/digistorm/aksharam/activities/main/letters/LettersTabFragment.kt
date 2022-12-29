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
import `in`.digistorm.aksharam.activities.main.MainActivity
import `in`.digistorm.aksharam.util.*

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

class LettersTabFragment: Fragment() {
    private val logTag = javaClass.simpleName

    private val viewModel: LettersTabViewModel by viewModels()
    private var letterCategoryAdapter: LetterCategoryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.letters_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logDebug(logTag, "onViewCreated")

        logDebug(logTag, "Creating/Getting View Model for LettersTabFragment")

        logDebug(logTag, "Letters category wise: ${viewModel.transliterator.languageData.lettersCategoryWise}")
        initialiseLanguageSelector(view)
        initialiseLettersTabTransSpinner(view)

        // Set up the info button
        view.findViewById<View>(R.id.lettersTabInfoButton).setOnClickListener { v: View? ->
            logDebug(logTag, "Info button clicked!")
            logDebug(logTag,
                "Fetching info for transliterating ${viewModel.transliterator.languageData.language} to ${viewModel.targetLanguage}")
            val info: HashMap<String, Map<String, String>> = viewModel.transliterator.languageData.info
            logDebug(logTag, "Data for info: $info")
            val lif = LanguageInfoFragment.newInstance(
                info["general"]?.get("en") + info[viewModel.targetLanguage.lowercase()]?.get("en"))
            MainActivity.replaceTabFragment(0, lif)
        }

        val expandableListView = view.findViewById<ExpandableListView>(R.id.letter_list)
        expandableListView.apply {
            letterCategoryAdapter = LetterCategoryAdapter(viewModel)
            setAdapter(letterCategoryAdapter)

            for (i in 0 until expandableListAdapter.groupCount) {
                expandGroup(i)
            }
        }
        initObservers(view)
        view.findViewById<LinearProgressIndicator>(R.id.progress_indicator).visibility = View.GONE
    }

    // Return an empty array list if we could not find any
    // downloaded files. Should not be a problem since we
    // are anyways exiting this activity.
    private fun getAllDownloadedLanguages(): ArrayList<String> {
        val languages: ArrayList<String> = getDownloadedLanguages(requireContext())
        if (languages.size == 0) {
            (requireActivity() as MainActivity).startInitialisationAcitivity()
            return ArrayList()
        }
        return languages
    }

    private fun initialiseLanguageSelector(view: View) {
        logDebug(logTag, "Initialising LettersTabLangSpinner")
        val languageSelector: TextInputLayout = view.findViewById(R.id.language_selector)
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            R.layout.drop_down_item,
            getAllDownloadedLanguages(),
        )
        adapter.setNotifyOnChange(true)

        val autoCompleteTextView = languageSelector.editText as? MaterialAutoCompleteTextView
        autoCompleteTextView?.setAdapter(adapter)
        val upperCased = viewModel.language.replaceFirstChar {
            if (it.isLowerCase())
                it.titlecase(Locale.getDefault())
            else
                it.toString()
        }
        logDebug(logTag, "Setting current selection to $upperCased")
        logDebug(logTag, "Its adapter position is ${adapter.getPosition(upperCased)}")
        autoCompleteTextView?.setText(upperCased, false)
        // Update view model when language selection changes
        autoCompleteTextView?.onItemClickListener = AdapterView.OnItemClickListener(
            fun (parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                logDebug(logTag, "LanguageSelector: ${adapter.getItem(position)} clicked.")
                viewModel.language = adapter.getItem(position)!!
            }
        )

        /* TODO: Verify and re-work language list under this drop down when one or more languages
           have been deleted/downloaded */
        GlobalSettings.instance?.addDataFileListChangedListener("LettersTabFragmentListener",
            object: DataFileListChanged {
                override fun onDataFileListChanged() {
                    logDebug("LTFListener", "Refreshing LettersTabFragment adapter")
                    if (context == null)
                        return
                    adapter.clear()
                    adapter.addAll(getAllDownloadedLanguages())
                    // adapter.notifyDataSetChanged();
                    // While the spinner shows updated text, its (Spinner's) getSelectedView() was sometimes returning
                    // a non-existant item (say, if the item is deleted). Resetting the adapter was the only way I could
                    // think of to fix this
                    logDebug("LTFListener", "Resetting spinner adapter")
                    // lettersTabLangSpinner.adapter = adapter
                }
            })
    }

    private fun initialiseLettersTabTransSpinner(view: View) {
        logDebug(logTag, "Initialising \"Convert To\" spinner.")
        val convertToSelector: TextInputLayout = view.findViewById(R.id.convert_to_selector)
        val transliterationLanguages = viewModel.getLanguageData().supportedLanguagesForTransliteration
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            R.layout.drop_down_item,
            transliterationLanguages,
        )
        adapter.setNotifyOnChange(true)
        val autoCompleteTextView = convertToSelector.editText as? MaterialAutoCompleteTextView
        autoCompleteTextView?.setAdapter(adapter)

        viewModel.targetLanguage = transliterationLanguages[0]
        logDebug(logTag, "Set initial target language to: ${viewModel.targetLanguage}")
        autoCompleteTextView?.setText(viewModel.targetLanguage, false)

        autoCompleteTextView?.onItemClickListener = AdapterView.OnItemClickListener(
            fun (parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                logDebug(logTag, "ConvertToSelector: ${adapter.getItem(position)} clicked.")
                viewModel.targetLanguage = adapter.getItem(position)!!
            }
        )
    }

    private fun initObservers(view: View) {
        // When language is updated, update the entire grid
        viewModel.languageLiveData.observe(viewLifecycleOwner) {
            logDebug(logTag, "languageObserver: change detected")
            val linearProgressIndicator: LinearProgressIndicator? = view.findViewById(R.id.progress_indicator)
            linearProgressIndicator?.visibility = View.VISIBLE
            viewModel.setTransliterator(viewModel.language, requireContext())
            val expandableListView: ExpandableListView? = view.findViewById(R.id.letter_list)
            if(expandableListView != null) {
                expandableListView.setAdapter(LetterCategoryAdapter(viewModel))
                for (i in 0 until expandableListView.expandableListAdapter.groupCount) {
                    expandableListView.expandGroup(i)
                }
                initialiseLettersTabTransSpinner(view)
            }
            else
                logDebug(logTag, "Could not find ExpandableListView")
            linearProgressIndicator?.visibility = View.GONE
        }

        // When targetLanguage is updated, update transliterated letter
        viewModel.targetLanguageLiveData.observe(viewLifecycleOwner) {
            logDebug(logTag, "targetLanguageObserver: change detected")
            val expandableListView: ExpandableListView? = activity?.findViewById(R.id.letter_list)
            if(expandableListView != null) {
                val lettersCategoryWise: LinkedHashMap<String, ArrayList<String>> =
                    viewModel.transliterator.languageData.lettersCategoryWise
                for (letters: ArrayList<String> in lettersCategoryWise.values) {
                    for (letter in letters) {
                        val letterView = expandableListView.findViewWithTag<LetterView>(letter)
                        if (letterView != null) {
                            logDebug(logTag, "Found LetterView with tag: $letter.")
                            letterView.transliteratedLetter =
                                viewModel.transliterator.transliterate(
                                    letter,
                                    viewModel.targetLanguage
                                )
                            // Update the current displayed string if necessary
                            if (letterView.text != letterView.letter)
                                letterView.text = letterView.transliteratedLetter
                        } else {
                            logDebug(logTag, "Could not find LetterView with tag: $letter.")
                        }
                    }
                }
            }
            else
                logDebug(logTag, "Could not find ExpandableListView")
        }
    }
}
