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

import `in`.digistorm.aksharam.activities.initialise.InitialiseAppActivity
import `in`.digistorm.aksharam.activities.main.screens.LettersScreen
import `in`.digistorm.aksharam.activities.main.screens.PracticeScreen
import `in`.digistorm.aksharam.activities.main.screens.TransliterateScreen
import `in`.digistorm.aksharam.util.logDebug

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.themeadapter.material3.Mdc3Theme

class MainActivity : ComponentActivity() {
    private val logTag = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Aksharam()
        }
    }

    fun startInitialisationAcitivity() {
        logDebug(logTag, "MainActivity ending. Starting Initialisation Activity.")
        val intent = Intent(this, InitialiseAppActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
    @Composable
    private fun Aksharam() {
        val tabs = listOf("Letters", "Transliterate", "Practice")
        val viewModel = AksharamViewModel(tabs = tabs)
        val pagerState = rememberPagerState()

        Mdc3Theme {
            Scaffold(
                topBar = {
                    AksharamTopBar()
                }
            ) { paddingValues ->
                Column {
                    AksharamTabRow(
                        tabs = tabs,
                        setState = { id -> viewModel.tabState = id },
                        // Restore selected tab index from viewModel
                        // View model will initialise this to 0 if a state does not exist
                        selectedTabIndex = viewModel.tabState,
                        setPagerIndicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                            )
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                    HorizontalPager(count = tabs.size, state = pagerState) { tabIndex ->
                        when(tabIndex as Int) {
                            0 -> LettersScreen(
                                modifier = Modifier.fillMaxSize()
                                    .background(Color.Black)
                            )
                            1 -> TransliterateScreen(
                                modifier = Modifier.fillMaxSize()
                                    .background(Color.Blue)
                            )
                            2 -> PracticeScreen(
                                modifier = Modifier.fillMaxSize()
                                    .background(Color.Cyan)
                            )
                        }
                    }
                }
            }
        }
    }

    @Preview
    @Composable
    fun AksharamPreview() {
        Aksharam()
    }
}