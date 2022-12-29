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
import `in`.digistorm.aksharam.util.logDebug
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class LetterGridAdapter(
    private var letters: ArrayList<String>,
): ListAdapter<Pair<String, String>, LetterGridAdapter.LetterGridViewHolder>(LetterGridDiff()) {
    private val logTag: String = this.javaClass.simpleName

    private class LetterGridDiff: DiffUtil.ItemCallback<Pair<String,String>>() {
        private val logTag = this.javaClass.simpleName
        override fun areItemsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            logDebug(logTag, "Are oldItem: $oldItem and newItem: $newItem the same:" +
                    " ${oldItem == newItem}")
            return oldItem == newItem
        }

        // No contents for us here. The items are the contents
        override fun areContentsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            logDebug(logTag, "Are contents of oldItem: $oldItem and " +
                    "newItem: $newItem the same: true")
            return true
        }
    }

    class LetterGridViewHolder(letterView: LetterView): RecyclerView.ViewHolder(letterView) {
        val letterView: LetterView
        init {
            this.letterView = letterView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LetterGridViewHolder {
        val letterView: LetterView = LayoutInflater.from(parent.context)
                .inflate(R.layout.letter_view, parent, false) as LetterView
        return LetterGridViewHolder(letterView)
    }

    override fun onBindViewHolder(holder: LetterGridViewHolder, position: Int) {
        logDebug(logTag, "Binding letter: ${getItem(position).first} at position: $position")
        holder.letterView.letters = getItem(position)
    }

    override fun getItemCount(): Int {
        return letters.size
    }
}