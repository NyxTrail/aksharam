package in.digistorm.aksharam.activities.main.transliterate;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import in.digistorm.aksharam.R;
import in.digistorm.aksharam.util.LanguageDetector;
import in.digistorm.aksharam.util.Transliterator;
import in.digistorm.aksharam.util.GlobalSettings;
import in.digistorm.aksharam.util.LabelledArrayAdapter;
import in.digistorm.aksharam.util.Log;

public class TransliterateTabFragment extends Fragment {
    private String targetLanguage;
    private Transliterator tr;
    private boolean sourceChanged = true;

    private final String logTag = "TransTabFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.transliteration_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(tr == null) {
            tr = Transliterator.getDefaultTransliterator(getContext());
        }

        ((EditText) view.findViewById(R.id.TransliterateTabInputTextField))
                .addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 0) {
                    view.findViewById(R.id.LanguageSelectionSpinner).setEnabled(true);
                    view.findViewById(R.id.LanguageSelectionSpinner).setAlpha(1.0f);
                }
                else {
                    view.findViewById(R.id.LanguageSelectionSpinner).setEnabled(false);
                    view.findViewById(R.id.LanguageSelectionSpinner).setAlpha(0.3f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                sourceChanged = true;
            }
        });

        view.findViewById(R.id.TransliterateButton).setOnClickListener(
                this::transliterateButtonOnClick
        );
        initialiseSpinner(view);

        Spinner languageSelectionSpinner = view.findViewById(R.id.LanguageSelectionSpinner);
        languageSelectionSpinner.setEnabled(false);
        languageSelectionSpinner.setAlpha(0.3f);

        GlobalSettings.getInstance().addDataFileListChangedListener("TransliterateTabFragmentListener", () -> {
            Log.d(logTag, "Re-initialising spinners");
            initialiseSpinner(view);
        });
    }

    public void transliterateButtonOnClick(View view) {
        String inputString = ((EditText) ((View)view.getParent())
                .findViewById(R.id.TransliterateTabInputTextField)).getText().toString();
        Log.d(logTag, "Transliterating " + inputString + " to " + targetLanguage);

        if(sourceChanged) {
            sourceChanged = false;
            String lang = new LanguageDetector(getContext())
                    .detectLanguage(inputString, getContext());
            Log.d(logTag, "Detected language: " + lang);
            if(lang == null) {
                Toast.makeText(getContext(), R.string.lang_could_not_detect,
                        Toast.LENGTH_LONG).show();
            }
            else if(!lang.equalsIgnoreCase(tr.getCurrentLang())) {
                tr = new Transliterator(lang, getContext());
                initialiseSpinner(null);
            }
            Log.d(logTag, lang + "; " + tr.getCurrentLang());
        }

        // Now we are ready to transliterate
        String outputString = tr.transliterate(inputString, targetLanguage);

        TextView tv = ((View) view.getParent()).findViewById(R.id.TransliterateTabOutputTextView);
        tv.setText(outputString);
    }

    public void initialiseSpinner(View v) {
        Log.d(logTag, "Transliterate tab spinner initialising...");
        Spinner languageSelectionSpinner;
        if(v == null)
            languageSelectionSpinner = getActivity().findViewById(R.id.LanguageSelectionSpinner);
        else
            languageSelectionSpinner = v.findViewById(R.id.LanguageSelectionSpinner);

        LabelledArrayAdapter<String> adapter = new LabelledArrayAdapter<>(getContext(),
                R.layout.spinner_item,
                // Transliterator class' LangDataReader should be initialised now,
                // no need to check what languages are available now; just fetch them
                R.id.spinnerItemTV, tr.getLangDataReader().getTransLangs(),
                R.id.spinnerLabelTV, getString(R.string.transliterate_tab_trans_hint));
        adapter.setDropDownViewResource(R.layout.spinner_drop_down);
        languageSelectionSpinner.setAdapter(adapter);

        languageSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                targetLanguage = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
}