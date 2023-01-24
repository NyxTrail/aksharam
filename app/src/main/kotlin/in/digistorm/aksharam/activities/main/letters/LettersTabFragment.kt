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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class LettersTabFragment: Fragment() {
    private val logTag = javaClass.simpleName

    private val viewModel: LettersTabViewModel by viewModels()
    private lateinit var languageListAdapter: ArrayAdapter<String>
    private lateinit var convertToListAdapter: ArrayAdapter<String>

    private lateinit var letterCategoryListView: RecyclerView
    private lateinit var convertToSelector: TextInputLayout
    private val convertToTextView: AutoCompleteTextView?
        get() {
            return convertToSelector.editText as? MaterialAutoCompleteTextView
        }
    private lateinit var languageSelector: TextInputLayout
    private val languageTextView: AutoCompleteTextView?
        get() {
            return languageSelector.editText as? MaterialAutoCompleteTextView
        }

    // A simple lock to prevent multiple observers from trying to update the UI
    private var uiUpdateLock: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.letters_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logDebug(logTag, "onViewCreated")

        // Initialise the transliterator
        viewModel.transliterator = Transliterator(requireContext())
        logDebug(logTag, "Letters category wise: ${viewModel.transliterator!!.languageData.lettersCategoryWise}")
        letterCategoryListView = view.findViewById(R.id.letter_categories)!!
        languageSelector = view.findViewById(R.id.language_selector)
        convertToSelector = view.findViewById(R.id.convert_to_selector)

        initialiseLanguageSelector(view)
        initialiseLettersTabTransSpinner(view)

        // Set up the info button
        view.findViewById<View>(R.id.lettersTabInfoButton).setOnClickListener { v: View? ->
            logDebug(logTag, "Info button clicked!")
            logDebug(logTag,
                "Fetching info for transliterating ${viewModel.transliterator!!.languageData.language} " +
                        "to ${viewModel.targetLanguage}")
            val info: HashMap<String, Map<String, String>> = viewModel.transliterator!!.languageData.info
            logDebug(logTag, "Data for info: $info")
            val lif = LanguageInfoFragment.newInstance(
                info["general"]?.get("en") + info[viewModel.targetLanguage.lowercase()]?.get("en"))
            // MainActivity.replaceTabFragment(0, lif)
        }

        val categoryListViewAdapter = LetterCategoryAdapter(
            viewModel.getLanguageData().lettersCategoryWise,
            viewModel.transliterator!!,
            viewModel.targetLanguage,
        )
        letterCategoryListView.adapter = categoryListViewAdapter
        categoryListViewAdapter.submitList(viewModel.transliterator!!.languageData.categories)

        logDebug(logTag, "Setting up observers for view model data")
        initObservers(view)
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
        languageListAdapter = ArrayAdapter(
            requireContext(),
            R.layout.drop_down_item,
            getAllDownloadedLanguages(),
        )
        languageListAdapter.setNotifyOnChange(true)

        languageTextView?.setAdapter(languageListAdapter)
        val upperCasedLanguage = viewModel.transliterator!!.getLanguage().replaceFirstChar {
            if (it.isLowerCase())
                it.titlecase()
            else
                it.toString()
        }
        logDebug(logTag, "Setting current selection to $upperCasedLanguage")
        logDebug(logTag, "Its adapter position is ${languageListAdapter.getPosition(upperCasedLanguage)}")
        // Initialise the dropdown
        languageTextView?.setText(upperCasedLanguage, false)
        // Initialise in the viewModel too
        viewModel.language = upperCasedLanguage
        // Update view model when language selection changes
        languageTextView?.onItemClickListener = AdapterView.OnItemClickListener(
            fun (parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                logDebug(logTag, "LanguageSelector: ${languageListAdapter.getItem(position)} clicked.")
                viewModel.language = languageListAdapter.getItem(position)!!
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
                    languageListAdapter.clear()
                    languageListAdapter.addAll(getAllDownloadedLanguages())
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
        val transliterationLanguages = viewModel.getLanguageData().supportedLanguagesForTransliteration
        convertToListAdapter = ArrayAdapter(
            requireContext(),
            R.layout.drop_down_item,
            transliterationLanguages,
        )
        convertToListAdapter.setNotifyOnChange(true)

        convertToTextView?.setAdapter(convertToListAdapter)

        // viewModel.targetLanguage = transliterationLanguages[0]
        logDebug(logTag, "Set initial target language to: ${transliterationLanguages[0]}")
        logDebug(logTag, "Its adapter position is ${convertToListAdapter.getPosition(transliterationLanguages[0])}")
        // Initialise the drop down
        convertToTextView?.setText(transliterationLanguages[0], false)
        // Initialise in the view model too
        viewModel.targetLanguage = transliterationLanguages[0]

        convertToTextView?.onItemClickListener = AdapterView.OnItemClickListener(
            fun (parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                logDebug(logTag, "ConvertToSelector: ${convertToListAdapter.getItem(position)} clicked.")
                viewModel.targetLanguage = convertToListAdapter.getItem(position)!!
            }
        )
    }

    private fun initObservers(view: View) {
        // When language is updated, update the entire grid
        viewModel.languageLiveData.observe(viewLifecycleOwner) {
            logDebug(logTag, "languageObserver: change detected")
            // We lock the UI. If the other observer is invoked, it should revert this lock and
            // return without touching the UI. We should not revert this lock in this observer.
            uiUpdateLock = true
            viewModel.setTransliterator(viewModel.language, requireContext())

            // Update the Convert To adapter
            val transliterationLanguages = viewModel.getLanguageData().supportedLanguagesForTransliteration
            logDebug(logTag, "Updating Convert To drop down with $transliterationLanguages")
            convertToListAdapter.apply {
                clear()
                addAll(transliterationLanguages)
                notifyDataSetChanged()
            }
            convertToTextView?.setText(convertToListAdapter.getItem(0)!!, false)
            viewModel.targetLanguage = convertToListAdapter.getItem(0)!!

            val list = ArrayList<String>(viewModel.getLanguageData().lettersCategoryWise.keys)
            (letterCategoryListView.adapter as LetterCategoryAdapter).apply {
                setLettersCategoryWise(viewModel.getLanguageData().lettersCategoryWise)
                updateTargetLanguage(viewModel.targetLanguage)
                updateTransliterator(viewModel.transliterator!!)
                logDebug(logTag, "LetterCategoryAdapter Inspection: \n" +
                        "Item count: ${itemCount} \n" +
                        "Current List: ${currentList}")
                submitList(list)
            }
        }

        // When targetLanguage is updated, update transliterated letter
        viewModel.targetLanguageLiveData.observe(viewLifecycleOwner) {
            val logBegin = "targetLanguageObserver:"
            logDebug(logTag, "$logBegin Change detected")
            if(uiUpdateLock) {
                logDebug(logTag, "$logBegin UI Locked. Returning...")
                uiUpdateLock = false
            } else {
                logDebug(logTag, "$logBegin UI not locked. We are free to update.")
                val list = ArrayList<String>()
                list.addAll(viewModel.getLanguageData().lettersCategoryWise.keys)
                val adapter: LetterCategoryAdapter =
                    letterCategoryListView.adapter as LetterCategoryAdapter
                adapter.updateTargetLanguage(viewModel.targetLanguage)
                adapter.updateLetterGrids(letterCategoryListView)
            }
        }
    }
}
