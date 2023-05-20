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
package `in`.digistorm.aksharam.activities.main.fragments.letters

import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.activities.main.ActivityViewModel
import `in`.digistorm.aksharam.databinding.LetterViewBinding
import `in`.digistorm.aksharam.databinding.WordAndMeaningBinding
import `in`.digistorm.aksharam.activities.main.util.logDebug
import `in`.digistorm.aksharam.activities.main.language.transliterate

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import `in`.digistorm.aksharam.databinding.FragmentLetterInfoBinding

class LetterInfoFragment : Fragment() {
    private val logTag = javaClass.simpleName

    private lateinit var binding: FragmentLetterInfoBinding
    private val args: LetterInfoFragmentArgs by navArgs()
    private val activityViewModel: ActivityViewModel by activityViewModels()

    private val letter by lazy { args.letter }
    private val targetLanguage by lazy { args.targetLanguage }
    private val languageData by lazy { activityViewModel.language.value!! }
    private val targetLanguageCode by lazy { languageData.getLanguageCode(targetLanguage) }
    private val letterDefinition by lazy { languageData.getLetterDefinition(letter) }
    private val letterType by lazy { letterDefinition?.type }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentLetterInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setupWithNavController(
            findNavController(),
            AppBarConfiguration(setOf(R.id.tabbedViewsFragment, R.id.initialisationScreen))
        )

