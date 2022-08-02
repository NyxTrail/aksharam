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
import `in`.digistorm.aksharam.util.AutoAdjustingTextView
import `in`.digistorm.aksharam.activities.main.MainActivity
import `in`.digistorm.aksharam.util.logDebug

import android.widget.BaseExpandableListAdapter
import android.annotation.SuppressLint
import android.graphics.Point
import android.view.ViewGroup
import android.widget.TextView
import android.graphics.Typeface
import androidx.annotation.RequiresApi
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnLongClickListener
import androidx.gridlayout.widget.GridLayout
import java.util.*

class LetterCategoryAdapter(
    private val viewModel: LettersTabViewModel,
    private val size: Point) : BaseExpandableListAdapter() {

    private val logTag = LetterCategoryAdapter::class.simpleName

    private val headers: Array<String> = viewModel.transliterator!!.language!!.lettersCategoryWise
        .keys.toTypedArray()


    override fun getGroupCount(): Int {
        return viewModel.transliterator!!.language!!.lettersCategoryWise.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        // Each category has a single child - a list of all letters in that category
        return 1
    }

    override fun getGroup(groupPosition: Int): Any {
        return headers[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any? {
        return viewModel.transliterator?.language?.lettersCategoryWise?.get(headers[groupPosition])
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    @SuppressLint("InflateParams")
    override fun getGroupView(
        groupPosition: Int, isExpanded: Boolean, convertView: View,
        parent: ViewGroup
    ): View {
        logDebug(logTag, "getting groupview for position $groupPosition")
        val letterCategoryHeaderTV =
            convertView.findViewById<TextView>(R.id.LetterCategoryHeaderText)
        // Set some padding on the left so that the text does not overwrite the expand indicator
        letterCategoryHeaderTV.setPadding(
            100,
            letterCategoryHeaderTV.paddingTop,
            letterCategoryHeaderTV.paddingRight,
            letterCategoryHeaderTV.paddingBottom
        )
        letterCategoryHeaderTV.setTypeface(null, Typeface.BOLD)
        letterCategoryHeaderTV.text =
            headers[groupPosition].uppercase(Locale.getDefault())
        letterCategoryHeaderTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f)
        return convertView
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun getChildView(
        groupPosition: Int, childPosition: Int, isLastChild: Boolean,
        convertView: View, parent: ViewGroup
    ): View {
        logDebug(logTag, "creating grid for group: $groupPosition")
        val gridLayout = convertView.findViewById<GridLayout>(R.id.LetterGrid)
        gridLayout.removeAllViews()
        gridLayout.isClickable = true

        val letters = viewModel.transliterator?.language
            ?.lettersCategoryWise?.get(headers[groupPosition])?.toTypedArray() ?: return convertView

        logDebug(logTag, "group is: " + Arrays.toString(letters))
        val cols = 5

        for ((i, letter) in letters.withIndex()) {
            val rowSpec = GridLayout.spec(i / cols, GridLayout.CENTER)
            val colSpec = GridLayout.spec(i % cols, GridLayout.CENTER)
            val tv = AutoAdjustingTextView(parent.context)
            tv.gravity = Gravity.CENTER
            tv.text = letter
            val tvLayoutParams = GridLayout.LayoutParams(rowSpec, colSpec)
            tvLayoutParams.width = size.x / 6
            var pixels = parent.resources.getDimensionPixelSize(R.dimen.letter_grid_tv_margin)
            tvLayoutParams.setMargins(pixels, pixels, pixels, pixels)
            tv.layoutParams = tvLayoutParams
            pixels = parent.resources.getDimensionPixelSize(R.dimen.letter_grid_tv_padding)
            tv.setPadding(pixels, pixels, pixels, pixels)
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            tv.setOnLongClickListener(OnLongClickListener { v: View? ->
                logDebug(logTag, "$letter long clicked!")
                val letterInfoFragment = LetterInfoFragment.newInstance(letter)
                MainActivity.replaceTabFragment(0, letterInfoFragment)
                true
            })
            tv.setOnClickListener(View.OnClickListener { v: View? ->
                logDebug(logTag, "$letter clicked!")
                if (tv.text.toString() == letter) {
                    if (!viewModel.getLanguage()
                            .equals(viewModel.targetLanguage, ignoreCase = true)
                    ) tv.text = viewModel.transliterator!!.transliterate(
                        letter,
                        viewModel.targetLanguage!!
                    ) else logDebug(logTag, "source lang = target lang... Error is data file?")
                } else tv.text = letter
            })
            gridLayout.addView(tv, tvLayoutParams)
        }
        return convertView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return false
    }
}