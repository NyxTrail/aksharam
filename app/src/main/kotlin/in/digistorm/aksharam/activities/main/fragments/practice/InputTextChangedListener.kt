package `in`.digistorm.aksharam.activities.main.fragments.practice

import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.activities.main.util.logDebug
import `in`.digistorm.aksharam.activities.main.util.setGreen
import `in`.digistorm.aksharam.activities.main.util.setRedGreen
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import `in`.digistorm.aksharam.util.transliterate

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

        // - CharSequence s is usually shorter than practiceString and transliteratedString.
        // - TransliteratedString and practiceString may not always be of the same length.
        var positionInCopy: Int = 0
        val spans: MutableList<Triple<Boolean, Int, Int>> = mutableListOf(Triple(true, 0, 0))
        for((positionInPracticeString: Int, char) in viewModel.practiceString.value!!.withIndex()) {
            if(positionInCopy >= s.length)
                break
            val transChar = transliterate(char.toString(),
                viewModel.practiceInSelected.value!!, viewModel.language.value!!)

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
        practiceTextTV.text = setRedGreen(viewModel.practiceString.value!!, spans, practiceTabFragment.requireContext())
    }
}