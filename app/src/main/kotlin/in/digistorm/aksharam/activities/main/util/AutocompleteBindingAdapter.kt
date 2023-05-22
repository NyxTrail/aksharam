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
