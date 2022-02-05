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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class PracticeTabFragment extends Fragment {
    private String language;
    private Transliterator tranliterator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.practice_tab_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialisePracticeTabLangSpinner(view);
    }

    public void initialisePracticeTabLangSpinner(View v) {
        Spinner practiceTabLangSpinner = v.findViewById(R.id.PracticeTabLangSpinner);

        LabelledArrayAdapter<String> adapter = new LabelledArrayAdapter<>(getContext(),
                R.layout.spinner_item,
                R.id.spinnerItemTV,
                LangDataReader.getAvailableSourceLanguages(getContext()),
                R.id.spinnerLabelTV, getString(R.string.practice_tab_lang_hint));
        adapter.setDropDownViewResource(R.layout.spinner_drop_down);
        practiceTabLangSpinner.setAdapter(adapter);
        practiceTabLangSpinner.setSelection(0);

        practiceTabLangSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                language = parent.getItemAtPosition(position).toString();
                tranliterator = new Transliterator(language, getContext());

                // re-initialise the "practice in" spinner
                initialisePracticeTabPracticeInSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void initialisePracticeTabPracticeInSpinner() {
        Spinner practiceTabPracticeInSpinner = getActivity().findViewById(
                R.id.PracticeTabPracticeInSpinner);

        LabelledArrayAdapter<String> practiceInAdapter = new LabelledArrayAdapter<>(getContext(),
                R.layout.spinner_item,
                R.id.spinnerItemTV,
                LangDataReader.getTransLangs(),
                R.id.spinnerLabelTV, getString(R.string.practice_tab_practice_in_hint));
        practiceInAdapter.setDropDownViewResource(R.layout.spinner_drop_down);
        practiceTabPracticeInSpinner.setAdapter(practiceInAdapter);
        practiceTabPracticeInSpinner.setSelection(0);
    }
}
