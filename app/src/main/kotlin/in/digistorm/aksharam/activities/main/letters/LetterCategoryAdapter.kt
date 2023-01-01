package `in`.digistorm.aksharam.activities.main.letters

import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.activities.main.MainActivity
import `in`.digistorm.aksharam.util.Transliterator
import `in`.digistorm.aksharam.util.logDebug
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.gridlayout.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.size
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/* RecyclerView adapter for each category of letters (e.g. Vowels, Consonants, etc).
   Each category is displayed in a separate Material 3 Card View.
 */
class LetterCategoryAdapter(
    private var lettersCategoryWise: LinkedHashMap<String, ArrayList<String>>,
    private var transliterator: Transliterator,
    private var targetLanguage: String,
): ListAdapter<String, LetterCategoryAdapter.LetterCategoryCardViewHolder>(
    LetterCategoryDiff()
) {
    private val logTag = this.javaClass.simpleName

    private val categories: Array<String>
        get() {
            return lettersCategoryWise.keys.toTypedArray()
        }

    fun setLettersCategoryWise(letters: LinkedHashMap<String, ArrayList<String>>) {
        lettersCategoryWise = letters
    }

    private class LetterCategoryDiff: DiffUtil.ItemCallback<String>() {
        private val logTag = this.javaClass.simpleName
        override fun areItemsTheSame(
            oldItem: String,
            newItem: String
        ): Boolean {
            logDebug(logTag, "Are oldItem: $oldItem and newItem: $newItem the same:" +
                    " ${oldItem == newItem}")
            return oldItem == newItem
        }

        // Iff items are same, the system checks whether their contents are also same
        override fun areContentsTheSame(
            oldItem: String,
            newItem: String
        ): Boolean {
            logDebug(logTag, "Are contents of oldItem: $oldItem and " +
                    "newItem: $newItem the same: false")
            return false
        }
    }

    class LetterCategoryCardViewHolder(cardView: View): RecyclerView.ViewHolder(cardView) {
        val categoryHeader: ConstraintLayout
        val letterCategoryHeaderText: TextView
        val letterGrid: GridLayout

        init {
            categoryHeader = cardView.findViewById(R.id.letter_category_header)
            letterCategoryHeaderText = cardView.findViewById(R.id.letter_category_header_text)
            letterGrid = cardView.findViewById(R.id.letter_grid)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LetterCategoryCardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.letter_category, parent, false)
        return LetterCategoryCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LetterCategoryCardViewHolder, position: Int) {
        logDebug(logTag, "Binding view for position: $position")
        logDebug(logTag, "Items: $currentList")
        holder.letterCategoryHeaderText.text = getItem(position).replaceFirstChar {
            if(it.isLowerCase())
                it.titlecase()
            else
                it.toString()
        }

        initialiseLetterGrid(holder, position)
    }

    /* Initialises the LetterGrid for a category of letters (i.e, this is run once for every
       ViewHolder. */
    private fun initialiseLetterGrid(
        letterCategoryCardViewHolder: LetterCategoryCardViewHolder,
        position: Int,
    ) {
        letterCategoryCardViewHolder.letterGrid.apply {
            removeAllViews()

            for(letter in lettersCategoryWise[categories[position]]!!) {
                val letterView: LetterView = LayoutInflater
                    .from(letterCategoryCardViewHolder.letterGrid.context)
                    .inflate(R.layout.letter_view,
                        letterCategoryCardViewHolder.letterGrid, false) as LetterView
                letterView.setOnLongClickListener {
                    logDebug(logTag, "$letter long clicked!")
                    val letterInfoFragment = LetterInfoFragment(
                        letter,
                        targetLanguage,
                        transliterator,
                    )
                    MainActivity.replaceTabFragment(0, letterInfoFragment)
                    true
                }
                letterView.letters = Pair(letter, transliterator.transliterate(letter, targetLanguage))
                letterCategoryCardViewHolder.letterGrid.addView(letterView)
            }
        }
    }

    fun updateTargetLanguage(language: String) {
        targetLanguage = language
        logDebug(logTag, "TargetLanguage updated to $targetLanguage")
    }

    fun updateTransliterator(transliterator: Transliterator) {
        this.transliterator = transliterator
        logDebug(logTag, "Transliterator updated")
    }

    fun updateLetterGrids(categoryView: RecyclerView) {
        logDebug(logTag, "Updating letter grids...")

        for((i, category) in categories.withIndex()) {
            logDebug(logTag, "Finding viewHolder for adapter position: $i")
            (categoryView.findViewHolderForAdapterPosition(i) as? LetterCategoryCardViewHolder?)?.apply {
                lettersCategoryWise[category]!!.map {
                    Pair(it, transliterator.transliterate(it, targetLanguage))
                }.let {
                        logDebug(logTag, "List contains: $it")
                        letterGrid.children.forEachIndexed { i, view ->
                            (view as LetterView).letters = it[i]
                        }
                    }
                }
            }
    }

    override fun getItemCount(): Int {
        return lettersCategoryWise.size
    }
}