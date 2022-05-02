package in.digistorm.aksharam.activities.main.letters;
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
import android.os.Bundle;
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
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONObject;

import java.util.Locale;

import in.digistorm.aksharam.R;
import in.digistorm.aksharam.activities.main.MainActivity;
import in.digistorm.aksharam.util.GlobalSettings;
import in.digistorm.aksharam.util.LabelledArrayAdapter;
import in.digistorm.aksharam.util.Log;
import in.digistorm.aksharam.util.Transliterator;

public class LettersTabFragment extends Fragment {
    private final String logTag = "in.digistorm.aksharam.activities.main.letters.LettersTabFragment";

    private ExpandableListView categoriesList;

    // The main language for which letters are displayed
    private String language;
    // The target language to transliterate to
    private String targetLanguage;

    private LabelledArrayAdapter<String> adapter;
    private Spinner lettersTabLangSpinner;

    private LettersTabViewModel viewModel;

    // Default constructor, used by PageCollectionAdapter to initialise the
    // fragment. The Fragment's children views are created via its onCreate
    // methods.
    public LettersTabFragment() {
        super();
    }

    // A constructor for LetterInfoFragment for cases when it does not
    // have access to its parent in.digistorm.aksharam.activities.main.letters.LettersTabFragment
    public LettersTabFragment(Context context) {
        super();
        // transliterator = new Transliterator(context);
    }

    public String getLettersTabFragmentLanguage() {
        if(language == null) {
            if(viewModel != null)
                return viewModel.getLanguage();
        }
        return language;
    }

    public void setLettersTabFragmentTargetLanguage(String lang) {
        // should add some sanity checks here
        targetLanguage = lang;
    }

    public void setLettersTabFragmentLanguage(String lang) {
        // should add some sanity checks here
        language = lang;
    }

    public String getLettersTabFragmentTargetLanguage() {
        if(targetLanguage == null) {
            if (viewModel != null)
                return viewModel.getTargetLanguage();
        }
        return targetLanguage;
    }

    public Transliterator getTransliterator() {
        return viewModel.getTransliterator();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.letters_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(logTag, "onViewCreated");
        if(viewModel == null) {
            Log.d(logTag, "Creating View Model for in.digistorm.aksharam.activities.main.letters.LettersTabFragment");
            viewModel = new ViewModelProvider(requireActivity()).get(LettersTabViewModel.class);
        }
        // attempt to initialise stuff with view model
        language = viewModel.getLanguage();
        targetLanguage = viewModel.getTargetLanguage();
        adapter = viewModel.getAdapter();
        viewModel.getTransliterator(getContext());

        initialiseLettersTabLangSpinner(view);

        // set up the info button
        view.findViewById(R.id.lettersTabInfoButton).setOnClickListener(v -> {
            Log.d(logTag, "Info button clicked!");
            Spinner transSpinner = view.findViewById(R.id.lettersTabTransSpinner);
            String transLanguage = (String) transSpinner.getItemAtPosition(transSpinner.getSelectedItemPosition());
            Log.d(logTag, "Fetching info for " + transLanguage);
            JSONObject infoJSON = viewModel.getTransliterator().getLangDataReader().getInfo(transLanguage, getContext());
            Log.d(logTag, infoJSON.toString());

            String info = infoJSON.optJSONObject("general").optString("en")
                    + infoJSON.optJSONObject(transLanguage.toLowerCase(Locale.ROOT))
                    .optString("en");
            LanguageInfoFragment lif = LanguageInfoFragment.newInstance(info);
            MainActivity.replaceTabFragment(0, lif);
        });

        Log.d(logTag, viewModel.getTransliterator().getLangDataReader().getCategories().toString());
        categoriesList = new ExpandableListView(getContext());
        categoriesList.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        categoriesList.setAdapter(new LetterCategoryAdapter( this));
        ScrollView sv = view.findViewById(R.id.LettersView);
        sv.addView(categoriesList);
        for (int i = 0; i < categoriesList.getExpandableListAdapter().getGroupCount(); i++) {
            categoriesList.expandGroup(i);
        }
    }

