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

import org.json.JSONObject;

import java.util.Locale;

public class LettersTabFragment extends Fragment {
    private final String logTag = "LettersTabFragment";

    private ExpandableListView categoriesList;

    // The main language for which letters are displayed
    private String language;
    // The target language to transliterate to
    private String targetLanguage;

    private Transliterator transliterator;

    private LabelledArrayAdapter<String> adapter;
    private Spinner lettersTabLangSpinner;

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
            transliterator = Transliterator.getDefaultTransliterator(getContext());

        return inflater.inflate(R.layout.letters_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialiseLettersTabLangSpinner(view);

        // set up the info button
        view.findViewById(R.id.lettersTabInfoButton).setOnClickListener(v -> {
            Log.d(logTag, "Info button clicked!");
            Spinner transSpinner = view.findViewById(R.id.lettersTabTransSpinner);
            String transLanguage = (String) transSpinner.getItemAtPosition(transSpinner.getSelectedItemPosition());
            Log.d(logTag, "Fetching info for " + transLanguage);
            JSONObject infoJSON = transliterator.getLangDataReader().getInfo(transLanguage, getContext());
            Log.d(logTag, infoJSON.toString());

            String info = infoJSON.optJSONObject("general").optString("en")
                    + infoJSON.optJSONObject(transLanguage.toLowerCase(Locale.ROOT))
                    .optString("en");
            LanguageInfoFragment lif = LanguageInfoFragment.newInstance(info);
            MainActivity.replaceTabFragment(0, lif);
        });

        Log.d(logTag, transliterator.getLangDataReader().getCategories().toString());
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
                transliterator.getLangDataReader().getAvailableSourceLanguages(getContext()),
                R.id.spinnerLabelTV, getString(R.string.letters_tab_lang_input_hint));
        adapter.setDropDownViewResource(R.layout.spinner_drop_down);
        adapter.setNotifyOnChange(true);
        lettersTabLangSpinner.setAdapter(adapter);
        lettersTabLangSpinner.setSelection(0);
        LettersTabFragment ltf = this;
        GlobalSettings.getInstance().addDataFileListChangedListener("LettersTabFragmentListener", () -> {
            Log.d("LTFListener", "Refreshing LettersTabFragment adapter");
            transliterator = new Transliterator(getContext());
            adapter.clear();
            // Invoke getAvailableSourceLanguages without Context object so that it does not
            // read the files again. The changed files have already been read into
            // LangDataReader when it was changed by the SettingsLanguageListAdapter
            adapter.addAll(transliterator.getLangDataReader().getAvailableSourceLanguages());
            // While the spinner shows updated text, its (Spinner's) getSelectedView() was sometimes returning
            // a non-existant item (say, if the item is deleted). Resetting the adapter was the only way I could
            // think of to fix this
            lettersTabLangSpinner.setAdapter(adapter);
        });
        lettersTabLangSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                language = parent.getItemAtPosition(position).toString();
                Log.d("LangSpinner", "Item selected " + language);
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

        Log.d(logTag, transliterator.getLangDataReader().getTransLangs().toString());
        LabelledArrayAdapter<String> adapter = new LabelledArrayAdapter<>(getContext(),
                R.layout.spinner_item, R.id.spinnerItemTV,
                transliterator.getLangDataReader().getTransLangs(),
                R.id.spinnerLabelTV, getString(R.string.letters_tab_trans_hint));
        adapter.setDropDownViewResource(R.layout.spinner_drop_down);
        lettersTabTransSpinner.setAdapter(adapter);

        lettersTabTransSpinner.setSelection(0);
        lettersTabTransSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("TransSpinner", "item selected: " + parent.getItemAtPosition(position).toString());
                targetLanguage = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
