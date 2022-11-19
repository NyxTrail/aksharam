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
import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import androidx.lifecycle.ViewModelProvider
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.lang.StringBuilder
import java.util.*

class PracticeTabFragment : Fragment() {
    private val logTag = PracticeTabFragment::class.simpleName

    private lateinit var textChangedListener: TextChangedListener

    private inner class TextChangedListener(val viewModel: PracticeTabViewModel) : TextWatcher {
        private val logTag = this.javaClass.simpleName
        override fun afterTextChanged(s: Editable) {}
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if(s.isEmpty())
                return

            val practiceTextTV =
                requireActivity().findViewById<TextView>(R.id.PracticeTabPracticeTextTV)

            // If entered string matches expected transliteration, show suitable message and return
            if(s == viewModel.transliteratedString) {
                clearInput()
                practiceTextTV.text = setGreen(viewModel.practiceString, requireContext())
                Toast.makeText(requireContext(), R.string.practice_tab_correct_text_entered, Toast.LENGTH_SHORT).show()
                requireActivity().findViewById<TextInputEditText>(R.id.PracticeTabInputTIET).isEnabled = false
                return
            }

            // If we reach here, user has not finished entering text
            var correctInProgress = true

            // - CharSequence s is usually shorter than practiceString and transliteratedString.
            // - TransliteratedString and practiceString may not always be of the same length.
            var positionInCopy: Int = 0
            val spans: MutableList<Triple<Boolean, Int, Int>> = mutableListOf(Triple(true, 0, 0))
            for((positionInPracticeString: Int, char) in viewModel.practiceString.withIndex()) {
                if(positionInCopy >= s.length)
                    break
                val transChar = viewModel.transliterator.transliterate(char.toString(), viewModel.transLang)

                // get transChar.length characters from sCopy
                var charsToCheck = ""
                charsToCheck = s.substring(positionInCopy until (transChar.length + positionInCopy))
                logDebug(logTag, "Characters to check: \"$charsToCheck\"")
                positionInCopy += transChar.length

                if(transChar == charsToCheck) {
                    if(correctInProgress)
                        spans[spans.lastIndex] = Triple(true, spans[spans.lastIndex].second, positionInPracticeString)
                    else {
                        if(char == ' ')
                            spans[spans.lastIndex] = Triple(false, spans[spans.lastIndex].second, positionInPracticeString)
                        else {
                            correctInProgress = true
                            spans.add(Triple(true, positionInPracticeString, positionInPracticeString))
                        }
                    }
                } else { // characters entered do not match
                    if(correctInProgress) {
                        correctInProgress = false
                        spans.add(Triple(false, positionInPracticeString, positionInPracticeString))
                    } else {
                        spans[spans.lastIndex] = Triple(false, spans[spans.lastIndex].second, positionInPracticeString)
                    }
                }
            }
            logDebug(logTag, spans.toString())
            practiceTextTV.text = setRedGreen(viewModel.practiceString, spans, requireContext())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.practice_tab_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logDebug(logTag, "onViewCreated")
        val viewModel: PracticeTabViewModel = ViewModelProvider(requireActivity())[
                PracticeTabViewModel::class.java]
        textChangedListener = TextChangedListener(viewModel)

        // Initialise viewModel with a Transliterator
        view.findViewById<View>(R.id.PracticeTabRefreshButton).setOnClickListener {
            clearInput()
            startPractice(viewModel)
            view.findViewById<View>(R.id.PracticeTabInputTIET).isEnabled = true
        }
        initialisePracticeTabLangSpinner(view)
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

    private fun initialisePracticeTabLangSpinner(view: View) {
        logDebug(logTag, "Initialising PracticeTabLangSpinner")
        val practiceTabLangSpinner: Spinner = view.findViewById(R.id.PracticeTabLangSpinner)
        val viewModel: PracticeTabViewModel = ViewModelProvider(requireActivity())[
                PracticeTabViewModel::class.java]

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
                    // a non-existant item (say, if the item is deleted). Resetting the adapter was the only way I could
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
                clearInput()
                val language = parent?.getItemAtPosition(position).toString()
                viewModel.setTransliterator(language, requireContext())

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

        logDebug(logTag, "Initialising \"Practice In\" spinner.")
        val practiceTabPracticeInSpinner = requireActivity().findViewById<Spinner>(
            R.id.PracticeTabPracticeInSpinner)
        val viewModel: PracticeTabViewModel = ViewModelProvider(requireActivity())[
                PracticeTabViewModel::class.java]
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
                    viewModel.transLang = parent?.getItemAtPosition(position).toString()
                    (requireActivity().findViewById<View>(R.id.PracticeTabInputTIL) as TextInputLayout).hint =
                        getString(R.string.practice_tab_practice_input_hint, viewModel.transLang)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    fun initialisePracticeTabPracticeTypeSpinner() {

        logDebug(logTag, "Initialising \"Practice Type\" spinner.")
        val practiceTabPracticeTypeSpinner: Spinner = requireActivity().findViewById(
            R.id.PracticeTabPracticeTypeSpinner)

        val viewModel: PracticeTabViewModel = ViewModelProvider(requireActivity())[
                PracticeTabViewModel::class.java]
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
        // a distinct conjunct with another consonant. Other languages like Malayalam or Hindi
        // have a few ligatures, yet this is true only for commonly occurring consonant combinations
        // Most of the combinations in these languages do not result in a meaningful ligature and
        // are usually represented using a half-consonant (with a virama). So, we will add random
        // ligatures only if the language's data file says we should.
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
                    viewModel.practiceType = parent?.getItemAtPosition(position).toString()
                    startPractice(viewModel)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    fun startPractice(viewModel: PracticeTabViewModel) {
        if (viewModel.practiceType.isEmpty())
            return

        // get all letters of current language, category-wise
        val vowels = viewModel.getLanguageData().vowels
        val consonants = viewModel.getLanguageData().consonants
        val ligatures = viewModel.getLanguageData().ligatures
        val signs = viewModel.getLanguageData().diacritics
        val chillu = viewModel.getLanguageData().chillu
        val random = Random()
        var practiceString = StringBuilder()

        // Special variable to hold the Virama.
        // Useful to detect chillu letters in Malayalam
        val virama = viewModel.getLanguageData().virama
        when (viewModel.practiceType.lowercase()) {
            // Let's construct a made-up word in current language
            // First letter can be a vowel or consonant (not a sign)
            // Second letter onwards can be a vowel sign or consonant (not a vowel)
            "random words" -> {
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
                                if (viewModel.getLanguage().equals("malayalam", ignoreCase = true)) {
                                    if (random.nextInt(100) < 31)
                                        chillu[random.nextInt(chillu.size)]
                                    else  // Since it's malayalam, we can just get one of the ligatures at random
                                        ligatures[random.nextInt(ligatures.size)]
                                } else  // construct ligature
                                    consonants[random.nextInt(consonants.size)] + virama + consonants[random.nextInt(consonants.size)]
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
                            val isCombineAfter = viewModel.getLanguageData()
                                .getLetterDefinition(letter)?.shouldCombineAfter() ?: false
                            val isCombineBefore = viewModel.getLanguageData()
                                .getLetterDefinition(letter)?.shouldCombineBefore() ?: false
                            val base = viewModel.getLanguageData().getLetterDefinition(letter)?.base

                            // Find the base of this ligature, if any.
                            // "base" of a ligature is the actual consonant used for combining with
                            // the vowel sign
                            predecessor = if (base == null || base.isEmpty()) letter else base
                            logDebug(logTag, "base: $predecessor")
                            if (isCombineAfter && isCombineBefore) {
                                // If letter can be combined before and after another letter,
                                // do one at random.
                                predecessor =
                                    if (random.nextBoolean()) consonants[random.nextInt(consonants.size)] +
                                            virama + predecessor else predecessor + virama + consonants[random.nextInt(
                                        consonants.size
                                    )]
                            } else if (isCombineAfter) {
                                predecessor = consonants[random.nextInt(consonants.size)] + virama + predecessor
                            } else if (isCombineBefore) {
                                predecessor = predecessor + virama + consonants[random.nextInt(
                                    consonants.size
                                )]
                            }
                        }
                    }
                    // what happens if sign selected is a virama?
                    val sign: String = signs[random.nextInt(signs.size)]
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
                    val nextChar = viewModel.getLanguageData().getLetterDefinition(ligature)?.base ?: ligature

                    // get the rules for combining this letter if such rule exists
                    val isCombineAfter = viewModel.getLanguageData()
                        .getLetterDefinition(ligature)?.shouldCombineAfter() ?: false
                    val isCombineBefore = viewModel.getLanguageData()
                        .getLetterDefinition(ligature)?.shouldCombineBefore() ?: false
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
        viewModel.practiceString = practiceString.toString()
        viewModel.transliteratedString = viewModel.transliterator
            .transliterate(viewModel.practiceString, viewModel.transLang)
        // Set max input length of the EditText to the length of the transliterated string
        requireActivity().findViewById<TextInputEditText>(R.id.PracticeTabInputTIET).filters =
            arrayOf(InputFilter.LengthFilter(viewModel.transliteratedString.length))
        requireActivity().findViewById<TextView>(R.id.PracticeTabPracticeTextTV).text =
            practiceString.toString()
    }
}