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
import `in`.digistorm.aksharam.util.AutoAdjustingTextView
import `in`.digistorm.aksharam.util.logDebug

import android.annotation.SuppressLint
import android.graphics.Point
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.LinearLayout
import android.text.Html
import android.view.Gravity
import android.view.ViewGroup
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.fragment.app.Fragment
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.ViewModelProvider
import java.util.ArrayList

class LetterInfoFragment : Fragment() {
    private val logTag = javaClass.simpleName
    private var currentLetter: String? = ""
    private var viewModel: LettersTabViewModel? = null

    // Set up the LetterInfo dialog
    @SuppressLint("SetTextI18n")
    private fun setUp(v: View, inflater: LayoutInflater) {
        currentLetter = if (arguments != null) arguments!!.getString("letter") else {
            logDebug(logTag, "Null arguments in Setup(View, LayoutInflater)")
            return
        }
        logDebug(logTag, "Showing info dialog for: $currentLetter")
        val tr = viewModel!!.transliterator
        setText(v.findViewById(R.id.letterInfoHeadingTV), currentLetter)
        setText(
            v.findViewById(R.id.letterInfoTransliteratedHeadingTV),
            tr.transliterate(currentLetter!!, viewModel!!.targetLanguage!!)
        )
        val letterExamples = viewModel!!.transliterator.language?.getLetterDefinition(currentLetter)?.getExamples()

        // We pack the examples into the WordAndMeaning Layout in letter_info.xml layout file
        val letterInfoWordAndMeaningLL =
            v.findViewById<LinearLayout>(R.id.letterInfoWordAndMeaningLL)

        // If there are no examples, hide this section
        if (letterExamples == null || letterExamples.toString() == "") {
            v.findViewById<View>(R.id.letterInfoWordsTV).visibility = View.GONE
            v.findViewById<View>(R.id.letterInfoMeaningTV).visibility = View.GONE
        } else {
            for ((key, value) in letterExamples) {
                // Don't attach to root. If attached, we wouldn't be able to find the TextView
                // ID's below
                val wordsAndMeaningView: View = inflater.inflate(
                    R.layout.word_and_meaning,
                    letterInfoWordAndMeaningLL, false
                )
                val px = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP, 4f,
                    resources.displayMetrics
                ).toInt()
                wordsAndMeaningView.setPadding(px, px, px, px)
                val meaning =
                    value[viewModel!!.transliterator.language!!.getLanguageCode(viewModel!!.targetLanguage)]
                logDebug(
                    logTag,
                    "targetLanguage: " + viewModel!!.targetLanguage + "; Word: " + key + "; meaning: " + meaning
                )
                setText(wordsAndMeaningView.findViewById(R.id.wordAndMeaningWordTV), key)
                setText(wordsAndMeaningView.findViewById(R.id.wordAndMeaningMeaningTV), meaning)
                setText(
                    wordsAndMeaningView.findViewById(R.id.wordAndMeaningTransliterationTV),
                    viewModel!!.transliterator.transliterate(
                        key,
                        viewModel!!.targetLanguage!!
                    )
                )
                letterInfoWordAndMeaningLL.addView(wordsAndMeaningView)
            }
        }

        // Check if extra info exists for this letter
        val letterInfo = viewModel!!.transliterator
            .language!!.getLetterDefinition(currentLetter).getInfo()
        val letterInfoInfoTV = v.findViewById<TextView>(R.id.letterInfoInfoTV)
        if (letterInfo == null || letterInfo.isEmpty()) {
            logDebug(
                logTag, "No additional info for " + currentLetter
                        + " was found. Hiding UI element."
            )
            letterInfoInfoTV.visibility = View.GONE
        } else {
            letterInfoInfoTV.text = Html.fromHtml(letterInfo["en"])
        }

