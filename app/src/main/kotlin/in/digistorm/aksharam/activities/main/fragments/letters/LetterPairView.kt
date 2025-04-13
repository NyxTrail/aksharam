/*
 * Copyright (c) 2022-2025 Alan M Varghese <alan@digistorm.in>
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

import `in`.digistorm.aksharam.activities.main.util.logDebug
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView

class LetterPairView: AppCompatTextView, View.OnClickListener {
    private var logTag: String = javaClass.simpleName

    /* Boolean to track if the letter or its transliterated pair is current displayed. */
    var letterShown: Boolean = true
        private set

    /* A pair of two letters. The letter in current language and its pair, the transliterated letter,
       in the target language. Also initialise the backing textView keeping in mind our current state. */
    var letters: Pair<String, String>? = null
        set(value) {
            field = value
            text = if(letterShown)
                field?.first!!
            else
                field?.second!!
        }

    constructor(context: Context): super(context)
    constructor(context: Context?, attrs: AttributeSet?): super(
        context!!, attrs
    )
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): super(
        context!!, attrs, defStyleAttr
    )

    /* Switch letter and transliterated letter on click. */
    override fun onClick(v: View?) {
        logDebug(logTag, "\"$text\" clicked")
        if(letters != null) {
            /* Letter is currently shown. Show transliterated letter now. */
            if(letterShown) {
                text = letters?.second!!
                letterShown = false
            }
            else { /* Show letter. */
                text = letters?.first!!
                letterShown = true
            }
        }
        else
            logDebug(logTag, "Letter,TransliteratedLetter not available in LetterView")
    }

    init {
        setOnClickListener(this)
    }
}