        setUp(layoutInflater)
    }

    private fun showDiacriticExamplesUI(visibility: Int) {
        binding.diacriticHint.visibility = visibility
        binding.diacriticExamplesContainer.visibility = visibility
    }

    // Set up the LetterInfo dialog
    private fun setUp(inflater: LayoutInflater) {
        logDebug(logTag, "Showing info dialog for: ${args.letter}")

        binding.heading.text = args.letter
        binding.transliteratedHeading.text =
            transliterate(args.letter, args.targetLanguage, languageData)

        /**
         * Example words and their meanings.
         */
        // We pack the examples into the WordAndMeaning Layout in letter_info.xml layout file
        val letterExamples = languageData.getLetterDefinition(args.letter)!!.examples

        // If there are no examples, hide this section
        if (letterExamples.isEmpty()) {
            logDebug(logTag, "No examples found for letter: $letter")
            binding.wordsHeading.visibility = View.GONE
            binding.meaningHeading.visibility = View.GONE
        } else {
            showExamplesWordsAndMeanings(letterExamples, inflater)
        }

        /**
         * Display additional info for the current letter.
         */
        // Check if extra info exists for this letter
        if (letterDefinition?.info?.get("en") == null) {
            logDebug(logTag, "No additional info for $letter was found. Hiding UI element.")
            binding.info.visibility = View.GONE
        } else {
            logDebug(logTag, "Info found for letter $letter: ${letterDefinition?.info}")
            binding.info.text = HtmlCompat.fromHtml(
                letterDefinition?.info?.get("en")!!, HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }

        when (letterType) {
            "vowels" -> {
            }
            "signs" -> {
                if (letterDefinition?.shouldExcludeCombiExamples() != true) {
                    showDiacriticsForSign(inflater)
                }
            }
            "consonants" -> {
                if (letterDefinition?.shouldExcludeCombiExamples() != true) {
                    showDiacriticsForLetter(inflater)
                    showLigatures(inflater)
                }
            }
            "ligatures" -> {
                if (letterDefinition?.shouldExcludeCombiExamples() != true) {
                    showDiacriticsForLetter(inflater)
                }
            }
            "chillu" -> {
            }
            else -> {
                logDebug(logTag, "Unknown category: $letterType for letter: $letter")
            }
        }
    }

    private fun showExamplesWordsAndMeanings(
        letterExamples: LinkedHashMap<String, Map<String, String>>,
        inflater: LayoutInflater
    ) {
        letterExamples.forEach { (word, meanings) ->
            val wordsAndMeaningBinding = WordAndMeaningBinding.inflate(
                inflater, binding.wordAndMeaningContents, false
            )
            logDebug(logTag, "Word: $word; meaning: ${meanings[targetLanguageCode]}")
            wordsAndMeaningBinding.word.text = word
            wordsAndMeaningBinding.meaning.text = meanings[targetLanguageCode]!!
            wordsAndMeaningBinding.transliteration.text =
                transliterate(word, targetLanguage, languageData)
            binding.wordAndMeaningContents.addView(wordsAndMeaningBinding.root)
        }
    }

    /**
     * If current letter is a sign, show all its combinations with consonants.
     */
    private fun showDiacriticsForSign(
        inflater: LayoutInflater
    ) {
        showDiacriticExamplesUI(View.VISIBLE)
        binding.diacriticHint.text =
            getString(R.string.letter_with_consonants_and_ligatures, letter)
        languageData.generateDiacriticsForSign(letter).forEach { diacritic ->
            val letterView =
                LetterViewBinding.inflate(
                    inflater,
                    binding.diacriticExamplesContainer,
                    false
                )
            letterView.letterView.text = diacritic
            binding.diacriticExamplesContainer.addView(letterView.root)
        }
    }

    /**
     * Combine current letter with all signs. Use when current letter is consonant or ligature.
     */
    private fun showDiacriticsForLetter (
        inflater: LayoutInflater
    ) {
        showDiacriticExamplesUI(View.VISIBLE)
        binding.diacriticHint.text =
            getString(R.string.letter_with_vowel_signs, letter)
        languageData.generateDiacriticsForLetter(letter).forEach { diacritic ->
            val letterView = LetterViewBinding.inflate(
                inflater,
                binding.diacriticExamplesContainer,
                false
            )
            letterView.letterView.text = diacritic
            binding.diacriticExamplesContainer.addView(letterView.root)
        }
    }

    /**
     * Generate ligatures by combining a consonant with every other consonant.
     * These combinations must follow the rule:
     * ligature = consonant_1 + virama + consonant_2
     * We depend on the data file to determine if this would be useful in a language.
     */
    private fun showLigatures(
        inflater: LayoutInflater
    ) {
        val (ligaturesWithLetterAsPrefix, ligaturesWithLetterAsSuffix) = languageData.generateLigatures(letter)
        ligaturesWithLetterAsPrefix.apply {
            if(isNotEmpty()) {
                binding.ligaturesWithLetterAsPrefixTv.text =
                    getString(
                        R.string.ligatures_with_letter_as_prefix,
                        letter,
                        letter,
                        languageData.virama
                    )
                forEach { ligature ->
                    val letterView = LetterViewBinding.inflate(
                        inflater,
                        binding.ligaturesWithLetterAsPrefixGl,
                        false
                    )
                    letterView.letterView.text = ligature
                    binding.ligaturesWithLetterAsPrefixGl.addView(letterView.root)
                }
                binding.ligaturesWithLetterAsPrefixTv.visibility = View.VISIBLE
                binding.ligaturesWithLetterAsPrefixGl.visibility = View.VISIBLE
            }
        }

        ligaturesWithLetterAsSuffix.apply {
            if(isNotEmpty()) {
                binding.ligaturesWithLetterAsSuffixTv.text =
                    getString(
                        R.string.ligatures_with_letter_as_suffix,
                        letter,
                        languageData.virama,
                        letter
                    )
                forEach { ligature ->
                    val letterView = LetterViewBinding.inflate(
                        inflater,
                        binding.ligaturesWithLetterAsSuffixGl,
                        false
                    )
                    letterView.letterView.text = ligature
                    binding.ligaturesWithLetterAsSuffixGl.addView(letterView.root)
                }
                binding.ligaturesWithLetterAsSuffixTv.visibility = View.VISIBLE
                binding.ligaturesWithLetterAsSuffixGl.visibility = View.VISIBLE
            }
        }
    }
}