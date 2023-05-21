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
package `in`.digistorm.aksharam.activities.main.util

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView

open class AutoAdjustingTextView : AppCompatTextView {
    private val logTag = javaClass.simpleName

    // This variable was added to debug a "rogue" non-blocking space (0x00A0) being added to the
    // text sent to TextView. Set to false to disable logging these.
    private val DEBUG_NBSP = false

    constructor(context: Context) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    )

    private fun logNBSPIssue(message: String) {
        if(DEBUG_NBSP)
            logDebug(logTag, message)
    }

    protected var text: String
        get() {
            val _text: CharSequence = super.getText()
            if(_text.toString().contains('\u00A0')) {
                logNBSPIssue("Text contains a non-breaking space (\\u00A0) at pos: " +
                        "${_text.indexOf('\u00A0')}when getting text: $_text")
                return _text.removeSuffix("\u00A0").toString()
            }
            logNBSPIssue("nbsp not found when getting text: \"$_text\"")
            return _text.toString()
        }
        protected set(value) {
            if(value.contains('\u00A0')) {
                logNBSPIssue("Text contains a non-breaking space (\\u00A0) when setting text: $value")
                super.setText(value.removeSuffix("\u00A0"))
            }
            logNBSPIssue("nbsp not found when setting text: \"$value\"")
            super.setText(value)
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val text = text
        var difference = measuredWidth - paint.measureText(text)
        var size: Float
        if (difference < 9) logDebug(logTag, "Resizing $text container text view")
        while (difference < 9) {
            size = textSize - 1.0f
            difference = measuredWidth - paint.measureText(text)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
        }
    }
}
