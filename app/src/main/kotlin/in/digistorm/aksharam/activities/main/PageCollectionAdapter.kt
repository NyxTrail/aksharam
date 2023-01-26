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
package `in`.digistorm.aksharam.activities.main

import `in`.digistorm.aksharam.activities.main.letters.LettersTabFragment
import `in`.digistorm.aksharam.activities.main.practice.PracticeTabFragment
import `in`.digistorm.aksharam.activities.main.transliterate.TransliterateTabFragment
import `in`.digistorm.aksharam.util.logDebug

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.*

class PageCollectionAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private val logTag = javaClass.simpleName
    private val fragments = ArrayList<Fragment>()
    private val backStack = ArrayList<Stack<Fragment>>()
    override fun getItemId(position: Int): Long {
        return getIdForFragment(fragments[position])
    }

    override fun containsItem(itemId: Long): Boolean {
        for (fragment in fragments) {
            if (getIdForFragment(fragment) == itemId) return true
        }
        return false
    }

    private fun getIdForFragment(fragment: Fragment): Long {
        return fragment.javaClass.name.hashCode().toLong()
    }

    override fun createFragment(position: Int): Fragment {
        logDebug(logTag, "position: $position")
        logDebug(logTag, fragments[position].javaClass.name)
        return fragments[position]
    }

    fun replaceFragment(index: Int, fragment: Fragment) {
        logDebug(logTag, "replacing fragment at index $index")
        backStack[index].push(fragments[index])
        fragments[index] = fragment
        notifyItemChanged(index)
        // mainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /* Returns true if something is actually replaced
       else returns false
    */
    fun restoreFragment(index: Int): Boolean {
        logDebug(logTag, "Restoring fragment at index: $index")
        return if (!backStack[index].isEmpty()) {
            fragments[index] = backStack[index].pop()
            notifyItemChanged(index)
            true
        } else false
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    init {
        logDebug(logTag, "Setting up fragments")

        // there is a separate back stack for each tab
        fragments.add(LettersTabFragment())
        backStack.add(Stack())
        fragments.add(TransliterateTabFragment())
        backStack.add(Stack())
        fragments.add(PracticeTabFragment())
        backStack.add(Stack())
    }
}
