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

import android.content.Context
import android.graphics.Point
import android.widget.ExpandableListView
import android.widget.Spinner
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import android.widget.ScrollView
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import java.util.ArrayList
import kotlin.math.log

class LettersTabFragment : Fragment {
    private val logTag = javaClass.simpleName

    // This will hold the id for the ExpandableListView for easily finding it later
    private var expandableListViewId = -1

    // Default constructor, used by PageCollectionAdapter to initialise the
    // fragment. The Fragment's children views are created via its onCreate
    // methods.
    constructor(): super() {}

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
        // TODO: initialise a specific language based on last user selection
        val viewModel: LettersTabViewModel = ViewModelProvider(requireActivity())[LettersTabViewModel::class.java]

        logDebug(logTag, "Letters category wise: ${viewModel.transliterator.languageData.lettersCategoryWise}")
        initialiseLettersTabLangSpinner(view)

        // set up the info button
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
        expandableListView.id = View.generateViewId()
        expandableListViewId = expandableListView.id
        expandableListView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        val size = Point()
        requireActivity().windowManager.defaultDisplay.getSize(size)
        expandableListView.setAdapter(LetterCategoryAdapter(viewModel))
        val sv = view.findViewById<ScrollView>(R.id.LettersView)
        sv.addView(expandableListView)
        for (i in 0 until expandableListView.expandableListAdapter.groupCount) {
            expandableListView.expandGroup(i)
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

        val viewModel: LettersTabViewModel = ViewModelProvider(requireActivity())[LettersTabViewModel::class.java]
        viewModel.adapter = adapter
        adapter.setDropDownViewResource(R.layout.spinner_drop_down)
        adapter.setNotifyOnChange(true)
        lettersTabLangSpinner.adapter = adapter
        // TODO: Can this be simplified? See PracticeTabFragment
        if (adapter.getPosition(viewModel.targetLanguage) != -1)
            lettersTabLangSpinner.setSelection(adapter.getPosition(viewModel.targetLanguage))
        else
            lettersTabLangSpinner.setSelection(0)

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
                view: View?,
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
                // At this point, langDataReader should be re-initialised
                initialiseLettersTabTransSpinner()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setTargetLanguage(viewModel: LettersTabViewModel,
                                  spinner: Spinner,
                                  adapter: LabelledArrayAdapter<String>) {
        if(viewModel.targetLanguage.isEmpty()) {
            logDebug(logTag, "Target language not set in view model. Selecting item 0...")
            spinner.setSelection(0)
            viewModel.targetLanguage = spinner.selectedItem.toString()
            logDebug(logTag, "Item 0 selected was ${viewModel.targetLanguage}")
        } else { // TODO: When will this branch be entered
            if (adapter.getPosition(viewModel.targetLanguage) == -1) {
                logDebug(logTag, "Target language not found in adapter. Selecting item 0...")
                spinner.setSelection(0)
                viewModel.targetLanguage = spinner.selectedItem.toString()
                logDebug(logTag, "Item 0 selected was ${viewModel.targetLanguage}")
            }
            else
                spinner.setSelection(adapter.getPosition(viewModel.targetLanguage))
        }
    }

    fun initialiseLettersTabTransSpinner() {
        logDebug(logTag, "Initialising \"Convert To\" spinner.")
        val lettersTabTransSpinner: Spinner = requireActivity().findViewById(R.id.lettersTabTransSpinner)
        val viewModel: LettersTabViewModel = ViewModelProvider(requireActivity())[
                LettersTabViewModel::class.java]

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
    }
}
