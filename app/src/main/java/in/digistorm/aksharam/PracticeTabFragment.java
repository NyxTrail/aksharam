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
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class PracticeTabFragment extends Fragment {
    private final String logTag = "PracticeTabFragment";

    private Spinner practiceTabLangSpinner;

    private String language;
    private Transliterator transliterator;
    private String transLang;
    private String practiceType;
    private String practiceString;

    private class TextChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            TextView practiceTextTV = getActivity().findViewById(R.id.PracticeTabPracticeTextTV);

            boolean correctInProgress = true;
            StringBuilder markup = new StringBuilder();
            String transliteratedString = transliterator.transliterate(practiceString, transLang);
            Log.d(logTag, "Practice text " + practiceString + " was transliterated to "
                    + transliteratedString);
            Log.d(logTag, "Text entered was: " + s.toString());

            if(transliteratedString.equals(s.toString())) {
                clearInput();
                practiceTextTV.setText(Html.fromHtml("<font color=\"#7FFF00\">"
                        + practiceString + "</font>"));
                Toast.makeText(getContext(), R.string.practice_tab_correct_text_entered,
                        Toast.LENGTH_SHORT).show();
                getActivity().findViewById(R.id.PracticeTabInputTIET).setEnabled(false);
                return;
            }

            int i;
            markup.append("<font color=\"#7FFF00\">");
            for(i = 0; i < transliteratedString.length() && i < s.length(); i++) {
                if(transliteratedString.charAt(i) == s.charAt(i)) {
                    if(!correctInProgress) {
                        markup.append("</font><font color=\"#7FFF00\">");
                        correctInProgress = true;
                    }
                    markup.append(practiceString.charAt(i));
                }
                else {
                    if(correctInProgress) {
                        correctInProgress = false;
                        markup.append("</font><font color=\"#DC143C\">");
                    }
                    markup.append(practiceString.charAt(i));
                }
            }
            markup.append("</font>");
            markup.append(practiceString.substring(i));
            Log.d(logTag, "Marked up string: " + markup);

            practiceTextTV.setText(Html.fromHtml(markup.toString()));
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private final TextChangedListener textChangedListener = new TextChangedListener();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.practice_tab_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.PracticeTabRefreshButton).setOnClickListener(v -> {
            clearInput();
            startPractice();
            view.findViewById(R.id.PracticeTabInputTIET).setEnabled(true);
        });

        initialisePracticeTabLangSpinner(view);
    }

    public void initialisePracticeTabLangSpinner(View v) {
        practiceTabLangSpinner = v.findViewById(R.id.PracticeTabLangSpinner);

        LabelledArrayAdapter<String> adapter = new LabelledArrayAdapter<>(getContext(),
                R.layout.spinner_item,
                R.id.spinnerItemTV,
                Transliterator.getLangDataReader().getAvailableSourceLanguages(getContext()),
                R.id.spinnerLabelTV, getString(R.string.practice_tab_lang_hint));
        adapter.setDropDownViewResource(R.layout.spinner_drop_down);
        adapter.setNotifyOnChange(true);
        practiceTabLangSpinner.setAdapter(adapter);
        practiceTabLangSpinner.setSelection(0);

        GlobalSettings.getInstance().addDataFileListChangedListener("PracticeTabFragmentListener", () -> {
            Log.d(logTag, "Change in data files detected. Updating adapter.");
            adapter.clear();
            // Invoke getAvailableSourceLanguages without Context object so that it does not
            // read the files again. The changed files have already been read into
            // LangDataReader when it was changed by the SettingsLanguageListAdapter
            adapter.addAll(Transliterator.getLangDataReader().getAvailableSourceLanguages());
            // While the spinner shows updated text, its (Spinner's) getSelectedView() was sometimes returning
            // a non-existant item (say, if the item is deleted). Resetting the adapter was the only way I could
            // think of to fix this
            practiceTabLangSpinner.setAdapter(adapter);
        });
        practiceTabLangSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clearInput();

                language = parent.getItemAtPosition(position).toString();
                transliterator = new Transliterator(language, getContext());

                // re-initialise the "practice in" spinner
                initialisePracticeTabPracticeInSpinner();
                initialisePracticeTabPracticeTypeSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void clearInput() {
        TextInputEditText textInputEditText = getActivity().findViewById(R.id.PracticeTabInputTIET);
        if(textInputEditText == null)
            return;

        Log.d(logTag, "Clearing input edit text");
        ((TextInputEditText) getActivity().findViewById(R.id.PracticeTabInputTIET))
                .removeTextChangedListener(textChangedListener);
        ((TextInputEditText) getActivity().findViewById(R.id.PracticeTabInputTIET)).setText("");
        ((TextInputEditText) getActivity().findViewById(R.id.PracticeTabInputTIET))
                .addTextChangedListener(textChangedListener);
    }

    public void initialisePracticeTabPracticeInSpinner() {
        clearInput();

        Spinner practiceTabPracticeInSpinner = getActivity().findViewById(
                R.id.PracticeTabPracticeInSpinner);

        LabelledArrayAdapter<String> practiceInAdapter = new LabelledArrayAdapter<>(getContext(),
                R.layout.spinner_item,
                R.id.spinnerItemTV,
                Transliterator.getLangDataReader().getTransLangs(),
                R.id.spinnerLabelTV, getString(R.string.practice_tab_practice_in_hint));
        practiceInAdapter.setDropDownViewResource(R.layout.spinner_drop_down);
        practiceTabPracticeInSpinner.setAdapter(practiceInAdapter);
        practiceTabPracticeInSpinner.setSelection(0);

        practiceTabPracticeInSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clearInput();

                transLang = parent.getItemAtPosition(position).toString();
                ((TextInputLayout) getActivity().findViewById(R.id.PracticeTabInputTIL))
                        .setHint(getString(R.string.practice_tab_practice_input_hint, transLang));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void initialisePracticeTabPracticeTypeSpinner() {
        Spinner practiceTabPracticeTypeSpinner = getActivity().findViewById(
                R.id.PracticeTabPracticeTypeSpinner);

        ArrayList<String> practiceTypes = new ArrayList<>();
        Set<String> categories = Transliterator.getLangDataReader().getCategories().keySet();
        for(String category: categories) {
            practiceTypes.add(category.substring(0, 1).toUpperCase(Locale.ROOT)
                    + category.substring(1));
        }

        // Additional practice types
        // Random ligatures work best in some languages like Kannada where each consonant can form
        // a distinct conjunct with another consonant. Other languages like Malayalam or Hindi
        // have a few ligatures, yet this is true only for commonly occurring consonant combinations
        // Most of the combinations in these languages do not result in a meaningful ligature and
        // are usually represented using a half-consonant (with a virama). So, we will add random
        // ligatures only if the language's data file says we should.
        if(Transliterator.getLangDataReader().isRandomiseLigatures())
            practiceTypes.add("Random Ligatures");
        practiceTypes.add("Random Words");

        LabelledArrayAdapter<String> practiceTypeAdapter = new LabelledArrayAdapter<>(getContext(),
                R.layout.spinner_item,
                R.id.spinnerItemTV,
                practiceTypes,
                R.id.spinnerLabelTV, getString(R.string.practice_tab_practice_type_hint));
        practiceTypeAdapter.setDropDownViewResource(R.layout.spinner_drop_down);
        practiceTabPracticeTypeSpinner.setAdapter(practiceTypeAdapter);
        practiceTabPracticeTypeSpinner.setSelection(0);

        practiceTabPracticeTypeSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clearInput();
                practiceType = parent.getItemAtPosition(position).toString();
                startPractice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void startPractice() {
        if(practiceType == null || practiceType.equals(""))
            return;

        // Handle complex practice types first

        // Then handle basic practice types (practice a plain letter category
        // get all letters of current language, category-wise
        Map<String, ArrayList<String>> letters = Transliterator.getLangDataReader().getCategories();
        // select a random letter from the category that matches lang
        Random random = new Random();
        StringBuilder practiceString = new StringBuilder();

        Log.d(logTag, "letters: " + letters.toString());
        Log.d(logTag, "letters[\"" + practiceType.toLowerCase(Locale.ROOT) + "\"]: "
                + letters.get(practiceType.toLowerCase(Locale.ROOT)));

        switch(practiceType.toLowerCase(Locale.ROOT)) {
            case "random words":
                // Let's construct a made-up word in current language
                // First letter can be a vowel or consonant (not a sign)
                // Second letter onwards can be a vowel sign or consonant (not a vowel)
                for(int i = 0; i < 5; i++) {
                    // What should be the length of a word?
                    int wordLength = random.nextInt(10) + 3;   // length is 3 to 10 + 3
                    Log.d(logTag, "Constructing word of length " + wordLength);

                    int numVowels = letters.get("vowels").size();
                    int numConsonants = letters.get("consonants").size();
                    int numSigns = letters.get("signs").size();
                    // Choose the first character
                    if(random.nextInt(2) == 0)
                        practiceString.append(letters.get("vowels").get(random.nextInt(numVowels)));
                    else {
                        practiceString.append(letters.get("consonants")
                                .get(random.nextInt(numConsonants)));
                    }

                    // Special variable to hold the Virama.
                    // Useful to detect chillu letters in Malayalam
                    String virama = Transliterator.getLangDataReader().getVirama();

                    for(int j = 1; j < wordLength; j++) {
                        ArrayList<String> categories = new ArrayList<>();
                        categories.add("vowels");
                        categories.add("consonants");
                        categories.add("signs");

                        String category;

                        String nextChar = "";

                        // if previous letter was a vowel or a sign...
                        String prevChar = practiceString.substring(practiceString.length() - 1,
                                practiceString.length());
                        if(letters.get("vowels").contains(prevChar) ||
                                letters.get("signs").contains(prevChar)) {
                            // ...next char must be a consonant
                            nextChar = letters.get("consonants").get(random.nextInt(numConsonants));
                        }
                        else {
                            // if previous character was a consonant, next character can be a
                            // consonant or a sign
                            category = categories.get(random.nextInt(2) + 1);
                            nextChar = letters.get(category).get(random.nextInt(letters
                                    .get(category).size()));
                        }

                        // Special handling for the chillu letters in Malayalam
                        if(language.equalsIgnoreCase("malayalam")) {
                            if(nextChar.equals(virama)) {
                                Log.d(logTag, "Virama " + virama + " detected in language: "
                                        + language);

                                practiceString = new StringBuilder(practiceString.substring(0,
                                        practiceString.length() - 1));
                                switch(prevChar) {
                                    case "ര":
                                        practiceString.append("ർ");
                                        continue;
                                    case "ല":
                                        practiceString.append("ൽ");
                                        continue;
                                    case "ള":
                                        practiceString.append("ൾ");
                                        continue;
                                    case "ണ":
                                        practiceString.append("ൺ");
                                        continue;
                                    case "ന":
                                        practiceString.append("ൻ");
                                        continue;
                                }
                            }
                            // since the previous way of getting Chillu is too rare,
                            // let's add a small chance of the current char being a chillu
                            if(random.nextInt(100) < 10) {
                                nextChar = letters.get("chillu")
                                        .get(random.nextInt(letters.get("chillu").size()));
                            }
                        }
                        practiceString.append(nextChar);
                    }
                    practiceString.append(" ");
                }
                break;
            case "random ligatures":
                int numConsonants = letters.get("consonants").size();
                // get the sign for the Virama
                for(int i = 0; i < 10; i++) {
                    // choose a random consonant
                    practiceString.append(letters.get("consonants")
                            .get(random.nextInt(numConsonants)));
                    practiceString.append(Transliterator.getLangDataReader().getVirama());
                    practiceString.append(letters.get("consonants")
                            .get(random.nextInt(numConsonants)));
                    practiceString.append(" ");
                }
                break;
            case "signs":
                int numSigns = letters.get("signs").size();
                String predecessor = "";
                for(int i = 0; i < 10; i++) {
                    // choose a consonant or a ligature to combine the sign with
                    switch (random.nextInt(2)) {
                        // choose a consonant
                        case 0:
                            do {
                                predecessor = letters.get("consonants").get(random.nextInt(
                                        letters.get("consonants").size()));
                            } while (Transliterator.getLangDataReader().isExcludeCombiExamples(predecessor));
                            break;
                        case 1:
                            do {
                                predecessor = letters.get("ligatures").get(random.nextInt(
                                        letters.get("ligatures").size()));
                            } while (Transliterator.getLangDataReader().isExcludeCombiExamples(predecessor));
                            break;
                    }
                    practiceString.append(predecessor + letters.get("signs")
                            .get(random.nextInt(numSigns))).append(" ");
                }
                break;
            default:
                int numLetters = letters.get(practiceType.toLowerCase(Locale.ROOT)).size();
                for(int i = 0; i < 10; i++)
                    practiceString.append(letters.get(practiceType.toLowerCase(Locale.ROOT))
                            .get(random.nextInt(numLetters))).append(" ");
        }

        // strip the last " " from practiceString
        practiceString = new StringBuilder(practiceString.substring(0,
                practiceString.length() - 1));

        this.practiceString = practiceString.toString();
        ((TextView) getActivity().findViewById(R.id.PracticeTabPracticeTextTV))
                .setText(practiceString.toString());
    }
}
