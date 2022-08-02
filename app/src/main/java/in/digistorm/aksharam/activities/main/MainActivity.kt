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

import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.util.GlobalSettings
import `in`.digistorm.aksharam.activities.htmlinfo.HTMLInfoActivity
import `in`.digistorm.aksharam.activities.settings.SettingsActivity
import `in`.digistorm.aksharam.activities.main.letters.LetterInfoFragment
import `in`.digistorm.aksharam.activities.initialise.InitialiseAppActivity
import `in`.digistorm.aksharam.util.getDownloadedLanguages
import `in`.digistorm.aksharam.util.logDebug

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity(), OnTabSelectedListener {
    private var tabPosition = 0
    private val logTag = javaClass.simpleName
    private var tabHeads: Array<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (tabHeads == null) {
            tabHeads = arrayOf(
                getText(R.string.letters_tab_header).toString(),
                getText(R.string.transliterate_tab_header).toString(),
                getText(R.string.practice_tab_header).toString()
            )
        }
        if (GlobalSettings.instance == null) GlobalSettings.createInstance(this)
        logDebug(logTag, "Initialising transliterator...")
        pageCollectionAdapter = PageCollectionAdapter(this)
        val viewPager = findViewById<ViewPager2>(R.id.pager)
        viewPager.adapter = pageCollectionAdapter
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        tabLayout.addOnTabSelectedListener(this)
        val tabLayoutMediator = TabLayoutMediator(
            tabLayout, viewPager
        ) { tab: TabLayout.Tab, position: Int -> tab.text = tabHeads!![position] }
        tabLayoutMediator.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.action_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        logDebug(logTag, "menuItem clicked: $item , id: $id")
        if (id == R.id.action_bar_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        } else if (id == R.id.dark_light_mode) {
            GlobalSettings.instance?.setDarkMode(!GlobalSettings.instance!!.darkMode, this)
            val mode = if (GlobalSettings.instance!!.darkMode
            ) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            for (t in supportFragmentManager.fragments) {
                /* LetterInfoFragment has to be re-initialised correctly when the uiMode *
                 * configuration change happens due to switching light/dark modes.       *
                 * We will simply close LetterInfoFragment if it is open. This should    *
                 * land us in Letters tab, if we were in LetterInfo when dark/light mode *
                 * was pressed.
                 */
                if (t is LetterInfoFragment) supportFragmentManager.beginTransaction().remove(t)
                    .commit()
            }
            AppCompatDelegate.setDefaultNightMode(mode)
        } else if (id == R.id.help) {
            val intent = Intent(this, HTMLInfoActivity::class.java)
            intent.putExtra(HTMLInfoActivity.EXTRA_NAME, HTMLInfoActivity.ExtraValues.HELP)
            startActivity(intent)
        } else if (id == R.id.privacy) {
            val intent = Intent(this, HTMLInfoActivity::class.java)
            intent.putExtra(HTMLInfoActivity.EXTRA_NAME, HTMLInfoActivity.ExtraValues.PRIVACY)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    fun startInitialisationAcitivity() {
        logDebug(logTag, "MainActivity ending. Starting Initialisation Activity.")
        val intent = Intent(this, InitialiseAppActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (!pageCollectionAdapter!!.restoreFragment(tabPosition)) super.onBackPressed()
    }

    public override fun onResume() {
        super.onResume()

        // if there are no downloaded files, switch to Initialisation activity
        if (getDownloadedLanguages(this).isEmpty()) {
            logDebug(logTag, "No files found in data directory. Switching to initialisation activity.")
            startInitialisationAcitivity()
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        tabPosition = tab.position
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}
    override fun onTabReselected(tab: TabLayout.Tab) {}

    companion object {
        private var pageCollectionAdapter: PageCollectionAdapter? = null
        @JvmStatic
        fun replaceTabFragment(index: Int, fragment: Fragment?) {
            pageCollectionAdapter!!.replaceFragment(index, fragment!!)
        }
    }
}