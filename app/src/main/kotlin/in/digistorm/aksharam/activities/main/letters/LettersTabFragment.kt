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

import android.widget.ExpandableListView
import android.widget.Spinner
import android.os.Bundle
import android.view.*
import android.widget.ScrollView
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kotlin.collections.ArrayList

class LettersTabFragment: Fragment() {
    private val logTag = javaClass.simpleName

    private val viewModel: LettersTabViewModel by viewModels()
    private var letterCategoryAdapter: LetterCategoryAdapter? = null

    // This will hold the id for the ExpandableListView for easily finding it later
    private var expandableListViewId = -1

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
        initialiseLettersTabLangSpinner(view)
        initialiseLettersTabTransSpinner(view)

        // Set up the info button
        view.findViewById<View>(R.id.lettersTabInfoButton).setOnClickListener { v: View? ->
            logDebug(logTag, "Info button clicked!")
            logDebug(logTag,
                "Fetching info for transliterating ${viewModel.getLanguage()} to ${viewModel.targetLanguage}")
            val info: HashMap<String, Map<String, String>> = viewModel.transliterator.languageData.info
            logDebug(logTag, "Data for info: $info")
            val lif = LanguageInfoFragment.newInstance(
                info["general"]?.get("en") + info[viewModel.targetLanguage.lowercase()]?.get("en"))
            MainActivity.replaceTabFragment(0, lif)
        }

        val expandableListView = ExpandableListView(requireContext())
        expandableListView.apply {
            id = View.generateViewId()
            expandableListViewId = expandableListView.id
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            letterCategoryAdapter = LetterCategoryAdapter(viewModel)
            setAdapter(letterCategoryAdapter)

            val sv = view.findViewById<ScrollView>(R.id.LettersView)
            sv.addView(expandableListView)

            for (i in 0 until expandableListAdapter.groupCount) {
                expandGroup(i)
            }
        }
        initObservers()
    }

    private fun initObservers() {
        // When targetLanguage is updated, update transliterated letter
        viewModel.targetLanguageLiveData.observe(viewLifecycleOwner) {
            logDebug(logTag, "targetLanguageObserver: change detected")
            val expandableListView = activity?.findViewById<ExpandableListView>(expandableListViewId)
            val lettersCategoryWise: LinkedHashMap<String, ArrayList<String>> =
                viewModel.transliterator.languageData.lettersCategoryWise
            for(letters: ArrayList<String> in lettersCategoryWise.values) {
                for(letter in letters) {
                    val letterView = expandableListView?.findViewWithTag<LetterView>(letter)
                    if(letterView != null) {
                        logDebug(logTag, "Found LetterView with tag: $letter.")
                        letterView.transliteratedLetter = viewModel.transliterator.transliterate(
                            letter,
                            viewModel.targetLanguage
                        )
                        // Update the current displayed string if necessary
                        if(letterView.text != letterView.letter)
                            letterView.text = letterView.transliteratedLetter
                    } else {
                        logDebug(logTag, "Could not find LetterView with tag: $letter.")
                    }
                }
            }
        }
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

    private fun initialiseLettersTabLangSpinner(view: View) {
        logDebug(logTag, "Initialising LettersTabLangSpinner")
        val lettersTabLangSpinner: Spinner = view.findViewById(R.id.lettersTabLangSpinner)
        val adapter: LabelledArrayAdapter<String> = LabelledArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            R.id.spinnerItemTV,
            getAllDownloadedLanguages(),
            R.id.spinnerLabelTV, getString(R.string.letters_tab_lang_input_hint)
        )

        viewModel.adapter = adapter
        adapter.setDropDownViewResource(R.layout.spinner_drop_down)
        adapter.setNotifyOnChange(true)
        lettersTabLangSpinner.adapter = adapter

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
                    lettersTabLangSpinner.adapter = adapter
                }
            })
        logDebug(logTag, "Setting up item listener for the language selection spinner")

        lettersTabLangSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                mView: View?,
                position: Int,
                id: Long
            ) {
                val language = parent?.getItemAtPosition(position).toString()
                logDebug("LangSpinner", "Item selected $language")
                viewModel.setTransliterator(language, requireContext())
                // ExpandableListView should have an ID set in onViewCreated()
                val expandableListView = requireActivity().findViewById<ExpandableListView>(expandableListViewId)
                expandableListView.setAdapter(LetterCategoryAdapter(viewModel))
                for (i in 0 until expandableListView.expandableListAdapter.groupCount) {
                    expandableListView.expandGroup(i)
                }
                initialiseLettersTabTransSpinner(view)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        lettersTabLangSpinner.setSelection(0)
    }

    private fun setTargetLanguage(viewModel: LettersTabViewModel,
                                  spinner: Spinner,
                                  adapter: LabelledArrayAdapter<String>) {
        try {
            if (adapter.getPosition(viewModel.targetLanguage) == -1) {
                logDebug(logTag, "Target language not found in adapter. Selecting item 0...")
                spinner.setSelection(0)
                viewModel.targetLanguage = spinner.selectedItem.toString()
                logDebug(logTag, "Item 0 selected was ${viewModel.targetLanguage}")
            } else
                spinner.setSelection(adapter.getPosition(viewModel.targetLanguage))
        } catch (e: NullPointerException) {
            logDebug(logTag, "NPE caught!!")
            logDebug(logTag, "Setting targetLanguage for first time in view model.")
            spinner.setSelection(0)
            viewModel.targetLanguage = spinner.selectedItem.toString()
            logDebug(logTag, "Item 0 selected was ${viewModel.targetLanguage}")
        }
    }

    fun initialiseLettersTabTransSpinner(view: View) {
        logDebug(logTag, "Initialising \"Convert To\" spinner.")
        val lettersTabTransSpinner: Spinner = view.findViewById(R.id.lettersTabTransSpinner)

        val adapter: LabelledArrayAdapter<String> = LabelledArrayAdapter(
            requireContext(),
            R.layout.spinner_item, R.id.spinnerItemTV,
            viewModel.getLanguageData().supportedLanguagesForTransliteration,
            R.id.spinnerLabelTV, getString(R.string.letters_tab_trans_hint)
        )
        adapter.setDropDownViewResource(R.layout.spinner_drop_down)
        lettersTabTransSpinner.adapter = adapter

        setTargetLanguage(viewModel, lettersTabTransSpinner, adapter)

        lettersTabTransSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val targetLanguage = parent?.getItemAtPosition(position).toString()
                    logDebug("TransSpinner", "Item selected: $targetLanguage")
                    viewModel.targetLanguage = targetLanguage
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        lettersTabTransSpinner.setSelection(0)
    }
}
