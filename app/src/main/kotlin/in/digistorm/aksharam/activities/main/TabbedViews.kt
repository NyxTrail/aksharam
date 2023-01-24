package `in`.digistorm.aksharam.activities.main

import `in`.digistorm.aksharam.databinding.TabbedViewsBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator

class TabbedViews: Fragment() {
    private val logTag = javaClass.simpleName
    private var tabHeads = arrayOf("Letters", "Transliterate", "Practice")

    private lateinit var binding: TabbedViewsBinding
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
    ): View? {
        val binding = TabbedViewsBinding.inflate(inflater, container, false)
        this.binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pageCollectionAdapter = PageCollectionAdapter(this.requireActivity())
        binding.pager.adapter = pageCollectionAdapter
        binding.tabLayout.addOnTabSelectedListener(TabSelectedListener())
        TabLayoutMediator(
            binding.tabLayout,
            binding.pager
        ) { tab: TabLayout.Tab, position: Int -> tab.text = tabHeads[position] }
            .attach()
    }
}