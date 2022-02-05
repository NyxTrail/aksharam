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
import android.widget.ExpandableListView;
import android.widget.ScrollView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LettersTabFragment extends Fragment {
    private String logTag = getClass().getName();

    private ExpandableListView categoriesList;

    // The main language for which letters are displayed
    private static String language = "ka";
    // The target language to transliterate to
    private static String targetLanguage = "ml";

    private static Transliterator transliterator;

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
        return transliterator;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (transliterator == null)
            transliterator = new Transliterator(language, getContext());

        return inflater.inflate(R.layout.letters_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialiseLettersTabLangSpinner(view);

        Log.d(logTag, LangDataReader.getCategories().toString());
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

        LabelledArrayAdapter<String> adapter = new LabelledArrayAdapter<>(getContext(),
                R.layout.spinner_item,
                R.id.spinnerItemTV,
                LangDataReader.getAvailableSourceLanguages(getContext()),
                R.id.spinnerLabelTV, getString(R.string.letters_tab_lang_input_hint));
        adapter.setDropDownViewResource(R.layout.spinner_drop_down);
        lettersTabLangSpinner.setAdapter(adapter);
        lettersTabLangSpinner.setSelection(0);
        lettersTabLangSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                language = parent.getItemAtPosition(position).toString();
                transliterator = new Transliterator(language, getContext());
                categoriesList.setAdapter(new LetterCategoryAdapter(getActivity()));
                for (int i = 0; i < categoriesList.getExpandableListAdapter()
                        .getGroupCount(); i++) {
                    categoriesList.expandGroup(i);
                }
                // At this point, LangDataReader should be re-initialised
                initialiseLettersTabTransSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void initialiseLettersTabTransSpinner() {
        Log.d(logTag, "setting up lettersTabTransSpinner");
        Spinner lettersTabTransSpinner = getActivity().findViewById(R.id.lettersTabTransSpinner);

        Log.d(logTag, LangDataReader.getTransLangs().toString());
        LabelledArrayAdapter<String> adapter = new LabelledArrayAdapter<>(getContext(),
                R.layout.spinner_item, R.id.spinnerItemTV,
                LangDataReader.getTransLangs(),
                R.id.spinnerLabelTV, getString(R.string.letters_tab_trans_hint));
        adapter.setDropDownViewResource(R.layout.spinner_drop_down);
        lettersTabTransSpinner.setAdapter(adapter);

        lettersTabTransSpinner.setSelection(0);
        lettersTabTransSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(logTag, "item selected: " + parent.getItemAtPosition(position).toString());
                targetLanguage = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}