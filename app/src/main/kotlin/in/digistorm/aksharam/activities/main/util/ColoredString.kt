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
package `in`.digistorm.aksharam.activities.main.util

import `in`.digistorm.aksharam.R
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat

fun setGreen(s: String, context: Context): SpannableString {
    val spannableString = SpannableString(s)
    spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_green)),
        0,
        s.length,
        Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    return spannableString
}

fun setRed(s:String, context: Context): SpannableString {
    val spannableString = SpannableString(s)
    spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_red)),
        0,
        s.length,
        Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    return spannableString
}

fun setRedGreen(s:String, spans: List<Triple<Boolean, Int, Int>>, context: Context): SpannableString{
    val spannableString = SpannableString(s)
    for(triple in spans) {
        if(triple.first) {
            if(triple.third + 1 > triple.second)
                spannableString.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_green)),
                    triple.second,
                    triple.third + 1,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        }
        else
            if(triple.third + 1 > triple.second)
                spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_red)),
                    triple.second,
                    triple.third + 1,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    }
    return spannableString
}