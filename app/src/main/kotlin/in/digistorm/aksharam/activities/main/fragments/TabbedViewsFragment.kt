/*
 * Copyright (c) 2023 Alan M Varghese <alan@digistorm.in>
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
package `in`.digistorm.aksharam.activities.main.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.activities.main.PageCollectionAdapter
import `in`.digistorm.aksharam.databinding.FragmentTabbedViewsBinding
import `in`.digistorm.aksharam.activities.main.language.getDownloadedLanguages
import `in`.digistorm.aksharam.activities.main.util.IdlingResourceHelper
import `in`.digistorm.aksharam.activities.main.util.logDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TabbedViewsFragment: Fragment() {
    private val logTag = javaClass.simpleName
    private var tabHeads = arrayOf("Letters", "Transliterate", "Practice")

    private lateinit var binding: FragmentTabbedViewsBinding
    private var pageCollectionAdapter: PageCollectionAdapter? = null

    private class TabSelectedListener: OnTabSelectedListener {
        private var tabPosition = 0
        override fun onTabSelected(tab: TabLayout.Tab) {
            tabPosition = tab.position
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {}
        override fun onTabReselected(tab: TabLayout.Tab) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        logDebug(logTag, "onCreateView")
        savedInstanceState?.apply {
            logDebug(logTag, "saved instance received")
        }

        val binding = FragmentTabbedViewsBinding.inflate(inflater, container, false)
        this.binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        logDebug(logTag, "onViewCreated")
        goToInitialisationScreenIfNoDownloadedFiles()

        binding.moreOptions.setOnClickListener {
            PopupMenu(requireContext(), binding.moreOptions, Gravity.TOP).apply {
                inflate(R.menu.action_bar_menu)
                setOnMenuItemClickListener {
                    when(it.itemId) {
                        R.id.settingsFragment -> findNavController().navigate(
                            TabbedViewsFragmentDirections.actionTabbedViewsFragmentToSettingsFragment()
                        )
                        R.id.helpFragment -> findNavController().navigate(
                            TabbedViewsFragmentDirections.actionTabbedViewsFragmentToHelpFragment()
                        )
                        R.id.privacyPolicyFragment -> findNavController().navigate(
                            TabbedViewsFragmentDirections.actionTabbedViewsFragmentToPrivacyPolicyFragment()
                        )
                    }
                    true
                }
                show()
            }
        }
        pageCollectionAdapter = PageCollectionAdapter(this)
        binding.pager.adapter = pageCollectionAdapter
        binding.tabLayout.addOnTabSelectedListener(TabSelectedListener())
        TabLayoutMediator(
            binding.tabLayout,
            binding.pager
        ) { tab: TabLayout.Tab, position: Int -> tab.text = tabHeads[position] }
            .attach()

        if(!IdlingResourceHelper.countingIdlingResource.isIdleNow)
            IdlingResourceHelper.countingIdlingResource.decrement()
    }

    override fun onResume() {
        logDebug(logTag, "onResume")
        super.onResume()
        goToInitialisationScreenIfNoDownloadedFiles()
    }

    private fun goToInitialisationScreenIfNoDownloadedFiles() {
        CoroutineScope(Dispatchers.Default).launch {
            // if there are no downloaded files, switch to initialisation screen
            if (getDownloadedLanguages(requireContext()).isEmpty()) {
                logDebug(logTag, "No files found in data directory. Switching to initialisation screen.")
                withContext(Dispatchers.Main) {
                    if(findNavController().currentDestination?.id == R.id.tabbedViewsFragment)
                        findNavController().navigate(
                            TabbedViewsFragmentDirections.actionTabbedViewsFragmentToInitialisationScreen())
                    else
                        logDebug(logTag, "Already navigated to initialisation screen.")
                }
            }
        }
    }
}