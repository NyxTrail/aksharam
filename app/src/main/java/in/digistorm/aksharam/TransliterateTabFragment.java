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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

public class TransliterateTabFragment extends Fragment {
    private String targetLanguage = "ml";
    private Transliterator tr;
    private boolean sourceChanged = true;

    private final String logTag = getClass().getName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.transliteration_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((EditText) view.findViewById(R.id.InputTextField)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(logTag, "Input string has changed.");
                sourceChanged = true;
            }
        });

        view.findViewById(R.id.TransliterateButton).setOnClickListener(
                this::transliterateButtonOnClick
        );
        initialiseSpinner(view);
    }

    public void transliterateButtonOnClick(View view) {
        String inputString = ((EditText) ((View)view.getParent()).findViewById(R.id.InputTextField))
                .getText().toString();
        Log.d(logTag, "Transliterating " + inputString);

        if(sourceChanged) {
            sourceChanged = false;
            LangDataReader.initialise(LangDataReader.getLangFile(
                    LangDataReader.detectLanguage(inputString, getContext())), getContext());
            tr = new Transliterator(LangDataReader.getLangData());
        }

        // Now we are ready to transliterate
        String outputString = tr.transliterate(inputString, targetLanguage);

        TextView tv = ((View) view.getParent()).findViewById(R.id.OutputTextView);
        tv.setText(outputString);
    }

    public void initialiseSpinner(View view) {
        Spinner languageSelectionSpinner = view.findViewById(R.id.LanguageSelectionSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.language_selection_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSelectionSpinner.setAdapter(adapter);

        languageSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                targetLanguage = LangDataReader.getLangCode(
                        parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}