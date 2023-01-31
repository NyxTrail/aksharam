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
package `in`.digistorm.aksharam.util

import `in`.digistorm.aksharam.R
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.gridlayout.widget.GridLayout
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.card.MaterialCardView

class ExpandableCardView: MaterialCardView {
    private val logTag = this.javaClass.simpleName

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int):
            super(context, attrs, defStyleAttr)

    val categoryHeader: ConstraintLayout by lazy {
        findViewById(R.id.letter_category_header)
    }
    val letterCategoryHeaderTextView: TextView by lazy {
        findViewById(R.id.letter_category_header_text)
    }
    val letterGrid: GridLayout by lazy {
        findViewById(R.id.letter_grid)
    }
    var collapsed: Boolean = false
    var findSiblings: (() -> MutableList<ExpandableCardView>)? = null
    private val transition: Transition
        get() {
            return ChangeBounds()
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
        }

    fun initialise() {
        categoryHeader.setOnClickListener {
            val logTag = "categoryHeaderClickListener"
            val imageView = it.findViewById<ImageView>(R.id.down_arrow)

            if (collapsed) {
                TransitionManager.beginDelayedTransition(
                    parent as ViewGroup,
                    transition.addTarget(this)
                )
                letterGrid.visibility = View.VISIBLE

                ObjectAnimator.ofFloat(imageView, "rotation", 0f).apply {
                    duration = 300
                    start()
                }
                collapsed = false
            } else {
                logDebug(logTag, "Finding siblings...")
                val siblings: MutableList<ExpandableCardView> = findSiblings?.invoke()!!
                val newTransition: Transition = transition.apply {
                    logDebug(logTag, "adding siblings as target for animation")
                    logDebug(logTag, "Siblings size: ${siblings.size}")
                    siblings.forEach { sibling ->
                        logDebug(logTag, "Found ECV for: ${sibling.letterCategoryHeaderTextView.text}")
                        addTarget(sibling)
                    }
                }
                TransitionManager.beginDelayedTransition(parent as ViewGroup, newTransition)
                letterGrid.visibility = View.GONE
                ObjectAnimator.ofFloat(imageView, "rotation", 180f).apply {
                    duration = 300
                    start()
                }
                collapsed = true
            }
        }
    }
}