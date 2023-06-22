/*
 * Copyright (c) 2023 Alan M Varghese <alan@digistorm.in>
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
package `in`.digistorm.aksharam.activities.main.fragments.practice

import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.activities.main.util.logDebug
import `in`.digistorm.aksharam.activities.main.util.setGreen
import `in`.digistorm.aksharam.activities.main.util.setRedGreen
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import `in`.digistorm.aksharam.activities.main.language.transliterate

class InputTextChangedListener(
    private val practiceTabFragment: PracticeTabFragment,
    private val viewModel: PracticeTabViewModel
): TextWatcher {
    private val logTag = javaClass.simpleName

    private val practiceTextTV: TextView = practiceTabFragment.requireView()
        .findViewById(R.id.practice_text)!!

    override fun afterTextChanged(s: Editable) {}
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if(s.isEmpty())
            return

        // If entered string matches expected transliteration, show suitable message and return
        logDebug(logTag, "Entered string: \"$s\"\n" +
                "Transliteration of practice string: \"${viewModel.transliteratedString.value}\".")
        if(s.toString() == viewModel.transliteratedString.value) {
            logDebug(logTag, "Entered text matches transliteration correctly.")
            // practiceTabFragment.clearInput()
            practiceTextTV.text = setGreen(viewModel.practiceString.value!!, practiceTabFragment.requireContext())
            viewModel.practiceSuccessCheck.value = true
            return
        }

        // If we reach here, user has not finished entering text
        var correctInProgress = true

        // - CharSequence s is usually(?) shorter than practiceString and transliteratedString.
        // - transliteratedString and practiceString may not always be of the same length.
        var positionInCopy = 0
        val spans: MutableList<Triple<Boolean, Int, Int>> = mutableListOf(Triple(true, 0, 0))
        for((positionInPracticeString: Int, char) in viewModel.practiceString.value!!.withIndex()) {
            if(positionInCopy >= s.length)
                break
            // Transliterate the next character in the practice string. The transliterated string may be
            // more than one character in length.
            val transChar = transliterate(char.toString(),
                viewModel.practiceInSelected.value!!, viewModel.language.value!!)

            // We should check these many characters in the string input by the user, starting from
            // positionInCopy.
            var charsToCheck = ""
            // While the user is typing the character combination for the transliteration of char,
            // the transliteration (by user) is not complete. In this situation we do not have enough
            // characters in s yet for a full transliteration.
            val endIndex = if(transChar.length + positionInCopy > s.length)
                s.length
            else
                transChar.length + positionInCopy

            charsToCheck = s.substring(positionInCopy until endIndex)
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
        practiceTextTV.text = setRedGreen(viewModel.practiceString.value!!, spans, practiceTabFragment.requireContext())
    }
}
