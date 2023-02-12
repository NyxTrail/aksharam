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
import `in`.digistorm.aksharam.databinding.LetterCategoryBinding
import `in`.digistorm.aksharam.util.ExpandableCardView
import `in`.digistorm.aksharam.util.logDebug
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavAction
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/* RecyclerView adapter for each category of letters (e.g. Vowels, Consonants, etc).
   Each category is displayed in a separate Material 3 Card View.
 */
class LetterCategoryAdapter(
    private var letterOnLongClickAction: (String) -> NavDirections
): ListAdapter<Map<String, List<Pair<String, String>>>, LetterCategoryAdapter.LetterCategoryCardViewHolder>(
    LetterCategoryDiff()
) {
    private val logTag = this.javaClass.simpleName

    init {
        logDebug(logTag, "LetterCategoryAdapter initialised with list: $currentList")
    }

    private class LetterCategoryDiff: DiffUtil.ItemCallback<Map<String, List<Pair<String, String>>>>() {
        private val logTag = this.javaClass.simpleName
        override fun areItemsTheSame(
            oldItem: Map<String, List<Pair<String, String>>>,
            newItem: Map<String, List<Pair<String, String>>>
        ): Boolean {
            logDebug(logTag, "Are oldItem: $oldItem and newItem: $newItem the same:" +
                    " ${oldItem == newItem}")
            /**
             * Items are same if:
             * 1. their keys are same,
             * 2. for each value in the Arraylist, first of the pair are the same
             **/
            val oldCategory = oldItem.keys.singleOrNull()
            val newCategory = newItem.keys.singleOrNull()
            if(oldCategory == newCategory) {
                val category = newCategory
                oldItem[category]!!.forEachIndexed { i, letterPair ->
                    if(letterPair.first != newItem[category]!![i].first)
                        return false
                }
            }
            else
                return false
            return true
        }

        // Iff items are same, the system checks whether their contents are also same
        override fun areContentsTheSame(
            oldItem: Map<String, List<Pair<String, String>>>,
            newItem: Map<String, List<Pair<String, String>>>
        ): Boolean {
            logDebug(logTag, "Are contents of oldItem: $oldItem and " +
                    "newItem: $newItem the same: false")
            return false
        }
    }

    class LetterCategoryCardViewHolder(
        private val letterCategoryBinding: LetterCategoryBinding
    ): RecyclerView.ViewHolder(letterCategoryBinding.root) {
        val expandableCardView: ExpandableCardView
            get() = letterCategoryBinding.expandableCardView!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LetterCategoryCardViewHolder {
//  TODO:      val view = LayoutInflater.from(parent.context).inflate(R.layout.letter_category, parent, false)
        val letterCategoryBinding = LetterCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LetterCategoryCardViewHolder(letterCategoryBinding)
    }

    override fun onBindViewHolder(holder: LetterCategoryCardViewHolder, position: Int) {
        logDebug(logTag, "Binding view for position: $position")
        logDebug(logTag, "Item: ${getItem(position)}")
        holder.expandableCardView.tag = getItem(position).keys.first()
        holder.expandableCardView.initialise()

        // Give an ExpandableCardView the ability to find its sibling, so that they are animated correctly
        holder.expandableCardView.findSiblings = fun () : MutableList<ExpandableCardView> {
            var nextCategory = currentList[position + 1]
            val siblingList = mutableListOf<ExpandableCardView>()
            var cardView: ExpandableCardView? = null
            // Collect all Card Views sequentially, until the first un-collapsed one
            while(nextCategory != null) {
                logDebug(logTag, "In loop")
                cardView = (holder.expandableCardView.parent as View).findViewWithTag(nextCategory.keys.first())
                        ?: break // We couldn't find the next category
                if(cardView.collapsed)
                    siblingList.add(cardView)
                else
                    break // We break at the first Card View that is not collapsed
                // Next category
                nextCategory = currentList[position + 1]
            }
            if(cardView != null && !cardView.collapsed)
                siblingList.add(cardView)

            return siblingList
        }

        holder.expandableCardView.letterCategoryHeaderTextView.text = getItem(position).keys.first().replaceFirstChar {
            if(it.isLowerCase())
                it.titlecase()
            else
                it.toString()
        }

        initialiseLetterGrid(holder, position)
    }

    /* Initialises the LetterGrid for a category of letters (i.e, this is run once for every
       category.) */
    private fun initialiseLetterGrid(
        letterCategoryCardViewHolder: LetterCategoryCardViewHolder,
        position: Int,
    ) {
        letterCategoryCardViewHolder.expandableCardView.letterGrid.apply {
            removeAllViews()

            logDebug(logTag, "Position to get: $position")
            val category = getItem(position).keys.first()
            val letterPairs: List<Pair<String, String>> = getItem(position)[category] ?: ArrayList()
            logDebug(logTag, "Current category: $category")
            logDebug(logTag, "Letter Pairs: $letterPairs")
            for(letterPair in letterPairs) {
                val letterView: LetterPairView = LayoutInflater
                    .from(letterCategoryCardViewHolder.expandableCardView.letterGrid.context)
                    .inflate(R.layout.letter_pair_view,
                        letterCategoryCardViewHolder.expandableCardView.letterGrid, false) as LetterPairView
                // letterView.setOnLongClickListener(letterOnLongClickListener(letterPair.first))
                letterView.setOnLongClickListener { view ->
                    view.findNavController().navigate(letterOnLongClickAction(letterPair.first))
                    true
                }
                letterView.letters = letterPair
                letterCategoryCardViewHolder.expandableCardView.letterGrid.addView(letterView)
            }
        }
    }
}