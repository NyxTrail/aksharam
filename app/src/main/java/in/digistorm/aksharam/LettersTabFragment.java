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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class LettersTabFragment extends Fragment {
    public static final String ARG_OBJECT = "object";
    public ArrayList<String> categories;
    private String logTag = getClass().getName();

    private ExpandableListView categoriesList;

    // The main language for which letters are displayed
    private static String language = "ka";
    // The target language to transliterate to
    private static String targetLanguage = "ml";

    private static Transliterator tr;

    public static void setLettersTabFragmentLanguage(String lang) {
        // should add some sanity checks here
        language = lang;
    }

    public static String getLettersTabFragmentLanguage() {
        return language;
    }

    public static void setLettersTabFragmentTargetLanguage(String lang) {
        // should add some sanity checks here
        targetLanguage = lang;
    }
    public static String getLettersTabFragmentTargetLanguage() {
        return targetLanguage;
    }

    public static Transliterator getTransliterator() {
        return tr;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (tr == null)
            tr = new Transliterator(language, getContext());

        return inflater.inflate(R.layout.letters_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialiseLettersTabLangSpinner(view);
        initialiseLettersTabTransSpinner(view);

        Log.d(logTag, LangDataReader.getCategories().toString());
        // First, create an ExpandableListView for the categories
        /*
        ScrollView sv = new ScrollView(getContext());
        sv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
         */
        categoriesList = new ExpandableListView(getContext());
        categoriesList.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        categoriesList.setAdapter(new LetterCategoryAdapter(getActivity()));
        ScrollView sv = view.findViewById(R.id.LettersView);
        sv.addView(categoriesList);
        for (int i = 0; i < categoriesList.getExpandableListAdapter().getGroupCount(); i++) {
            categoriesList.expandGroup(i);
        }
    }

    public void initialiseLettersTabLangSpinner(@NonNull View view) {
        Spinner lettersTabLangSpinner = view.findViewById(R.id.lettersTabLangSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.language_selection_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lettersTabLangSpinner.setAdapter(adapter);
        lettersTabLangSpinner.setSelection(2);
        lettersTabLangSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (parent.getItemAtPosition(position).toString()) {
                    case "Malayalam":
                        language = "ml";
                        LangDataReader.initialise("malayalam.json", getContext());
                        categoriesList.setAdapter(new LetterCategoryAdapter(getActivity()));
                        for (int i = 0; i < categoriesList.getExpandableListAdapter().getGroupCount(); i++) {
                            categoriesList.expandGroup(i);
                        }
                        break;
                    case "Hindi":
                        language = "hi";
                        break;
                    case "Kannada":
                        language = "ka";
                        LangDataReader.initialise("kannada.json", getContext());
                        categoriesList.setAdapter(new LetterCategoryAdapter(getActivity()));
                        for (int i = 0; i < categoriesList.getExpandableListAdapter().getGroupCount(); i++) {
                            categoriesList.expandGroup(i);
                        }
                        break;
                    default:
                        // Something went wrong
                        Toast.makeText(getContext(),
                                "LettersTabFragment: Something went wrong!",
                                Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void initialiseLettersTabTransSpinner(@NonNull View view) {
        Spinner lettersTabTransSpinner = view.findViewById(R.id.lettersTabTransSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.language_selection_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lettersTabTransSpinner.setAdapter(adapter);
        lettersTabTransSpinner.setSelection(0);
        lettersTabTransSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (parent.getItemAtPosition(position).toString()) {
                    case "Malayalam":
                        targetLanguage = "ml";
                        break;
                    case "Hindi":
                        targetLanguage = "hi";
                        break;
                    default:
                        // Something went wrong
                        Toast.makeText(getContext(),
                                "LettersTabFragment: Something went wrong!",
                                Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}