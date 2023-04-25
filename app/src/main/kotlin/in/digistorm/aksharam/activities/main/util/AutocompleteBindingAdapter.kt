package `in`.digistorm.aksharam.activities.main.util

import android.widget.ArrayAdapter
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.activities.main.fragments.letters.LetterCategoryAdapter

private const val logTag: String = "AutoCompleteBindingAdapter"

@BindingAdapter(value = ["list", "android:text"], requireAll = false)
fun MaterialAutoCompleteTextView.writeSimpleItems(
    oldItems: List<String>?,
    oldText: String?,
    newItems: List<String>?, // TODO: null issues in practice tab
    newText: String?,
) {
    logDebug(logTag, "writeSimpleItems")
    logDebug(logTag, "oldItems: $oldItems, oldText: $oldText")
    logDebug(logTag, "newItems: $newItems, newText: $newText")

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
fun RecyclerView.setLettersCategoryWise(newLettersCategoryWise: List<Map<String, ArrayList<Pair<String, String>>>>) {
    logDebug(logTag, "Write letters category wise: $newLettersCategoryWise")
    if(adapter == null)
        logDebug(logTag, "Could not acquire adapter.")
    (adapter as? LetterCategoryAdapter)?.submitList(newLettersCategoryWise)
}
