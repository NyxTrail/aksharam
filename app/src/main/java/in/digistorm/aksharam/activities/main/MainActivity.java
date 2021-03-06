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

package in.digistorm.aksharam.activities.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import in.digistorm.aksharam.activities.htmlinfo.HTMLInfoActivity;
import in.digistorm.aksharam.activities.initialise.InitialiseAppActivity;
import in.digistorm.aksharam.activities.main.letters.LetterInfoFragment;
import in.digistorm.aksharam.R;
import in.digistorm.aksharam.activities.settings.SettingsActivity;
import in.digistorm.aksharam.util.LangDataReader;
import in.digistorm.aksharam.util.GlobalSettings;
import in.digistorm.aksharam.util.Log;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {
    private static PageCollectionAdapter pageCollectionAdapter;
    private int tabPosition;
    private final String logTag = getClass().getSimpleName();
    private String[] tabHeads;
    public static void replaceTabFragment(int index, Fragment fragment) {
        pageCollectionAdapter.replaceFragment(index, fragment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(tabHeads == null) {
            tabHeads = new String[]{
                    getText(R.string.letters_tab_header).toString(),
                    getText(R.string.transliterate_tab_header).toString(),
                    getText(R.string.practice_tab_header).toString()
            };
        }

        if(GlobalSettings.getInstance() == null)
            GlobalSettings.createInstance(this);

        Log.d(logTag, "Initialising transliterator...");

        pageCollectionAdapter = new PageCollectionAdapter(this);
        ViewPager2 viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(pageCollectionAdapter);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(this);


        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager,
                ((tab, position) -> tab.setText(tabHeads[position]))
        );
        tabLayoutMediator.attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Log.d(logTag, "menuItem clicked: " + item + " , id: " + id);
        if(id == R.id.action_bar_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.dark_light_mode) {
            GlobalSettings.getInstance().setDarkMode(!GlobalSettings.getInstance().getDarkMode(), this);
            int mode = GlobalSettings.getInstance().getDarkMode() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            for(Fragment t: getSupportFragmentManager().getFragments()) {
                /* LetterInfoFragment has to be re-initialised correctly when the uiMode *
                 * configuration change happens due to switching light/dark modes.       *
                 * We will simply close LetterInfoFragment if it is open. This should    *
                 * land us in Letters tab, if we were in LetterInfo when dark/light mode *
                 * was pressed.
                 */
                if(t instanceof LetterInfoFragment)
                    getSupportFragmentManager().beginTransaction().remove(t).commit();
            }

            AppCompatDelegate.setDefaultNightMode(mode);
        }
        else if(id == R.id.help) {
            Intent intent = new Intent(this, HTMLInfoActivity.class);
            intent.putExtra(HTMLInfoActivity.EXTRA_NAME, HTMLInfoActivity.EXTRA_VALUES.HELP);
            startActivity(intent);
        }
        else if(id == R.id.privacy) {
            Intent intent = new Intent(this, HTMLInfoActivity.class);
            intent.putExtra(HTMLInfoActivity.EXTRA_NAME, HTMLInfoActivity.EXTRA_VALUES.PRIVACY);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void startInitialisationAcitivity() {
        Log.d(logTag, "MainActivity ending. Starting Initialisation Activity.");
        Intent intent = new Intent(this, InitialiseAppActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (!pageCollectionAdapter.restoreFragment(tabPosition))
            super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();

        // if there are no downloaded files, switch to Initialisation activity
        if(LangDataReader.getDownloadedLanguages(this).isEmpty()) {
            Log.d(logTag, "No files found in data directory. Switching to initialisation activity.");
            startInitialisationAcitivity();
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        tabPosition = tab.getPosition();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
