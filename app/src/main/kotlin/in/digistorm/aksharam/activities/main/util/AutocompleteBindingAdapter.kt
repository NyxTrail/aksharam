/*
 * Copyright (c) 2023-2025 Alan M Varghese <alan@digistorm.in>
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

import android.widget.ArrayAdapter
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.activities.main.fragments.letters.LetterCategoryAdapter
import `in`.digistorm.aksharam.activities.main.language.TransliteratedLetters

private const val logTag: String = "AutoCompleteBindingAdapter"

@BindingAdapter(value = ["list", "android:text"], requireAll = false)
fun MaterialAutoCompleteTextView.writeSimpleItems(
    oldItems: List<String>?,
    oldText: String?,
    newItems: List<String>?,
    newText: String?,
) {
    logDebug(logTag, "writeSimpleItems")
    logDebug(logTag, "oldItems: $oldItems, oldText: $oldText")
    logDebug(logTag, "newItems: $newItems, newText: $newText")
    logDebug(logTag, "text in view: \"$text\"")

    if(newItems != null) {
        if(newItems != oldItems)
            setAdapter(
                ArrayAdapter(
                    context,
                    R.layout.drop_down_item,
                    newItems
                )
            )
    }

    if(text.toString() != newText) {
        setText(newText, false)
    }
}

@BindingAdapter("letters_category_wise")
fun RecyclerView.setLettersCategoryWise(newLettersCategoryWise: TransliteratedLetters?) {
    (adapter as? LetterCategoryAdapter)?.apply {
        if(currentList != newLettersCategoryWise?.categories) {
            logDebug(logTag, "Write letters category wise: $newLettersCategoryWise")
            submitList(newLettersCategoryWise?.categories)
        }
    } ?: logDebug(logTag, "LetterCategoryAdapter could not be accessed.")
}
