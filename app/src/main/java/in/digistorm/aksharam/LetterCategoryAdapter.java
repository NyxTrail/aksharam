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

import android.content.Context;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import androidx.gridlayout.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

public class LetterCategoryAdapter extends BaseExpandableListAdapter {
    private final FragmentActivity activity;
    private final String logTag = "LetterCategoryAdapter";

    private final String[] headers;

    // The adapters copy of the parent fragment
    private LettersTabFragment lettersTabFragment;

    public LetterCategoryAdapter(FragmentActivity activity, LettersTabFragment ltf) {
        lettersTabFragment = ltf;
        headers = lettersTabFragment.getTransliterator().getLangDataReader()
                .getCategories().keySet().toArray(new String[0]);
        this.activity = activity;
    }

    @Override
    public int getGroupCount() {
        return lettersTabFragment.getTransliterator().getLangDataReader()
                .getCategories().size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        // Each category has a single child - a list of all letters in that category
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return headers[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return lettersTabFragment.getTransliterator().getLangDataReader()
                .getCategories().get(headers[groupPosition]);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
        Log.d(logTag, "getting groupview for position " + groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.letter_category_header, null);
        }

        TextView letterCategoryHeaderTV = convertView.findViewById(R.id.LetterCategoryHeaderText);
        // Set some padding on the left so that the text does not overwrite the expand indicator
        letterCategoryHeaderTV.setPadding(100,
                letterCategoryHeaderTV.getPaddingTop(),
                letterCategoryHeaderTV.getPaddingRight(),
                letterCategoryHeaderTV.getPaddingBottom());
        letterCategoryHeaderTV.setTypeface(null, Typeface.BOLD);
        letterCategoryHeaderTV.setText(headers[groupPosition].toUpperCase());
        letterCategoryHeaderTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);

        return convertView;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.letter_category_content, null);
        }
        Log.d(logTag, "creating grid for group: " + groupPosition);
        Log.d(logTag, "group is: " + lettersTabFragment.getTransliterator()
                .getLangDataReader().getCategories().get(headers[groupPosition]));
        GridLayout gridLayout = convertView.findViewById(R.id.LetterGrid);
        gridLayout.removeAllViews();
        gridLayout.setClickable(true);

        String[] letters = lettersTabFragment.getTransliterator().getLangDataReader()
                .getCategories().get(headers[groupPosition]).toArray(new String[0]);
        for(String letter: letters) {
            LinearLayout linearLayout = new LinearLayout(activity);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            try {
                display.getRealSize(size);
            } catch (NoSuchMethodError err) {
                display.getSize(size);
            }
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    size.x/6,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.setGravity(Gravity.CENTER);

            TextView tv = new TextView(activity);
            tv.setText(letter);
            ViewGroup.MarginLayoutParams tvLayoutParams = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            int pixels = lettersTabFragment.getResources().getDimensionPixelSize(R.dimen.letter_grid_margin);
            tvLayoutParams.setMargins(pixels, pixels, pixels, pixels);
            tv.setLayoutParams(tvLayoutParams);
            pixels = lettersTabFragment.getResources().getDimensionPixelSize(R.dimen.letter_grid_padding);
            tv.setPadding(pixels, pixels, pixels, pixels);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);

            linearLayout.setClickable(true);
            linearLayout.setLongClickable(true);
            linearLayout.setOnLongClickListener(v -> {
                Log.d(logTag, letter + " long clicked!");

                LetterInfoFragment letterInfoFragment = LetterInfoFragment.newInstance(letter, lettersTabFragment);
                MainActivity.replaceTabFragment(0, letterInfoFragment);
                return true;
            });

            linearLayout.setOnClickListener(v -> {
                Log.d(logTag, letter + " clicked!");
                if (tv.getText().toString().equals(letter)) {
                    if (!lettersTabFragment.getLettersTabFragmentLanguage().equalsIgnoreCase(
                            lettersTabFragment.getLettersTabFragmentTargetLanguage()))
                        tv.setText(lettersTabFragment.getTransliterator().transliterate(
                                letter,
                                lettersTabFragment.getLettersTabFragmentTargetLanguage()));
                    else
                        Log.d(logTag, "source lang = target lang");
                }
                else
                    tv.setText(letter);
            });

            linearLayout.addView(tv);
            gridLayout.addView(linearLayout);
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}