        // For consonants and ligatures, show examples of how they can combine with
        // vowel diacritics. For consonants, display possible ligatures with other
        // consonants if ligatures_auto_generatable
        val category = viewModel!!.transliterator.language!!.getLetterDefinition(currentLetter)
            .getType()
        var showDiacriticExamples = true
        if (category != null && !viewModel!!.transliterator.language!!.getLetterDefinition(currentLetter).shouldExcludeCombiExamples()
            && (category.equals("consonants", ignoreCase = true)
                    || category.equals("ligatures", ignoreCase = true))
        ) {
            displaySignConsonantCombinations(v, category)
            if (category.equals(
                    "consonants",
                    ignoreCase = true
                )
            ) if (viewModel!!.transliterator.language!!.areLigaturesAutoGeneratable()) displayLigatures(
                v
            )
        } else showDiacriticExamples = false

        // For a sign, display how it combines with each consonant
        if (category != null && category.equals(
                "signs",
                ignoreCase = true
            )
        ) displaySignConsonantCombinations(v, category) else if (!showDiacriticExamples) {
            v.findViewById<View>(R.id.diacriticSelectorHintTV).visibility = View.GONE
            v.findViewById<View>(R.id.diacriticExamplesGL).visibility = View.GONE
        }
    }

    // Lets try to combine current letter with all letters
    fun displayLigatures(v: View) {
        /* ligatureAfterHintTV, ligaturesGLAfter, linearLayoutAfter, etc are all for
         * ligatures formed when currentLetter appears *after* the virama.
         * ligatureBeforeHintTV, ligaturesGLBefore, linearLayoutBefore, etc are all for
         * ligatures formed when currentLetter appears *before* the virama.
         * TODO: some way to reduce code duplication?
         */
        val ligatureAfterHintTV = v.findViewById<TextView>(R.id.letterInfoLigaturesAfterTV)
        ligatureAfterHintTV.visibility = View.VISIBLE
        val ligatureBeforeHintTV = v.findViewById<TextView>(R.id.letterInfoLigaturesBeforeTV)
        ligatureBeforeHintTV.visibility = View.VISIBLE
        val consonants: ArrayList<String> = viewModel!!.transliterator.language!!.consonants
        val virama = viewModel!!.transliterator.language!!.virama

        // v.findViewById(R.id.letterInfoLigaturesLL).setVisibility(View.VISIBLE);
        val ligaturesGLBefore = v.findViewById<GridLayout>(R.id.letterInfoLigaturesBeforeGL)
        ligaturesGLBefore.removeAllViews()
        ligaturesGLBefore.visibility = View.VISIBLE
        val ligaturesGLAfter = v.findViewById<GridLayout>(R.id.letterInfoLigaturesAfterGL)
        ligaturesGLAfter.removeAllViews()
        ligaturesGLAfter.visibility = View.VISIBLE
        ligatureBeforeHintTV.text = getString(
            R.string.letter_info_ligature_consonant_before,
            currentLetter, currentLetter, virama
        )
        ligatureAfterHintTV.text = getString(
            R.string.letter_info_ligature_consonant_after,
            currentLetter, virama, currentLetter
        )
        val size = Point()
        requireActivity().windowManager.defaultDisplay.getSize(size)
        val cols = 5
        for ((i, consonant) in consonants.withIndex()) {
            val ligatureAfter = consonant + virama + currentLetter
            val ligatureBefore = currentLetter + virama + consonant

            // Can we use the same row, col specs for both GridLayouts?
            val rowSpec = GridLayout.spec(i / cols, GridLayout.CENTER)
            val colSpec = GridLayout.spec(i % cols, GridLayout.CENTER)

            // UI elements for ligatureBefore
            var textView = AutoAdjustingTextView(requireContext())
            textView.gravity = Gravity.CENTER
            val tvLayoutParams = GridLayout.LayoutParams(rowSpec, colSpec)
            tvLayoutParams.width = size.x / 6
            textView.layoutParams = tvLayoutParams
            var px = resources.getDimensionPixelSize(R.dimen.letter_grid_tv_margin)
            tvLayoutParams.setMargins(px, px, px, px)
            px = resources.getDimensionPixelSize(R.dimen.letter_grid_tv_padding)
            textView.setPadding(px, px, px, px)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            textView.text = ligatureBefore
            ligaturesGLBefore.addView(textView, tvLayoutParams)

            // UI elements for ligatureAfter
            textView = AutoAdjustingTextView(requireContext())
            textView.gravity = Gravity.CENTER
            px = resources.getDimensionPixelSize(R.dimen.letter_grid_tv_margin)
            tvLayoutParams.setMargins(px, px, px, px)
            textView.layoutParams = tvLayoutParams
            px = resources.getDimensionPixelSize(R.dimen.letter_grid_tv_padding)
            textView.setPadding(px, px, px, px)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            textView.text = ligatureAfter
            ligaturesGLAfter.addView(textView, tvLayoutParams)
        }
    }

    @SuppressLint("SetTextI18n")
    fun displaySignConsonantCombinations(v: View, type: String) {
        val diacriticSelectorHintTV = v.findViewById<TextView>(R.id.diacriticSelectorHintTV)
        var items: ArrayList<String>? = null
        // We need to display examples for a sign
        if (type.equals("signs", ignoreCase = true)) {
            diacriticSelectorHintTV.text = getString(
                R.string.consonants_with_diacritic,
                currentLetter
            )
            items = viewModel!!.transliterator.language!!.consonants
            items.addAll(viewModel!!.transliterator.language!!.ligatures)
        } else if (type.equals("consonants", ignoreCase = true)
            || type.equals("ligatures", ignoreCase = true)
        ) {
            diacriticSelectorHintTV.text = getString(
                R.string.diacritics_with_consonant,
                currentLetter
            )
            items = viewModel!!.transliterator.language!!.diacritics
        }
        if (items == null) return
        logDebug(logTag, "Items obtained: $items")
        val diacriticExamplesGridLayout = v.findViewById<GridLayout>(R.id.diacriticExamplesGL)
        diacriticExamplesGridLayout.removeAllViews()
        val size = Point()
        val display = requireActivity().windowManager.defaultDisplay
        display.getSize(size)

        val cols = 5
        for ((i, item) in items.withIndex()) {
            val rowSpec = GridLayout.spec(i / cols, GridLayout.CENTER)
            val colSpec = GridLayout.spec(i % cols, GridLayout.CENTER)
            val textView = AutoAdjustingTextView(requireContext())
            textView.gravity = Gravity.CENTER
            textView.setSingleLine()
            val tvLayoutParams = GridLayout.LayoutParams(rowSpec, colSpec)
            tvLayoutParams.width = size.x / 6
            tvLayoutParams.height = resources.getDimensionPixelSize(R.dimen.letter_grid_height)
            var px = resources.getDimensionPixelSize(R.dimen.letter_grid_tv_padding)
            tvLayoutParams.setMargins(px, px, px, px)
            textView.layoutParams = tvLayoutParams
            px = resources.getDimensionPixelSize(R.dimen.letter_grid_tv_margin)
            textView.setPadding(px, px, px, px)
            if (type.equals("signs", ignoreCase = true)
                && !viewModel!!.transliterator.language
                    ?.getLetterDefinition(currentLetter)?.shouldExcludeCombiExamples()!!
            ) {
                textView.text = item + currentLetter
            } else if ((type.equals("consonants", ignoreCase = true)
                        || type.equals("ligatures", ignoreCase = true))
                && !(viewModel?.transliterator?.language
                    ?.getLetterDefinition(currentLetter)?.shouldExcludeCombiExamples())!!
            ) textView.text = currentLetter + item
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            // Add the textView and its parent linear layout only if the textview has some content
            if (textView.text != null && textView.text != "") {
                diacriticExamplesGridLayout.addView(textView, tvLayoutParams)
            }
        }
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
        return inflater.inflate(R.layout.letter_info, container, true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(
            LettersTabViewModel::class.java
        )
        setUp(view, layoutInflater)
    }

    companion object {
        fun newInstance(letter: String?): LetterInfoFragment {
            val letterInfoFragment = LetterInfoFragment()
            val args = Bundle()
            args.putString("letter", letter)
            letterInfoFragment.arguments = args
            return letterInfoFragment
        }
    }
}