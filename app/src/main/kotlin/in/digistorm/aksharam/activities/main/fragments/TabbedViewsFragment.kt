package `in`.digistorm.aksharam.activities.main.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.activities.main.PageCollectionAdapter
import `in`.digistorm.aksharam.databinding.FragmentTabbedViewsBinding
import `in`.digistorm.aksharam.activities.main.language.getDownloadedLanguages
import `in`.digistorm.aksharam.activities.main.util.logDebug

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
    }

    override fun onResume() {
        logDebug(logTag, "onResume")
        super.onResume()
        goToInitialisationScreenIfNoDownloadedFiles()
    }

    private fun goToInitialisationScreenIfNoDownloadedFiles() {
        // if there are no downloaded files, switch to initialisation screen
        if (getDownloadedLanguages(requireContext()).isEmpty()) {
            logDebug(logTag, "No files found in data directory. Switching to initialisation screen.")
            findNavController().navigate(
                TabbedViewsFragmentDirections.actionTabbedViewsFragmentToInitialisationScreen())
            // if(findNavController().currentDestination?.id != R.id.initialisationScreen)
        }
    }
}