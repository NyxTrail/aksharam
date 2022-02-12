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
import android.widget.ExpandableListView;
import android.widget.ScrollView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class LettersTabFragment extends Fragment {
    private String logTag = getClass().getName();

    private ExpandableListView categoriesList;

    // The main language for which letters are displayed
    private String language = "Kannada";
    // The target language to transliterate to
    private String targetLanguage = "ml";

    private Transliterator transliterator;

    private LabelledArrayAdapter<String> adapter;
    private Spinner lettersTabLangSpinner;

    public class AdapterNotifier implements OnLangDataReaderChanged{
        @Override
        public void onLangDataReaderChanged() {
            Log.d(logTag, "onLangDataReaderChanged invoked!");

            // First set LettersTabLangSpinner correctly
            if(lettersTabLangSpinner != null) {
                String currentLang = Transliterator.getLangDataReader().getCurrentLang();
                currentLang = currentLang.substring(0, 1).toUpperCase(Locale.ROOT) +
                        currentLang.substring(1);
                Log.d(logTag, "onLangDataReaderChanged: current lang is " + currentLang);
                int index = Transliterator.getLangDataReader().getAvailableSourceLanguages().indexOf(currentLang);
                lettersTabLangSpinner.setSelection(index);
            }
        }
    }

    public void setLettersTabFragmentLanguage(String lang) {
        // should add some sanity checks here
        language = lang;
    }

    public String getLettersTabFragmentLanguage() {
        return language;
    }

    public void setLettersTabFragmentTargetLanguage(String lang) {
        // should add some sanity checks here
        targetLanguage = lang;
    }
    public String getLettersTabFragmentTargetLanguage() {
        return targetLanguage;
    }

    public Transliterator getTransliterator() {
        return transliterator;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (transliterator == null)
            transliterator = new Transliterator(language, new AdapterNotifier(), getContext());

        return inflater.inflate(R.layout.letters_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialiseLettersTabLangSpinner(view);

        Log.d(logTag, Transliterator.getLangDataReader().getCategories().toString());
        categoriesList = new ExpandableListView(getContext());
        categoriesList.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        categoriesList.setAdapter(new LetterCategoryAdapter(getActivity(), this));
        ScrollView sv = view.findViewById(R.id.LettersView);
        sv.addView(categoriesList);
        for (int i = 0; i < categoriesList.getExpandableListAdapter().getGroupCount(); i++) {
            categoriesList.expandGroup(i);
        }
    }

    public void initialiseLettersTabLangSpinner(@NonNull View view) {
        Log.d(logTag, "Initialising lettersTabLangSpinner spinner");
        lettersTabLangSpinner = view.findViewById(R.id.lettersTabLangSpinner);

        adapter = new LabelledArrayAdapter<>(getContext(),
                R.layout.spinner_item,
                R.id.spinnerItemTV,
                Transliterator.getLangDataReader().getAvailableSourceLanguages(getContext()),
                R.id.spinnerLabelTV, getString(R.string.letters_tab_lang_input_hint));
        adapter.setDropDownViewResource(R.layout.spinner_drop_down);
        lettersTabLangSpinner.setAdapter(adapter);
        lettersTabLangSpinner.setSelection(0);
        LettersTabFragment ltf = this;
        lettersTabLangSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                language = parent.getItemAtPosition(position).toString();
                // re-initialising Transliterator is not necessary if it already has been
                // re-initialised. We can know this is the case by checking the currently loaded
                // language
                if(transliterator == null)
                    transliterator = new Transliterator(language, getContext());

                else if (!transliterator.getCurrentLang().toLowerCase(Locale.ROOT)
                            .equals(language.toLowerCase(Locale.ROOT)))
                        transliterator = new Transliterator(language, getContext());

                // what is the right way to pass the object reference?
                categoriesList.setAdapter(new LetterCategoryAdapter(getActivity(), ltf));
                for (int i = 0; i < categoriesList.getExpandableListAdapter()
                        .getGroupCount(); i++) {
                    categoriesList.expandGroup(i);
                }
                // At this point, langDataReader should be re-initialised
                initialiseLettersTabTransSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void initialiseLettersTabTransSpinner() {
        Log.d(logTag, "Initialising lettersTabTransSpinner");
        Spinner lettersTabTransSpinner = getActivity().findViewById(R.id.lettersTabTransSpinner);

        Log.d(logTag, Transliterator.getLangDataReader().getTransLangs().toString());
        LabelledArrayAdapter<String> adapter = new LabelledArrayAdapter<>(getContext(),
                R.layout.spinner_item, R.id.spinnerItemTV,
                Transliterator.getLangDataReader().getTransLangs(),
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