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

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import in.digistorm.aksharam.R;
import in.digistorm.aksharam.util.LanguageDetector;
import in.digistorm.aksharam.util.Transliterator;
import in.digistorm.aksharam.util.GlobalSettings;
import in.digistorm.aksharam.util.LabelledArrayAdapter;
import in.digistorm.aksharam.util.Log;

public class TransliterateTabFragment extends Fragment {
    private final String logTag = getClass().getSimpleName();

    private String targetLanguage;
    private Transliterator tr;

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
            }

            @Override
            public void afterTextChanged(Editable s) {
                transliterate(s.toString(), view);
            }
        });

        initialiseSpinner(view);

        GlobalSettings.getInstance().addDataFileListChangedListener("TransliterateTabFragmentListener", () -> {
            Log.d(logTag, "Re-initialising spinners");
            initialiseSpinner(view);
        });
    }

    private void transliterate(String s, View view) {
        Log.d(logTag, "Transliterating " + s + " to " + targetLanguage);
        if(s.length() == 0 || view == null)
            return;
        String lang = detectLanguage(s);
        if(lang != null && !lang.equalsIgnoreCase(tr.getCurrentLang())) {
            tr = new Transliterator(lang, getContext());
            initialiseSpinner(null);
        }
        Log.d(logTag, lang + "; " + tr.getCurrentLang());

        // Now we are ready to transliterate
        String outputString = tr.transliterate(s, targetLanguage);
        setText(view, R.id.TransliterateTabOutputTextView, outputString);
    }

    // v: view to run findViewById on
    // id: id of TextView to find
    // text: text to set
    private void setText(View v, int id, CharSequence text) {
        if(v == null)
            return;
        TextView tv = v.findViewById(id);
        if(tv == null)
            return;
        tv.setText(text);
    }

    // v: view to run findViewById on
    // id: id of TextView to find
    // text: text to set
    private void setText(Activity activity, int id, CharSequence text) {
        if(activity == null)
            return;
        TextView tv = activity.findViewById(id);
        if(tv == null)
            return;
        tv.setText(text);
    }

    private String detectLanguage(String inputString) {
        String lang = new LanguageDetector(getContext()).detectLanguage(inputString, getContext());
        Log.d(logTag, "Detected language: " + lang);
        if(lang == null) {
            Log.d(logTag, getString(R.string.lang_could_not_detect));
            if(getActivity() == null)
                return null;
            setText(getActivity(), R.id.transliterateTabInfoTV,
                    Html.fromHtml(getText(R.string.lang_could_not_detect).toString()));
            return null;
        }
        setText(getActivity(), R.id.transliterateTabInfoTV, getText(R.string.transliterate_tab_info_default));
        return lang;
    }

    public void initialiseSpinner(View view) {
        Log.d(logTag, "Transliterate tab spinner initialising...");
        Spinner languageSelectionSpinner = null;
        if(view != null)
            languageSelectionSpinner = view.findViewById(R.id.LanguageSelectionSpinner);
        else if(getActivity() != null) {
            languageSelectionSpinner = getActivity().findViewById(R.id.LanguageSelectionSpinner);
        }

        if(languageSelectionSpinner == null) {
            Log.d(logTag, "Language selection spinner could not be found. Unable to initialise.");
            return ;
        }

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
                EditText editText = getActivity().findViewById(R.id.TransliterateTabInputTextField);
                if(editText == null)
                    return ;
                transliterate(editText.getText().toString(), getActivity().findViewById(R.id.TransliterateTabOutputTextView));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
}