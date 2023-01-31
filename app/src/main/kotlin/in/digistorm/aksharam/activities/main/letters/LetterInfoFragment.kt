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
import `in`.digistorm.aksharam.activities.main.models.AksharamViewModel
import `in`.digistorm.aksharam.databinding.LetterInfoBinding
import `in`.digistorm.aksharam.databinding.LetterViewBinding
import `in`.digistorm.aksharam.databinding.WordAndMeaningBinding
import `in`.digistorm.aksharam.util.Transliterator
import `in`.digistorm.aksharam.util.logDebug
import `in`.digistorm.aksharam.util.transliterate

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.LinearLayout
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs

class LetterInfoFragment : Fragment() {
    private val logTag = javaClass.simpleName

    private lateinit var binding: LetterInfoBinding
    private val args: LetterInfoFragmentArgs by navArgs()

    // Set up the LetterInfo dialog
    @SuppressLint("SetTextI18n")
    private fun setUp(v: View, inflater: LayoutInflater) {
        logDebug(logTag, "Showing info dialog for: ${args.letter}")

        val activityViewModel: AksharamViewModel by activityViewModels()
        val letter = args.letter
        val targetLanguage = args.targetLanguage
        val languageData = activityViewModel.language.value!!
        val targetLanguageCode = languageData.getLanguageCode(targetLanguage)!!
        val letterDefinition = languageData.getLetterDefinition(letter)!!
        val letterType = letterDefinition.type

        binding.letterInfoHeadingTV.text = args.letter
        binding.letterInfoTransliteratedHeadingTV.text = transliterate(args.letter, args.targetLanguage, languageData)

        /**
         * Example words and their meanings.
         */
        // We pack the examples into the WordAndMeaning Layout in letter_info.xml layout file
        val letterExamples = languageData.getLetterDefinition(args.letter)!!.examples
        val letterInfoWordAndMeaningLL = binding.letterInfoWordAndMeaningLL

        // If there are no examples, hide this section
        if (letterExamples.isEmpty()) {
            logDebug(logTag, "No examples found for letter: $letter")
            binding.letterInfoWordsTV.visibility = View.GONE
            binding.letterInfoMeaningTV.visibility = View.GONE
        } else {
            letterExamples.forEach { (word, meanings) ->
                val wordsAndMeaningBinding = WordAndMeaningBinding.inflate(
                    inflater, letterInfoWordAndMeaningLL, false
                )
                logDebug(logTag, "Word: $word; meaning: ${meanings[targetLanguageCode]}")
                wordsAndMeaningBinding.wordAndMeaningWordTV.text = word
                wordsAndMeaningBinding.wordAndMeaningMeaningTV.text = meanings[targetLanguageCode]!!
                wordsAndMeaningBinding.wordAndMeaningTransliterationTV.text =
                    transliterate(letter, targetLanguage, languageData)
                letterInfoWordAndMeaningLL.addView(wordsAndMeaningBinding.root)
            }
        }

        /**
         * Display additional info for the current letter.
         */
        // Check if extra info exists for this letter
        if(letterDefinition.info["en"] == null) {
            logDebug(logTag, "No additional info for $letter was found. Hiding UI element.")
            binding.letterInfoInfoTV.visibility = View.GONE
        } else {
            logDebug(logTag, "Info found for letter $letter: ${letterDefinition.info}")
            binding.letterInfoInfoTV.text = HtmlCompat.fromHtml(
                letterDefinition.info["en"]!!, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

        when(letterType) {
            "vowels" -> {
                setVisibilityForDiacriticExamplesUI(View.GONE)
            }
            /**
             * If current letter is a sign, show all its combinations with consonants.
             */
            "signs" -> {
                if(!letterDefinition.shouldExcludeCombiExamples()) {
                    setVisibilityForDiacriticExamplesUI(View.VISIBLE)
                    binding.diacriticSelectorHintTV.text =
                        getString(R.string.consonants_with_diacritic, letter)
                    languageData.consonants.forEach { consonant ->
                        if (languageData.getLetterDefinition(consonant)
                                ?.shouldExcludeCombiExamples() != true
                        ) {
                            val letterView =
                                LetterViewBinding.inflate(
                                    inflater,
                                    binding.diacriticExamplesGL,
                                    false
                                )
                            letterView.letterView!!.text = consonant + letter
                            binding.diacriticExamplesGL.addView(letterView.root)
                        }
                    }
                    languageData.ligatures.forEach { ligature ->
                        if (languageData.getLetterDefinition(ligature)
                                ?.shouldExcludeCombiExamples() != true
                        ) {
                            val letterView =
                                LetterViewBinding.inflate(
                                    inflater,
                                    binding.diacriticExamplesGL,
                                    false
                                )
                            letterView.letterView!!.text = ligature + letter
                            binding.diacriticExamplesGL.addView(letterView.root)
                        }
                    }
                } else {
                    setVisibilityForDiacriticExamplesUI(View.GONE)
                }
            }
            "consonants" -> {
                setVisibilityForDiacriticExamplesUI(View.GONE)

            }
            "ligatures" -> {
                setVisibilityForDiacriticExamplesUI(View.GONE)

            }
            else -> {
                logDebug(logTag, "Unknown category: $letterType for letter: $letter")
            }
        }
//        val category: String = transliterator.languageData.getLetterDefinition(currentLetter)?.type ?: ""
//        var showDiacriticExamples = true
//        if (category.isNotEmpty()
//            && !transliterator.languageData.getLetterDefinition(currentLetter)!!.shouldExcludeCombiExamples()
//            && (areStringsEqual(category, "consonants")
//                    || areStringsEqual(category, "ligatures"))) {
//            displaySignConsonantCombinations(v, category)
//            if (areStringsEqual(category, "consonants"))
//                if (transliterator.languageData.areLigaturesAutoGeneratable())
//                    displayLigatures(v)
//        }
//        else
//            showDiacriticExamples = false
//
//         For a sign, display how it combines with each consonant
//        if (category.isNotEmpty() && areStringsEqual(category, "signs"))
//            displaySignConsonantCombinations(v, category)
//        else if (!showDiacriticExamples) {
//            v.findViewById<View>(R.id.diacriticSelectorHintTV).visibility = View.GONE
//            v.findViewById<View>(R.id.diacriticExamplesGL).visibility = View.GONE
//        }
    }

    // Lets try to combine current letter with all letters
//    private fun displayLigatures(v: View) {
//        /* ligatureAfterHintTV, ligaturesGLAfter, linearLayoutAfter, etc are all for
//         * ligatures formed when currentLetter appears *after* the virama.
//         * ligatureBeforeHintTV, ligaturesGLBefore, linearLayoutBefore, etc are all for
//         * ligatures formed when currentLetter appears *before* the virama.
//         * TODO: some way to reduce code duplication?
//         */
//        val ligatureAfterHintTV = v.findViewById<TextView>(R.id.letterInfoLigaturesAfterTV)
//        ligatureAfterHintTV.visibility = View.VISIBLE
//        val ligatureBeforeHintTV = v.findViewById<TextView>(R.id.letterInfoLigaturesBeforeTV)
//        ligatureBeforeHintTV.visibility = View.VISIBLE
//        val consonants: ArrayList<String> = transliterator.languageData.consonants
//        val virama = transliterator.languageData.virama
//
//        val ligaturesGLBefore = v.findViewById<GridLayout>(R.id.letterInfoLigaturesBeforeGL)
//        ligaturesGLBefore.removeAllViews()
//        ligaturesGLBefore.visibility = View.VISIBLE
//        val ligaturesGLAfter = v.findViewById<GridLayout>(R.id.letterInfoLigaturesAfterGL)
//        ligaturesGLAfter.removeAllViews()
//        ligaturesGLAfter.visibility = View.VISIBLE
//        ligatureBeforeHintTV.text = getString(
//            R.string.letter_info_ligature_consonant_before,
//            currentLetter, currentLetter, virama
//        )
//        ligatureAfterHintTV.text = getString(
//            R.string.letter_info_ligature_consonant_after,
//            currentLetter, virama, currentLetter
//        )
//        val size = Point()
//        requireActivity().windowManager.defaultDisplay.getSize(size)
//        val cols = 5
//        for ((i, consonant) in consonants.withIndex()) {
//            val ligatureAfter = consonant + virama + currentLetter
//            val ligatureBefore = currentLetter + virama + consonant
//
//            // Can we use the same row, col specs for both GridLayouts?
//            val rowSpec = GridLayout.spec(i / cols, GridLayout.CENTER)
//            val colSpec = GridLayout.spec(i % cols, GridLayout.CENTER)
//
//            // UI elements for ligatureBefore
//            var textView = AutoAdjustingTextView(requireContext())
//            textView.gravity = Gravity.CENTER
//            val tvLayoutParams = GridLayout.LayoutParams(rowSpec, colSpec)
//            tvLayoutParams.width = size.x / 6
//            textView.layoutParams = tvLayoutParams
//            var px = resources.getDimensionPixelSize(R.dimen.letter_grid_tv_margin)
//            tvLayoutParams.setMargins(px, px, px, px)
//            px = resources.getDimensionPixelSize(R.dimen.letter_grid_tv_padding)
//            textView.setPadding(px, px, px, px)
//            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
//            textView.text = ligatureBefore
//            ligaturesGLBefore.addView(textView, tvLayoutParams)
//
//            // UI elements for ligatureAfter
//            textView = AutoAdjustingTextView(requireContext())
//            textView.gravity = Gravity.CENTER
//            px = resources.getDimensionPixelSize(R.dimen.letter_grid_tv_margin)
//            tvLayoutParams.setMargins(px, px, px, px)
//            textView.layoutParams = tvLayoutParams
//            px = resources.getDimensionPixelSize(R.dimen.letter_grid_tv_padding)
//            textView.setPadding(px, px, px, px)
//            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
//            textView.text = ligatureAfter
//            ligaturesGLAfter.addView(textView, tvLayoutParams)
//        }
//    }

    @SuppressLint("SetTextI18n")
//    fun displaySignConsonantCombinations(v: View, type: String) {
//        logDebug(logTag, "Displaying combinations for $currentLetter")
//        val diacriticSelectorHintTV = v.findViewById<TextView>(R.id.diacriticSelectorHintTV)
//        var items: ArrayList<String>? = null
//        // We need to display examples for a sign
//        if (type.equals("signs", ignoreCase = true)) {
//            diacriticSelectorHintTV.text = getString(
//                R.string.consonants_with_diacritic,
//                currentLetter
//            )
//            items = transliterator.languageData.consonants
//            items.addAll(transliterator.languageData.ligatures)
//        } else if (type.equals("consonants", ignoreCase = true)
//            || type.equals("ligatures", ignoreCase = true)) {
//            diacriticSelectorHintTV.text = getString(
//                R.string.diacritics_with_consonant,
//                currentLetter
//            )
//            items = transliterator.languageData.diacritics
//        }
//        if (items == null) return
//        logDebug(logTag, "Items obtained: $items")
//        val diacriticExamplesGridLayout = v.findViewById<GridLayout>(R.id.diacriticExamplesGL)
//        diacriticExamplesGridLayout.removeAllViews()
//        val size = Point()
//        val display = requireActivity().windowManager.defaultDisplay
//        display.getSize(size)
//
//        val cols = 5
//        for ((i, item) in items.withIndex()) {
//            val rowSpec = GridLayout.spec(i / cols, GridLayout.CENTER)
//            val colSpec = GridLayout.spec(i % cols, GridLayout.CENTER)
//            val textView = AutoAdjustingTextView(requireContext())
//            textView.gravity = Gravity.CENTER
//            textView.setSingleLine()
//            val tvLayoutParams = GridLayout.LayoutParams(rowSpec, colSpec)
//            tvLayoutParams.width = size.x / 6
//            tvLayoutParams.height = resources.getDimensionPixelSize(R.dimen.letter_grid_height)
//            var px = resources.getDimensionPixelSize(R.dimen.letter_grid_tv_padding)
//            tvLayoutParams.setMargins(px, px, px, px)
//            textView.layoutParams = tvLayoutParams
//            px = resources.getDimensionPixelSize(R.dimen.letter_grid_tv_margin)
//            textView.setPadding(px, px, px, px)
//            val shouldExcludeCombiExamples: Boolean = transliterator.languageData
//                .getLetterDefinition(currentLetter)?.shouldExcludeCombiExamples()!!
//            if (areStringsEqual(type, "signs") && !shouldExcludeCombiExamples) {
//                textView.text = item + currentLetter
//            } else if ((areStringsEqual(type, "consonants")
//                        || areStringsEqual(type, "ligatures")) && !shouldExcludeCombiExamples)
//                textView.text = currentLetter + item
//            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
//            // Add the textView and its parent linear layout only if the textview has some content
//            if (textView.text != null && textView.text != "") {
//                diacriticExamplesGridLayout.addView(textView, tvLayoutParams)
//            }
//        }
//    }

    private fun areStringsEqual(string: String, target: String): Boolean {
        return string.equals(target, ignoreCase = true)
    }

    // A wrapper for setText to add a space at the end, to work around clipping of long characters
    @SuppressLint("SetTextI18n")
    private fun setText(tv: TextView, text: String?) {
        when (text!!.substring(text.length - 1)) {
            "्", "ँ", "ॅ", "ॉ", "്", "ೃ" -> tv.text = "$text "
            else -> tv.text = text
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = LetterInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUp(view, layoutInflater)
    }

    private fun setVisibilityForDiacriticExamplesUI(visibility: Int) {
        binding.diacriticSelectorHintTV.visibility = visibility
        binding.diacriticExamplesGL.visibility = visibility
    }
}