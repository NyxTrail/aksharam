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

import android.content.Context
import `in`.digistorm.aksharam.R
import `in`.digistorm.aksharam.databinding.ActivityMainBinding
import `in`.digistorm.aksharam.activities.main.util.logDebug

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.test.espresso.idling.CountingIdlingResource
import `in`.digistorm.aksharam.activities.main.fragments.initialise.InitialisationScreenDirections
import `in`.digistorm.aksharam.activities.main.util.IdlingResourceHelper

class MainActivity : AppCompatActivity() {
    private val logTag = javaClass.simpleName

    private val activityViewModel: ActivityViewModel by viewModels()
    private lateinit var activityMainBinding: ActivityMainBinding

    private val navController: NavController
        get() = findNavController(R.id.nav_host_fragment_container)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        activityViewModel.availableLanguages.observe(this) { languageFiles ->
            val navController = findNavController(activityMainBinding.navHostFragmentContainer.id)
            if(languageFiles.size > 0 && navController.currentDestination?.id == R.id.initialisationScreen) {
                findNavController(R.id.nav_host_fragment_container)
                    .navigate(InitialisationScreenDirections.actionInitialisationScreenToTabbedViewsFragment())
            }
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                logDebug(logTag, "on back pressed")
                val letterNavController = findViewById<FragmentContainerView>(R.id.letters_tab_contents)
                    ?.getFragment<NavHostFragment>()?.navController
                if(letterNavController != null &&
                    letterNavController.currentDestination?.id != R.id.lettersFragment
                ) {
                    letterNavController.popBackStack()
                }
                else {
                    if(navController.currentDestination?.id == R.id.tabbedViewsFragment)
                        finish()
                    else {
                        navController.navigateUp()
                        logDebug(
                            logTag,
                            "Current destination is ${navController.currentDestination}"
                        )
                    }
                }
            }

        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        val view = super.onCreateView(parent, name, context, attrs)
        if (parent != null) {
            ViewCompat.setOnApplyWindowInsetsListener(parent) { v, insets ->
                // https://developer.android.com/develop/ui/views/layout/edge-to-edge
                logDebug(logTag, "Setting inset to support edge to edge display.")
                val bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            or WindowInsetsCompat.Type.displayCutout()
                )
                logDebug(logTag, "Setting inset to " + bars.left + "," + bars.top)
                v.updatePadding(
                    left = bars.left,
                    top = bars.top,
                    right = bars.right,
                    bottom = bars.bottom,
                )
                WindowInsetsCompat.CONSUMED
            }
        }
        return view
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

    fun getIdlingResource(): CountingIdlingResource {
        return IdlingResourceHelper.countingIdlingResource
    }
}