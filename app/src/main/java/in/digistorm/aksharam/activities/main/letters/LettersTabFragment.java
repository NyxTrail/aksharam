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
import android.graphics.Point;
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

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import in.digistorm.aksharam.R;
import in.digistorm.aksharam.activities.main.MainActivity;
import in.digistorm.aksharam.util.GlobalSettings;
import in.digistorm.aksharam.util.LabelledArrayAdapter;
import in.digistorm.aksharam.util.LangDataReader;
import in.digistorm.aksharam.util.Log;
import in.digistorm.aksharam.util.Transliterator;

public class LettersTabFragment extends Fragment {
    private final String logTag = getClass().getSimpleName();

    private ExpandableListView categoriesList;

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
    }

    public String getLettersTabFragmentLanguage() {
        return viewModel.getLanguage();
    }

    public String getLettersTabFragmentTargetLanguage() {
        return viewModel.getTargetLanguage();
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
            Log.d(logTag, "Creating View Model for LettersTabFragment");
            viewModel = new ViewModelProvider(requireActivity()).get(LettersTabViewModel.class);
        }
        adapter = viewModel.getAdapter();
        viewModel.resetTransliterator(getContext());

        initialiseLettersTabLangSpinner(view);

        // set up the info button
        view.findViewById(R.id.lettersTabInfoButton).setOnClickListener(v -> {
            Log.d(logTag, "Info button clicked!");
            Log.d(logTag, "Fetching info for transliterating " + viewModel.getLanguage() +
                    " to " + viewModel.getTargetLanguage());

            Map<String, Map<String, String>> info = viewModel.getTransliterator(getContext())
                    .getLanguage().getInfo();
            Log.d(logTag, info.toString());
            LanguageInfoFragment lif = LanguageInfoFragment.newInstance(
                    info.get("general").get("en") +
                            info.get(viewModel.getTargetLanguage().toLowerCase(Locale.ROOT)).get("en"));
            MainActivity.replaceTabFragment(0, lif);
        });

        Log.d(logTag, viewModel.getTransliterator().getLanguage().getLettersCategoryWise().toString());
        categoriesList = new ExpandableListView(getContext());
        categoriesList.setId(View.generateViewId());
        categoriesList.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        Point size = new Point();
        requireActivity().getWindowManager().getDefaultDisplay().getSize(size);
        categoriesList.setAdapter(new LetterCategoryAdapter(viewModel, size));
        ScrollView sv = view.findViewById(R.id.LettersView);
        sv.addView(categoriesList);
        for (int i = 0; i < categoriesList.getExpandableListAdapter().getGroupCount(); i++) {
            categoriesList.expandGroup(i);
        }
    }

    public void initialiseLettersTabLangSpinner(@NonNull View view) {
        Log.d(logTag, "Initialising LettersTabLangSpinner");
        lettersTabLangSpinner = view.findViewById(R.id.lettersTabLangSpinner);

        ArrayList<String> languages = LangDataReader.getDownloadedLanguages(requireContext());
        if(languages.size() == 0) {
            ((MainActivity) requireActivity()).startInitialisationAcitivity();
            return;
        }

        adapter = new LabelledArrayAdapter<>(requireContext(),
                R.layout.spinner_item,
                R.id.spinnerItemTV,
                languages,
                R.id.spinnerLabelTV, getString(R.string.letters_tab_lang_input_hint));
        viewModel.setAdapter(adapter);
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
        GlobalSettings.getInstance().addDataFileListChangedListener("LettersTabFragmentListener", () -> {
            Log.d("LTFListener", "Refreshing LettersTabFragment adapter");
            if(getContext() == null)
                return;

            viewModel.resetTransliterator(getContext());
            adapter.clear();
            ArrayList<String> lang = LangDataReader.getDownloadedLanguages(getContext());
            if(lang.size() == 0) {
                ((MainActivity) requireActivity()).startInitialisationAcitivity();
                return;
            }

            adapter.addAll(lang);
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
                String language = parent.getItemAtPosition(position).toString();
                Log.d("LangSpinner", "Item selected " + language);

                viewModel.setTransliterator(language, getContext());

                Point size = new Point();
                requireActivity().getWindowManager().getDefaultDisplay().getSize(size);
                categoriesList.setAdapter(new LetterCategoryAdapter(viewModel, size));
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
        Spinner lettersTabTransSpinner = requireActivity().findViewById(R.id.lettersTabTransSpinner);

        LabelledArrayAdapter<String> adapter = new LabelledArrayAdapter<>(requireContext(),
                R.layout.spinner_item, R.id.spinnerItemTV,
                viewModel.getTransliterator().getLanguage().getSupportedLanguagesForTransliteration(),
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
                String targetLanguage = parent.getItemAtPosition(position).toString();
                Log.d("TransSpinner", "item selected: " + targetLanguage);
                viewModel.setTargetLanguage(targetLanguage);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
