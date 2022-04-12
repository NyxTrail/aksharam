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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.Stack;

import in.digistorm.aksharam.util.Log;

public class PageCollectionAdapter extends FragmentStateAdapter {
    private final String logTag = "PageCollectionAdatper";
    private MainActivity mainActivity;

    private ArrayList<Fragment> fragments = new ArrayList<>();
    private ArrayList<Stack<Fragment>> backStack = new ArrayList<>();

    public ArrayList<Fragment> getFragments() {
        return fragments;
    }

    public PageCollectionAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.mainActivity = (MainActivity) fragmentActivity;

        Log.d(logTag, "Setting up fragments");

        // there is a separate back stack for each tab
        fragments.add(new LettersTabFragment());
        backStack.add(new Stack<>());
        fragments.add(new TransliterateTabFragment());
        backStack.add(new Stack<>());
        fragments.add(new PracticeTabFragment());
        backStack.add(new Stack<>());
    }


    @Override
    public long getItemId(int position) {
       return getIdForFragment(fragments.get(position));
    }

    @Override
    public boolean containsItem(long itemId) {
        for(Fragment fragment: fragments) {
            if(getIdForFragment(fragment) == itemId)
                return true;
        }
        return false;
    }

    private long getIdForFragment(Fragment fragment) {
        return fragment.getClass().getName().hashCode();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d(logTag, "position: " + position);
        Log.d(logTag, fragments.get(position).getClass().getName());
        switch(position) {
            case 0:
                return fragments.get(0);
            case 1:
                return fragments.get(1);
            case 2:
                return fragments.get(2);
            default:
                // something bad happened
                Log.d(logTag, "Invalid tab at position " + position);
                return fragments.get(0);
        }
    }

    public void replaceFragment(int index, Fragment fragment) {
        Log.d(logTag, "replacing fragment at index " + index);
        backStack.get(index).push(fragments.get(index));
        fragments.set(index, fragment);
        notifyItemChanged(index);
        // mainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /* Returns true if something is actually replaced
       else returns false
    */
    public boolean restoreFragment(int index) {
        Log.d(logTag, "Restoring fragment at index: " + index);
        if(!backStack.get(index).isEmpty()) {
            fragments.set(index, backStack.get(index).pop());
            notifyItemChanged(index);
//            if (backStack.get(index).isEmpty())
//                mainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            return true;
        }
        else
            return false;
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}
