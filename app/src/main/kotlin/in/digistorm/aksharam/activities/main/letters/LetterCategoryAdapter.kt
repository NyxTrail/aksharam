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
package `in`.digistorm.aksharam.activities.main.letters

import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.util.ExpandableCardView
import `in`.digistorm.aksharam.util.Transliterator
import `in`.digistorm.aksharam.util.logDebug
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
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

    private fun findNextCategory(category: String): String? {
        logDebug(logTag, "Finding next category of $category")
        val indexOfCategory = categories.indexOf(category)
        if(indexOfCategory + 1 == categories.size)
            return null
        logDebug(logTag, "Next category: ${categories[indexOfCategory + 1]}")
        return categories[indexOfCategory + 1]
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

    class LetterCategoryCardViewHolder(
        val cardView: ExpandableCardView
    ): RecyclerView.ViewHolder(cardView) {
        init {
            cardView.initialize()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LetterCategoryCardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.letter_category, parent, false)
        return LetterCategoryCardViewHolder(view as ExpandableCardView)
    }

    override fun onBindViewHolder(holder: LetterCategoryCardViewHolder, position: Int) {
        logDebug(logTag, "Binding view for position: $position")
        logDebug(logTag, "Items: $currentList")
        holder.cardView.tag = getItem(position)

        // Give an ExpandableCardView the ability to find its sibling
        holder.cardView.findSiblings = fun () : MutableList<ExpandableCardView> {
            var nextCategory = findNextCategory(getItem(position))
            val siblingList = mutableListOf<ExpandableCardView>()
            var cardView: ExpandableCardView? = null
            // Collect all Card Views sequentially, until the first un-collapsed one
            while(nextCategory != null) {
                logDebug(logTag, "In loop")
                cardView = (holder.cardView.parent as View).findViewWithTag(nextCategory)
                        ?: break // We couldn't find the next category
                if(cardView.collapsed)
                    siblingList.add(cardView)
                else
                    break // We break at the first Card View that is not collapsed
                // Next category
                nextCategory = findNextCategory(nextCategory)
            }
            if(cardView != null && !cardView.collapsed)
                siblingList.add(cardView)

            return siblingList
        }

        holder.cardView.letterCategoryHeaderTextView.text = getItem(position).replaceFirstChar {
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
        letterCategoryCardViewHolder.cardView.letterGrid.apply {
            removeAllViews()

            logDebug(logTag, "position to get: $position")
            logDebug(logTag, "Categories are: ")
            categories.forEach {
                logDebug(logTag, it)
            }
            logDebug(logTag, "categories[position]: ${categories[position]}")
            for(letter in lettersCategoryWise[categories[position]]!!) {
                val letterView: LetterView = LayoutInflater
                    .from(letterCategoryCardViewHolder.cardView.letterGrid.context)
                    .inflate(R.layout.letter_view,
                        letterCategoryCardViewHolder.cardView.letterGrid, false) as LetterView
                letterView.setOnLongClickListener {
                    logDebug(logTag, "$letter long clicked!")
                    val letterInfoFragment = LetterInfoFragment(
                        letter,
                        targetLanguage,
                        transliterator,
                    )
                    // MainActivity.replaceTabFragment(0, letterInfoFragment)
                    true
                }
                letterView.letters = Pair(letter, transliterator.transliterate(letter, targetLanguage))
                letterCategoryCardViewHolder.cardView.letterGrid.addView(letterView)
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
                        cardView.letterGrid.children.forEachIndexed { i, view ->
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