    public void initialiseLettersTabLangSpinner(@NonNull View view) {
        Log.d(logTag, "Initialising lettersTabLangSpinner spinner");
        lettersTabLangSpinner = view.findViewById(R.id.lettersTabLangSpinner);

        // adapter = viewModel.getAdapter();
        // if(adapter == null) {
            adapter = new LabelledArrayAdapter<>(getContext(),
                    R.layout.spinner_item,
                    R.id.spinnerItemTV,
                    viewModel.getTransliterator().getLangDataReader().getAvailableSourceLanguages(getContext()),
                    R.id.spinnerLabelTV, getString(R.string.letters_tab_lang_input_hint));
            viewModel.setAdapter(adapter);
        // }
        adapter.setDropDownViewResource(R.layout.spinner_drop_down);
        adapter.setNotifyOnChange(true);
        lettersTabLangSpinner.setAdapter(adapter);
        if(viewModel.getTargetLanguage() != null) {
            if(adapter.getPosition(viewModel.getTargetLanguage()) != -1)
                lettersTabLangSpinner.setSelection(adapter.getPosition(viewModel.getTargetLanguage()));
            else
                lettersTabLangSpinner.setSelection(0);
        }
        else
            lettersTabLangSpinner.setSelection(0);
        LettersTabFragment ltf = this;
        GlobalSettings.getInstance().addDataFileListChangedListener("LettersTabFragmentListener", () -> {
            Log.d("LTFListener", "Refreshing in.digistorm.aksharam.activities.main.letters.LettersTabFragment adapter");
            if(getContext() == null)
                return;

            viewModel.getTransliterator(getContext());
            adapter.clear();
            adapter.addAll(viewModel.getTransliterator().getLangDataReader().getAvailableSourceLanguages(getContext()));
            // adapter.notifyDataSetChanged();
            // While the spinner shows updated text, its (Spinner's) getSelectedView() was sometimes returning
            // a non-existant item (say, if the item is deleted). Resetting the adapter was the only way I could
            // think of to fix this
            Log.d("LTFListener", "Resetting spinner adapter");
            lettersTabLangSpinner.setAdapter(adapter);
        });
        lettersTabLangSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                language = parent.getItemAtPosition(position).toString();
                viewModel.setLanguage(language);
                Log.d("LangSpinner", "Item selected " + language);
                // re-initialising Transliterator is not necessary if it already has been
                // re-initialised. We can know this is the case by checking the currently loaded
                // language
                if(viewModel.getTransliterator() == null)
                    viewModel.getTransliterator(language, getContext());

                else if (!viewModel.getTransliterator().getCurrentLang().toLowerCase(Locale.ROOT)
                            .equals(language.toLowerCase(Locale.ROOT)))
                    viewModel.getTransliterator(language, getContext());

                // what is the right way to pass the object reference?
                categoriesList.setAdapter(new LetterCategoryAdapter(ltf));
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

        Log.d(logTag, viewModel.getTransliterator().getLangDataReader().getTransLangs().toString());
        LabelledArrayAdapter<String> adapter = new LabelledArrayAdapter<>(getContext(),
                R.layout.spinner_item, R.id.spinnerItemTV,
                viewModel.getTransliterator().getLangDataReader().getTransLangs(),
                R.id.spinnerLabelTV, getString(R.string.letters_tab_trans_hint));
        adapter.setDropDownViewResource(R.layout.spinner_drop_down);
        lettersTabTransSpinner.setAdapter(adapter);

        if(viewModel.getTargetLanguage() == null)
            lettersTabTransSpinner.setSelection(0);
        else {
            if(adapter.getPosition(viewModel.getTargetLanguage()) == -1)
                lettersTabTransSpinner.setSelection(0);
            else
                lettersTabTransSpinner.setSelection(adapter.getPosition(viewModel.getTargetLanguage()));
        }
        lettersTabTransSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("TransSpinner", "item selected: " + parent.getItemAtPosition(position).toString());
                targetLanguage = parent.getItemAtPosition(position).toString();
                viewModel.setTargetLanguage(targetLanguage);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
