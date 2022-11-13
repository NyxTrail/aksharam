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

class TransliterateTabFragment : Fragment() {
    private val logTag = javaClass.simpleName
    private var targetLanguage: String? = null
    private var tr: Transliterator? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.transliteration_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (tr == null) {
            tr = Transliterator(requireContext())
        }
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
                    transliterate(s.toString(), view)
                }
            })
        initialiseSpinner(view)
        GlobalSettings.instance?.addDataFileListChangedListener(
            "TransliterateTabFragmentListener",
            object: DataFileListChanged {
                override fun onDataFileListChanged() {
                    logDebug(logTag, "Re-initialising spinners")
                    initialiseSpinner(view)
                }
            })
    }

    private fun transliterate(s: String, view: View?) {
        logDebug(logTag, "Transliterating $s to $targetLanguage")
        if (s.length == 0 || view == null) return
        val lang = detectLanguage(s)
        if (lang != null && !lang.equals(tr!!.language!!.language, ignoreCase = true)) {
            tr = Transliterator(lang, requireContext())
            initialiseSpinner(null)
        }
        logDebug(logTag, lang + "; " + tr!!.language!!.language)

        // Now we are ready to transliterate
        val outputString = tr!!.transliterate(s, targetLanguage!!)
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

    fun initialiseSpinner(view: View?) {
        logDebug(logTag, "Transliterate tab spinner initialising...")
        var languageSelectionSpinner: Spinner? = null
        if (view != null) languageSelectionSpinner =
            view.findViewById(R.id.LanguageSelectionSpinner) else if (activity != null) {
            languageSelectionSpinner = requireActivity().findViewById(R.id.LanguageSelectionSpinner)
        }
        if (languageSelectionSpinner == null) {
            logDebug(logTag, "Language selection spinner could not be found. Unable to initialise.")
            return
        }
        val adapter: LabelledArrayAdapter<String> = LabelledArrayAdapter<String>(
            requireContext(),
            R.layout.spinner_item,
            R.id.spinnerItemTV,
            tr!!.language!!.supportedLanguagesForTransliteration,
            R.id.spinnerLabelTV, getString(R.string.transliterate_tab_trans_hint)
        )
        adapter.setDropDownViewResource(R.layout.spinner_drop_down)
        languageSelectionSpinner.adapter = adapter
        languageSelectionSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    targetLanguage = parent.getItemAtPosition(position).toString()
                    val editText =
                        requireActivity().findViewById<EditText>(R.id.TransliterateTabInputTextField)
                            ?: return
                    transliterate(
                        editText.text.toString(), requireActivity()
                            .findViewById(R.id.TransliterateTabOutputTextView)
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }
}