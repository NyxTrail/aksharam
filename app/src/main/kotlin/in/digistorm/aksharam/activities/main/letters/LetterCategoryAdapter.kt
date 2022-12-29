package `in`.digistorm.aksharam.activities.main.letters

import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.util.Transliterator
import `in`.digistorm.aksharam.util.logDebug
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
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

    // A list of references to currently active view holders
    private var letterCategoryViewHolders: MutableList<LetterCategoryCardViewHolder> = mutableListOf()

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
        val letterGrid: RecyclerView
        var letterGridAdapter: LetterGridAdapter? = null

        init {
            categoryHeader = cardView.findViewById(R.id.letter_category_header)
            letterCategoryHeaderText = cardView.findViewById(R.id.letter_category_header_text)
            letterGrid = cardView.findViewById(R.id.letter_grid)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LetterCategoryCardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.letter_category, parent, false)
        // Populate the gridLayout with the required number of
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

        if(letterCategoryViewHolders.size <= position)
            letterCategoryViewHolders.add(position, holder)
        else
            letterCategoryViewHolders[position] = holder

        initialiseLetterGrid(holder, position)
    }

    /* Initialises the LetterGrid for a category of letters (i.e, this is run once for every
       ViewHolder. */
    private fun initialiseLetterGrid(
        letterCategoryCardViewHolder: LetterCategoryCardViewHolder,
        position: Int,
    ) {
        letterCategoryCardViewHolder.letterGrid.apply {
            val mAdapter = LetterGridAdapter(lettersCategoryWise[getItem(position)]!!)

            // Create a list of letter, transliteration pairs
            lettersCategoryWise[getItem(position)]?.map {
                Pair(it, transliterator.transliterate(it, targetLanguage))
            }.let {
                logDebug(logTag, "Submitting list $it for category: ${getItem(position)}")
                mAdapter.submitList(it!!)
            }

            adapter = mAdapter
        }
        letterCategoryCardViewHolder.letterGridAdapter =
            letterCategoryCardViewHolder.letterGrid.adapter as LetterGridAdapter
    }

    fun updateTargetLanguage(language: String) {
        targetLanguage = language
        logDebug(logTag, "TargeLanguage updated to $targetLanguage")
    }

    fun updateTransliterator(transliterator: Transliterator) {
        this.transliterator = transliterator
        logDebug(logTag, "Transliterator updated")
    }

    fun updateLetterGrids() {
        logDebug(logTag, "Updating letter grids...")
        logDebug(logTag, "Size of CardViewHolder list: ${letterCategoryViewHolders.size}")

        if(letterCategoryViewHolders.size > 0) {
            for ((i: Int, viewHolder: LetterCategoryCardViewHolder) in letterCategoryViewHolders.withIndex()) {
                logDebug(logTag, "Creating letter list for ${categories[i]}")
                lettersCategoryWise[categories[i]]?.map {
                    Pair(it, transliterator.transliterate(it, targetLanguage))
                }.let {
                    logDebug(logTag, "Submitting list $it for category: " +
                            viewHolder.letterCategoryHeaderText.toString().lowercase()
                    )
                    viewHolder.letterGridAdapter!!.submitList(it!!)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return lettersCategoryWise.size
    }
}