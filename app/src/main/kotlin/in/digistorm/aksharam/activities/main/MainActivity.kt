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
import `in`.digistorm.aksharam.databinding.ActivityMainBinding
import `in`.digistorm.aksharam.activities.main.util.logDebug

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import `in`.digistorm.aksharam.activities.main.fragments.initialise.InitialisationScreenDirections

class MainActivity : AppCompatActivity() {
    private val logTag = javaClass.simpleName

    private val activityViewModel: ActivityViewModel by viewModels()
    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        logDebug(logTag, "onCreate")
        super.onCreate(savedInstanceState)

        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        activityViewModel.availableLanguages.observe(this) { languageFiles ->
            val navController = findNavController(R.id.nav_host_fragment_container)
            if(languageFiles.size > 0 && navController.currentDestination?.id == R.id.initialisationScreen) {
                findNavController(R.id.nav_host_fragment_container)
                    .navigate(InitialisationScreenDirections.actionInitialisationScreenToTabbedViewsFragment())
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        logDebug(logTag, "Action bar back button pressed!")
        val navController = findNavController(R.id.nav_host_fragment_container)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    public override fun onResume() {
        logDebug(logTag, "onResume")
        super.onResume()
    }
}