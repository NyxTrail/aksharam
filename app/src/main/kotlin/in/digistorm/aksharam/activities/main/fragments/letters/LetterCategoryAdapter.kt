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

import android.animation.ObjectAnimator
import android.util.SparseBooleanArray
import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.databinding.LetterCategoryBinding
import `in`.digistorm.aksharam.activities.main.util.logDebug
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.core.util.getOrDefault
import androidx.core.util.set
import androidx.core.view.ViewCompat
import androidx.gridlayout.widget.GridLayout
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.card.MaterialCardView
import `in`.digistorm.aksharam.activities.main.helpers.upperCaseFirstLetter
import `in`.digistorm.aksharam.activities.main.language.Category

/* RecyclerView adapter for each category of letters (e.g. Vowels, Consonants, etc).
   Each category is displayed in a separate Material 3 Card View.
 */
class LetterCategoryAdapter(
    private var letterOnLongClickAction: (String) -> NavDirections,
    private val cardStates: SparseBooleanArray = SparseBooleanArray()
): ListAdapter<Category, LetterCategoryAdapter.LetterCategoryCardViewHolder>(
    LetterCategoryDiff { cardStates }
) {
    private val logTag = this.javaClass.simpleName

    init {
        logDebug(logTag, "LetterCategoryAdapter initialised with list: $currentList")
    }

    private class LetterCategoryDiff(val getCardStates: (() -> SparseBooleanArray)): DiffUtil.ItemCallback<Category>() {
        private val logTag = this.javaClass.simpleName

        override fun areItemsTheSame(
            oldItem: Category,
            newItem: Category
        ): Boolean {
            logDebug(logTag, "Are oldItem: $oldItem and newItem: $newItem the same:" +
                    " ${oldItem == newItem}")
            /**
             * Items are same if:
             * 1. Their names are same,
             * 2. For each value in the Arraylist, first of the pair are the same
             *    This means only the transliteration language has been changed.
             **/
            if(oldItem.name == newItem.name) {
                oldItem.letterPairs.forEachIndexed { i, letterPair ->
                    if(letterPair.first != newItem.letterPairs[i].first) {
                        getCardStates.invoke().clear()
                        return false
                    }
                }
            }
            else
                return false
            return true
        }

        // Iff items are same, the system checks whether their contents are also same
        override fun areContentsTheSame(
            oldItem: Category,
            newItem: Category
        ): Boolean {
            logDebug(logTag, "Are contents of oldItem: $oldItem and " +
                    "newItem: $newItem the same: false")
            return false
        }
    }

    inner class LetterCategoryCardViewHolder(
        private val letterCategoryBinding: LetterCategoryBinding
    ): RecyclerView.ViewHolder(letterCategoryBinding.root) {
        // private val logTag = this.javaClass.simpleName

        private var position: Int = RecyclerView.NO_POSITION

        private val transition: Transition
            get() {
                return ChangeBounds()
                    .setDuration(300)
                    .setInterpolator(AccelerateDecelerateInterpolator())
            }

        val cardView: MaterialCardView by lazy {
            letterCategoryBinding.cardView
        }

        val letterGrid: GridLayout by lazy {
            letterCategoryBinding.letterGrid
        }

        fun getTag(): Any {
            return cardView.tag
        }

        private fun isCollapsed(): Boolean {
            return cardStates.getOrDefault(position, false)
        }

        var text: CharSequence
            get() = letterCategoryBinding.letterCategoryHeaderText.text
            set(value) {
                letterCategoryBinding.letterCategoryHeaderText.text = value
            }

        var findNextSiblings: (() -> MutableList<MaterialCardView>)? = null

        fun initialise(position: Int) {
            this.position = position

            if(isCollapsed()) {
                letterGrid.visibility = View.GONE
            } else {
                letterGrid.visibility = View.VISIBLE
            }

            letterCategoryBinding.apply {
                letterCategoryHeader.setOnClickListener {
                    val logTag = "categoryHeaderClickListener"
                    val imageView = downArrow

                    if (isCollapsed()) {
                        TransitionManager.beginDelayedTransition(
                            cardView.parent as ViewGroup,
                            transition.addTarget(cardView)
                        )
                        letterGrid.visibility = View.VISIBLE

                        ObjectAnimator.ofFloat(imageView, "rotation", 0f).apply {
                            duration = 300
                            start()
                        }
                        cardStates[position] = false
                    } else {
                        logDebug(logTag, "Finding siblings...")
                        val siblings: MutableList<MaterialCardView> = findNextSiblings?.invoke()!!
                        val newTransition: Transition = transition.apply {
                            logDebug(logTag, "adding siblings as target for animation")
                            logDebug(logTag, "Siblings size: ${siblings.size}")
                            siblings.forEach { sibling ->
                                logDebug(logTag, "Found CardView for: " +
                                        "${sibling.findViewById<TextView>(R.id.letter_category_header_text)?.text}")
                                addTarget(sibling)
                            }
                        }
                        TransitionManager.beginDelayedTransition(cardView.parent as ViewGroup, newTransition)
                        letterGrid.visibility = View.GONE
                        ObjectAnimator.ofFloat(imageView, "rotation", 180f).apply {
                            duration = 300
                            start()
                        }
                        cardStates[position] = true
                    }
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LetterCategoryCardViewHolder {
        val letterCategoryBinding = LetterCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LetterCategoryCardViewHolder(letterCategoryBinding)
    }

    override fun onBindViewHolder(holder: LetterCategoryCardViewHolder, position: Int) {
        logDebug(logTag, "Binding view for position: $position")
        logDebug(logTag, "Item: ${getItem(position)}")
        holder.cardView.tag = getItem(position).name
        holder.initialise(position)

        // Give an ExpandableCardView the ability to find its sibling, so that they are animated correctly
        holder.findNextSiblings = fun () : MutableList<MaterialCardView> {
            var pos = holder.bindingAdapterPosition
            return if(pos == RecyclerView.NO_POSITION) {
                logDebug(logTag, "bindingAdapterPosition was NO_POSITION")
                mutableListOf()
            } else {
                val siblingList = mutableListOf<MaterialCardView>()
                logDebug(logTag, "bindingAdapterPosition is $pos")
                while(++pos < itemCount) {
                    (((holder.cardView.parent as? RecyclerView)
                        ?.findViewHolderForAdapterPosition(pos)) as? LetterCategoryCardViewHolder)?.cardView?.let {
                        siblingList.add(it)
                    }
                }
                mutableListOf()
            }
        }

        holder.text = getItem(position).name.upperCaseFirstLetter()

        initialiseLetterGrid(holder, position)
    }

    /* Initialises the LetterGrid for a category of letters (i.e, this is run once for every
       category.) */
    private fun initialiseLetterGrid(
        letterCategoryCardViewHolder: LetterCategoryCardViewHolder,
        position: Int,
    ) {
        letterCategoryCardViewHolder.letterGrid.apply {
            removeAllViews()

            logDebug(logTag, "Initialising letter grid for position: $position")
            val category = getItem(position).name
            logDebug(logTag, "Current category: $category")
            logDebug(logTag, "Letter Pairs: ${getItem(position).letterPairs}")
            for(letterPair in getItem(position).letterPairs) {
                val letterView: LetterPairView = LayoutInflater
                    .from(letterCategoryCardViewHolder.letterGrid.context)
                    .inflate(R.layout.letter_pair_view,
                        letterCategoryCardViewHolder.letterGrid, false) as LetterPairView
                letterView.setOnLongClickListener { view ->
                    val extras = FragmentNavigatorExtras(letterView to "${letterPair.first}_heading")

                    view.findNavController().navigate(letterOnLongClickAction(letterPair.first), extras)
                    true
                }
                letterView.letters = letterPair
                letterView.tag = letterPair.first
                ViewCompat.setTransitionName(letterView, "${letterPair.first}_letter")
                letterCategoryCardViewHolder.letterGrid.addView(letterView)
            }
        }
    }
}
