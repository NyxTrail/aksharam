package in.digistorm.aksharam;

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

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class PageCollectionAdapter extends FragmentStateAdapter {
    private final String logTag = getClass().toString();

    public PageCollectionAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public static TabLayoutMediator.TabConfigurationStrategy tabConfigurationStrategy() {
        return (tab, position) -> {
            switch(position) {
                case 0:
                    tab.setText(R.string.letters_tab_header);
                    break;
                case 1:
                    tab.setText(R.string.transliterate_tab_header);
                    break;
            }
        };
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new LettersTabFragment();
                break;
            case 1:
                fragment = new TransliterateTabFragment();
                break;
            default:
                // something bad happened
                Log.d(logTag, "Invalid tab at position " + position);
                fragment = new LettersTabFragment();
                break;
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
