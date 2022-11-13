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

import android.widget.Spinner
import android.text.TextWatcher
import android.widget.TextView
import android.text.Html
import android.widget.Toast
import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.lang.StringBuilder
import java.util.*

class PracticeTabFragment : Fragment() {
    private val logTag = PracticeTabFragment::class.simpleName

    private var practiceTabLangSpinner: Spinner? = null
    private var practiceString: String? = null
    private var viewModel: PracticeTabViewModel? = null

    private inner class TextChangedListener : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            val practiceTextTV =
                requireActivity().findViewById<TextView>(R.id.PracticeTabPracticeTextTV)
            var correctInProgress = true
            val markup = StringBuilder()
            val transliteratedString = viewModel!!.transliterator!!.transliterate(
                practiceString!!, viewModel!!.transLang!!
            )
            logDebug(
                logTag, "Practice text " + practiceString + " was transliterated to "
                        + transliteratedString
            )
            logDebug(logTag, "Text entered was: $s")
            if (transliteratedString == s.toString()) {
                clearInput()
                practiceTextTV.text = Html.fromHtml(
                    "<font color=\"#7FFF00\">"
                            + practiceString + "</font>"
                )
                Toast.makeText(
                    context, R.string.practice_tab_correct_text_entered,
                    Toast.LENGTH_SHORT
                ).show()
                requireActivity().findViewById<View>(R.id.PracticeTabInputTIET).isEnabled = false
                return
            }
            markup.append("<font color=\"#7FFF00\">")
            var i = 0
            while (i < transliteratedString.length && i < s.length) {
                if (transliteratedString[i] == s[i]) {
                    if (!correctInProgress) {
                        markup.append("</font><font color=\"#7FFF00\">")
                        correctInProgress = true
                    }
                    markup.append(practiceString!![i])
                } else {
                    if (correctInProgress) {
                        correctInProgress = false
                        markup.append("</font><font color=\"#DC143C\">")
                    }
                    markup.append(practiceString!![i])
                }
                i++
            }
            markup.append("</font>")
            markup.append(practiceString!!.substring(i))
            logDebug(logTag, "Marked up string: $markup")
            practiceTextTV.text = Html.fromHtml(markup.toString())
        }

        override fun afterTextChanged(s: Editable) {}
    }

    private val textChangedListener = TextChangedListener()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.practice_tab_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logDebug(logTag, "onViewCreated")
        if (viewModel == null) {
            logDebug(logTag, "Creating View Model for PracticeTabFragment")
            viewModel = ViewModelProvider(requireActivity()).get(
                PracticeTabViewModel::class.java
            )
        }
        // Initialise viewModel with a Transliterator
        viewModel!!.resetTransliterator(context)
        view.findViewById<View>(R.id.PracticeTabRefreshButton).setOnClickListener { v: View? ->
            clearInput()
            startPractice()
            view.findViewById<View>(R.id.PracticeTabInputTIET).isEnabled = true
        }
        initialisePracticeTabLangSpinner(view)
    }

    // Return an empty array list if we could not find any
    // downloaded files. Should not be a problem since we
    // are anyways exiting this activity.
    private val downloadedLanguages: ArrayList<String>
        get() {
            val languages: ArrayList<String> =
                getDownloadedLanguages(requireContext())
            if (languages.size == 0) {
                (requireActivity() as MainActivity).startInitialisationAcitivity()
                return ArrayList() // Return an empty array list if we could not find any
                // downloaded files. Should not be a problem since we
                // are anyways exiting this activity.
            }
            return languages
        }

    private fun initialisePracticeTabLangSpinner(v: View) {
        logDebug(logTag, "Initialising PracticeTabLangSpinner")
        practiceTabLangSpinner = v.findViewById(R.id.PracticeTabLangSpinner)
        val adapter: LabelledArrayAdapter<String> = LabelledArrayAdapter<String>(
            requireContext(),
            R.layout.spinner_item,
            R.id.spinnerItemTV,
            downloadedLanguages,
            R.id.spinnerLabelTV, getString(R.string.practice_tab_lang_hint)
        )
        adapter.setDropDownViewResource(R.layout.spinner_drop_down)
        adapter.setNotifyOnChange(true)
        practiceTabLangSpinner?.adapter = adapter
        practiceTabLangSpinner?.setSelection(0)
        GlobalSettings.instance?.addDataFileListChangedListener("PracticeTabFragmentListener",
            object: DataFileListChanged {
                override fun onDataFileListChanged() {
                    logDebug("PTFListener", "Change in data files detected. Updating adapter.")
                    if (context == null) return
                    viewModel!!.resetTransliterator(context)
                    adapter.clear()
                    adapter.addAll(downloadedLanguages)
                    // While the spinner shows updated text, its (Spinner's) getSelectedView() was sometimes returning
                    // a non-existant item (say, if the item is deleted). Resetting the adapter was the only way I could
                    // think of to fix this
                    logDebug("PTFListener", "Resetting spinner adapter")
                    practiceTabLangSpinner?.adapter = adapter
                }
            })
        practiceTabLangSpinner?.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                logDebug(logTag, "onItemSelected invoked by $parent")
                clearInput()
                val language = parent.getItemAtPosition(position).toString()
                viewModel?.setTransliterator(language, context)

                // re-initialise the "practice in" spinner
                initialisePracticeTabPracticeInSpinner()
                initialisePracticeTabPracticeTypeSpinner()
                requireActivity().findViewById<View>(R.id.PracticeTabInputTIET).isEnabled = true
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun clearInput() {
        val textInputEditText =
            requireActivity().findViewById<TextInputEditText>(R.id.PracticeTabInputTIET)
                ?: return
        logDebug(logTag, "Clearing input edit text")
        textInputEditText.removeTextChangedListener(textChangedListener)
        textInputEditText.setText("")
        textInputEditText.addTextChangedListener(textChangedListener)
    }

    fun initialisePracticeTabPracticeInSpinner() {
        clearInput()
        val practiceTabPracticeInSpinner = requireActivity().findViewById<Spinner>(
            R.id.PracticeTabPracticeInSpinner
        )
        val practiceInAdapter: LabelledArrayAdapter<String> = LabelledArrayAdapter<String>(
            requireContext(),
            R.layout.spinner_item,
            R.id.spinnerItemTV,
            viewModel!!.transliterator!!.language!!.supportedLanguagesForTransliteration,
            R.id.spinnerLabelTV, getString(R.string.practice_tab_practice_in_hint)
        )
        practiceInAdapter.setDropDownViewResource(R.layout.spinner_drop_down)
        practiceTabPracticeInSpinner.adapter = practiceInAdapter
        practiceTabPracticeInSpinner.setSelection(0)
        practiceTabPracticeInSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    clearInput()
                    viewModel!!.transLang = parent.getItemAtPosition(position).toString()
                    (requireActivity().findViewById<View>(R.id.PracticeTabInputTIL) as TextInputLayout).hint =
                        getString(R.string.practice_tab_practice_input_hint, viewModel!!.transLang)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    fun initialisePracticeTabPracticeTypeSpinner() {
        val practiceTabPracticeTypeSpinner = requireActivity().findViewById<Spinner>(
            R.id.PracticeTabPracticeTypeSpinner
        )
        val practiceTypes = ArrayList<String>()
        val categories: Set<String> =
            viewModel!!.transliterator!!.language!!.lettersCategoryWise.keys
        for (category in categories) {
            practiceTypes.add(
                category.substring(0, 1).uppercase()
                        + category.substring(1)
            )
        }

        // Additional practice types
        // Random ligatures work best in some languages like Kannada where each consonant can form
        // a distinct conjunct with another consonant. Other languages like Malayalam or Hindi
        // have a few ligatures, yet this is true only for commonly occurring consonant combinations
        // Most of the combinations in these languages do not result in a meaningful ligature and
        // are usually represented using a half-consonant (with a virama). So, we will add random
        // ligatures only if the language's data file says we should.
        if (viewModel!!.transliterator!!.language!!.areLigaturesAutoGeneratable()) practiceTypes.add(
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
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    clearInput()
                    viewModel!!.practiceType = parent.getItemAtPosition(position).toString()
                    startPractice()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    fun startPractice() {
        if (viewModel!!.practiceType == null || viewModel!!.practiceType == "") return

        // get all letters of current language, category-wise
        val vowels = viewModel!!.transliterator!!.language!!.vowels
        val consonants = viewModel!!.transliterator!!.language!!.consonants
        val ligatures = viewModel!!.transliterator!!.language!!.ligatures
        val signs = viewModel!!.transliterator!!.language!!.diacritics
        val chillu = viewModel!!.transliterator!!.language!!.chillu
        val random = Random()
        var practiceString = StringBuilder()

        // Special variable to hold the Virama.
        // Useful to detect chillu letters in Malayalam
        val virama = viewModel!!.transliterator!!.language!!.virama
        when (viewModel!!.practiceType!!.lowercase()) {
            "random words" ->                 // Let's construct a made-up word in current language
                // First letter can be a vowel or consonant (not a sign)
                // Second letter onwards can be a vowel sign or consonant (not a vowel)
            {
                var i = 0
                while (i < 5) {

                    // What should be the length of a word?
                    val wordLength = random.nextInt(6) + 3 // length is 3 to 6 + 3
                    logDebug(logTag, "Constructing word of length $wordLength")

                    // Choose the first character. Vowel or consonant
                    if (random.nextBoolean()) practiceString.append(vowels[random.nextInt(vowels.size)]) else {
                        practiceString.append(consonants[random.nextInt(consonants.size)])
                    }
                    var j = 1
                    while (j < wordLength) {
                        var nextChar: String
                        val prevChar = practiceString.substring(
                            practiceString.length - 1,
                            practiceString.length
                        )

                        // 20% chance that the next character is a joint letter
                        if (random.nextInt(100) < 21 && prevChar != virama) {
                            // for malayalam, there is also a chance the next character is a chillu
                            nextChar =
                                if (viewModel!!.language.equals("malayalam", ignoreCase = true)) {
                                    if (random.nextInt(100) < 31)
                                        chillu[random.nextInt(chillu.size)]
                                    else  // Since it's malayalam, we can just get one of the ligatures at random
                                        ligatures[random.nextInt(ligatures.size)]
                                } else  // construct ligature
                                    consonants[random.nextInt(consonants.size)]
                                        .toString() + virama + consonants[random.nextInt(consonants.size)]
                        } else if (vowels.contains(prevChar) || signs.contains(prevChar)) {
                            // ...next char must be a consonant
                            nextChar = consonants[random.nextInt(consonants.size)]
                        } else {
                            // if previous character was a consonant, next character can be a
                            // consonant or a sign
                            val randomChoice = if (random.nextBoolean()) consonants else signs
                            do {
                                nextChar = randomChoice[random.nextInt(randomChoice.size)]
                            } while (prevChar == virama && nextChar == virama)
                        }
                        practiceString.append(nextChar)
                        j++
                    }
                    practiceString.append(" ")
                    i++
                }
            }
            "random ligatures" -> {
                var i = 0
                while (i < 10) {

                    // choose a random consonant
                    practiceString.append(consonants[random.nextInt(consonants.size)])
                        .append(virama)
                        .append(consonants[random.nextInt(consonants.size)])
                    practiceString.append(" ")
                    i++
                }
            }
            "signs" -> {
                // predecessor is the consonant or ligature to combine the sign with
                var predecessor = ""
                var i = 0
                while (i < 10) {
                    when (random.nextInt(2)) {
                        0 -> predecessor = consonants[random.nextInt(consonants.size)]
                        1 -> {
                            val letter = ligatures[random.nextInt(ligatures.size)]
                            logDebug(logTag, "letter chosen: $letter")

                            // Following for ligatures that have combining rules.
                            // Certain consonants (right now, only ರ್ in Kannada), form different types of
                            // ligatures before and after a consonant.
                            val isCombineAfter = viewModel?.transliterator?.language
                                ?.getLetterDefinition(letter)?.shouldCombineAfter() ?: false
                            val isCombineBefore = viewModel?.transliterator?.language
                                ?.getLetterDefinition(letter)?.shouldCombineBefore() ?: false
                            val base = viewModel?.transliterator?.language
                                ?.getLetterDefinition(letter)?.base
                            // Find the base of this ligature, if any.
                            // "base" of a ligature is the actual consonant used for combining with
                            // the vowel sign
                            predecessor = if (base == null || base.isEmpty()) letter else base
                            logDebug(logTag, "base: $predecessor")
                            if (isCombineAfter && isCombineBefore) {
                                // If letter can be combined before and after another letter,
                                // do one at random.
                                predecessor =
                                    if (random.nextBoolean()) consonants[random.nextInt(consonants.size)].toString() +
                                            virama + predecessor else predecessor + virama + consonants[random.nextInt(
                                        consonants.size
                                    )]
                            } else if (isCombineAfter) {
                                predecessor = consonants[random.nextInt(consonants.size)]
                                    .toString() + virama + predecessor
                            } else if (isCombineBefore) {
                                predecessor = predecessor + virama + consonants[random.nextInt(
                                    consonants.size
                                )]
                            }
                        }
                    }
                    // what happens if sign selected is a virama?
                    val sign: String? = signs[random.nextInt(signs.size)]
                    practiceString.append(predecessor).append(sign).append(" ")
                    i++
                }
            }
            "ligatures" -> {
                var i = 0
                while (i < 10) {
                    val ligature = ligatures[random.nextInt(ligatures.size)]
                    logDebug(logTag, "Ligature obtained: $ligature")
                    // nextChar is base char if a base exists in the data file.
                    // if there is no base in the data file, nextChar equals ligature (variable above)
                    val nextChar = viewModel?.transliterator?.language?.getLetterDefinition(ligature)?.base ?: ligature

                    // get the rules for combining this letter if such rule exists
                    val isCombineAfter = viewModel?.transliterator?.language
                        ?.getLetterDefinition(ligature)?.shouldCombineAfter() ?: false
                    val isCombineBefore = viewModel?.transliterator?.language
                        ?.getLetterDefinition(ligature)?.shouldCombineBefore() ?: false
                    if (isCombineAfter && isCombineBefore) {
                        // randomly select either combineBefore or combineAfter
                        when (random.nextInt(2)) {
                            0 -> practiceString.append(consonants[random.nextInt(consonants.size)])
                                .append(virama).append(nextChar).append(" ")
                            1 -> practiceString.append(nextChar).append(virama).append(
                                consonants[random.nextInt(consonants.size)]
                            ).append(" ")
                        }
                    } else if (isCombineAfter) {
                        practiceString.append(consonants[random.nextInt(consonants.size)])
                            .append(virama)
                            .append(nextChar).append(" ")
                    } else if (isCombineBefore) {
                        practiceString.append(nextChar).append(virama).append(nextChar)
                            .append(consonants[random.nextInt(consonants.size)])
                    } else {
                        practiceString.append(nextChar).append(" ")
                    }
                    i++
                }
            }
            "vowels" -> {
                var i = 0
                while (i < 10) {
                    practiceString.append(vowels[random.nextInt(vowels.size)]).append(" ")
                    i++
                }
            }
            "consonants" -> {
                var i = 0
                while (i < 10) {
                    practiceString.append(consonants[random.nextInt(consonants.size)]).append(" ")
                    i++
                }
            }
            "chillu" -> {
                var i = 0
                while (i < 10) {
                    practiceString.append(chillu[random.nextInt(chillu.size)]).append(" ")
                    i++
                }
            }
        }

        // strip the last " " from practiceString
        practiceString = StringBuilder(
            practiceString.substring(
                0,
                practiceString.length - 1
            )
        )
        this.practiceString = practiceString.toString()
        (requireActivity().findViewById<View>(R.id.PracticeTabPracticeTextTV) as TextView).text =
            practiceString.toString()
    }
}