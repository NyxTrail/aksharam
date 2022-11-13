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

class LettersTabFragment : Fragment {
    private val logTag = LettersTabFragment::class.simpleName

    private var adapter: LabelledArrayAdapter<String>? = null
    // private var lettersTabLangSpinner: Spinner? = null

    // This will hold the id for the ExpandableListView for easily finding it later
    private var expandableListViewId = -1

    // Default constructor, used by PageCollectionAdapter to initialise the
    // fragment. The Fragment's children views are created via its onCreate
    // methods.
    constructor() : super() {}

    // A constructor for LetterInfoFragment for cases when it does not
    // have access to its parent in.digistorm.aksharam.activities.main.letters.LettersTabFragment
    constructor(context: Context?) : super() {}

    val transliterator: Transliterator?
        get() = ViewModelProvider(requireActivity()).get(LettersTabViewModel::class.java).transliterator

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
        val viewModel: LettersTabViewModel = ViewModelProvider(requireActivity())[LettersTabViewModel::class.java]

        adapter = viewModel.adapter
        logDebug(logTag, "Letters category wise: ${viewModel.transliterator.language?.lettersCategoryWise}")
        initialiseLettersTabLangSpinner(view)

        // set up the info button
        view.findViewById<View>(R.id.lettersTabInfoButton).setOnClickListener { v: View? ->
            logDebug(logTag, "Info button clicked!")
            logDebug(logTag,
                "Fetching info for transliterating ${viewModel.getLanguage()} to ${viewModel.targetLanguage}")
            val info: HashMap<String, Map<String, String>>? = viewModel.transliterator.language!!.info
            logDebug(logTag, "Data for info: $info")
            val lif = LanguageInfoFragment.newInstance(
                info?.get("general")?.get("en") + info?.get(viewModel.targetLanguage?.lowercase())?.get("en"))
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
        expandableListView.setAdapter(LetterCategoryAdapter(viewModel, size))
        val sv = view.findViewById<ScrollView>(R.id.LettersView)
        sv.addView(expandableListView)
        for (i in 0 until expandableListView.expandableListAdapter.groupCount) {
            expandableListView.expandGroup(i)
        }
    }

    private fun initialiseLettersTabLangSpinner(view: View) {
        logDebug(logTag, "Initialising LettersTabLangSpinner")
        val lettersTabLangSpinner: Spinner = requireView().findViewById(R.id.lettersTabLangSpinner)
        val languages: ArrayList<String> = getDownloadedLanguages(requireContext())
        if (languages.size == 0) {
            (requireActivity() as MainActivity).startInitialisationAcitivity()
            return
        }
        adapter = LabelledArrayAdapter<String>(
            requireContext(),
            R.layout.spinner_item,
            R.id.spinnerItemTV,
            languages,
            R.id.spinnerLabelTV, getString(R.string.letters_tab_lang_input_hint)
        )

        val viewModel: LettersTabViewModel = ViewModelProvider(requireActivity())
            .get(LettersTabViewModel::class.java)
        viewModel.adapter = adapter
        adapter?.setDropDownViewResource(R.layout.spinner_drop_down)
        adapter?.setNotifyOnChange(true)
        lettersTabLangSpinner.adapter = adapter

        if (viewModel.targetLanguage != null) {
            if (adapter?.getPosition(viewModel.targetLanguage) != -1)
                lettersTabLangSpinner.setSelection(adapter?.getPosition(viewModel.targetLanguage) ?: 0)
            else
                lettersTabLangSpinner.setSelection(0)
        }
        else
            lettersTabLangSpinner.setSelection(0)
        GlobalSettings.instance?.addDataFileListChangedListener("LettersTabFragmentListener",
            object: DataFileListChanged {
                override fun onDataFileListChanged() {
                    logDebug("LTFListener", "Refreshing LettersTabFragment adapter")
                    if (context == null)
                        return
                    adapter?.clear()
                    val lang: ArrayList<String> = getDownloadedLanguages(context!!)
                    if (lang.size == 0) {
                        (requireActivity() as MainActivity).startInitialisationAcitivity()
                        return
                    }
                    adapter?.addAll(lang)
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
                val size = Point()
                requireActivity().windowManager.defaultDisplay.getSize(size)
                // ExpandableListView should have an ID set in onViewCreated()
                val expandableListView = requireActivity().findViewById<ExpandableListView>(expandableListViewId)
                expandableListView.setAdapter(LetterCategoryAdapter(viewModel, size))
                for (i in 0 until expandableListView.expandableListAdapter.groupCount) {
                    expandableListView.expandGroup(i)
                }
                // At this point, langDataReader should be re-initialised
                initialiseLettersTabTransSpinner()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    fun initialiseLettersTabTransSpinner() {
        logDebug(logTag, "Initialising lettersTabTransSpinner")
        val lettersTabTransSpinner =
            requireActivity().findViewById<Spinner>(R.id.lettersTabTransSpinner)
        val viewModel: LettersTabViewModel = ViewModelProvider(requireActivity())
            .get(LettersTabViewModel::class.java)

        val adapter: LabelledArrayAdapter<String> = LabelledArrayAdapter<String>(
            requireContext(),
            R.layout.spinner_item, R.id.spinnerItemTV,
            viewModel.transliterator!!.language!!.supportedLanguagesForTransliteration,
            R.id.spinnerLabelTV, getString(R.string.letters_tab_trans_hint)
        )
        adapter.setDropDownViewResource(R.layout.spinner_drop_down)
        lettersTabTransSpinner.adapter = adapter

        if (viewModel.targetLanguage == null) lettersTabTransSpinner.setSelection(0) else {
            if (adapter.getPosition(viewModel.targetLanguage) == -1) lettersTabTransSpinner.setSelection(
                0
            ) else lettersTabTransSpinner.setSelection(adapter.getPosition(viewModel.targetLanguage))
        }
        lettersTabTransSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val targetLanguage = parent?.getItemAtPosition(position).toString()
                    logDebug("TransSpinner", "item selected: $targetLanguage")
                    viewModel.targetLanguage = targetLanguage
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }
}