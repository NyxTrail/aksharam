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
import `in`.digistorm.aksharam.databinding.ActivityMainBinding
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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController

class MainActivity : AppCompatActivity() {
    private val logTag = javaClass.simpleName

    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val navController = activityMainBinding.navHostFragmentContainer.getFragment<NavHostFragment>().navController
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        if (GlobalSettings.instance == null) GlobalSettings.createInstance(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.action_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        logDebug(logTag, "Action bar back button pressed!")
        val navController = findNavController(R.id.nav_host_fragment_container)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        logDebug(logTag, "Menu item clicked: $item , id: ${item.itemId}")
        val navController = findNavController(R.id.nav_host_fragment_container)
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    fun startInitialisationActivity() {
        logDebug(logTag, "MainActivity ending. Starting Initialisation Activity.")
        val intent = Intent(this, InitialiseAppActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    public override fun onResume() {
        super.onResume()

        // if there are no downloaded files, switch to Initialisation activity
        if (getDownloadedLanguages(this).isEmpty()) {
            logDebug(logTag, "No files found in data directory. Switching to initialisation activity.")
            startInitialisationActivity()
        }
    }
}