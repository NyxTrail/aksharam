package in.digistorm.aksharam.activities.main.practice;

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
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import in.digistorm.aksharam.R;
import in.digistorm.aksharam.activities.main.MainActivity;
import in.digistorm.aksharam.util.GlobalSettings;
import in.digistorm.aksharam.util.LabelledArrayAdapter;
import in.digistorm.aksharam.util.LangDataReader;
import in.digistorm.aksharam.util.Log;

public class PracticeTabFragment extends Fragment {
    private final String logTag = getClass().getSimpleName();

    private Spinner practiceTabLangSpinner;

    private String practiceString;

    private PracticeTabViewModel viewModel;

    private class TextChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            TextView practiceTextTV = requireActivity().findViewById(R.id.PracticeTabPracticeTextTV);

            boolean correctInProgress = true;
            StringBuilder markup = new StringBuilder();
            String transliteratedString = viewModel.getTransliterator().transliterate(practiceString, viewModel.getTransLang());
            Log.d(logTag, "Practice text " + practiceString + " was transliterated to "
                    + transliteratedString);
            Log.d(logTag, "Text entered was: " + s.toString());

            if(transliteratedString.equals(s.toString())) {
                clearInput();
                practiceTextTV.setText(Html.fromHtml("<font color=\"#7FFF00\">"
                        + practiceString + "</font>"));
                Toast.makeText(getContext(), R.string.practice_tab_correct_text_entered,
                        Toast.LENGTH_SHORT).show();
                requireActivity().findViewById(R.id.PracticeTabInputTIET).setEnabled(false);
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

        Log.d(logTag, "onViewCreated");
        if(viewModel == null) {
            Log.d(logTag, "Creating View Model for PracticeTabFragment");
            viewModel = new ViewModelProvider(requireActivity()).get(PracticeTabViewModel.class);
        }
        // Initialise viewModel with a Transliterator
        viewModel.resetTransliterator(getContext());

        view.findViewById(R.id.PracticeTabRefreshButton).setOnClickListener(v -> {
            clearInput();
            startPractice();
            view.findViewById(R.id.PracticeTabInputTIET).setEnabled(true);
        });

        initialisePracticeTabLangSpinner(view);
    }

    public ArrayList<String> getDownloadedLanguages() {
        ArrayList<String> languages = LangDataReader.getDownloadedLanguages(requireContext());
        if(languages.size() == 0) {
            ((MainActivity) requireActivity()).startInitialisationAcitivity();
            return new ArrayList<>(); // Return an empty array list if we could not find any
                                      // downloaded files. Should not be a problem since we
                                      // are anyways exiting this activity.
        }
        return languages;
    }

    public void initialisePracticeTabLangSpinner(View v) {
        Log.d(logTag, "Initialising PracticeTabLangSpinner");
        practiceTabLangSpinner = v.findViewById(R.id.PracticeTabLangSpinner);


        LabelledArrayAdapter<String> adapter = new LabelledArrayAdapter<>(requireContext(),
                R.layout.spinner_item,
                R.id.spinnerItemTV,
                getDownloadedLanguages(),
                R.id.spinnerLabelTV, getString(R.string.practice_tab_lang_hint));
        adapter.setDropDownViewResource(R.layout.spinner_drop_down);
        adapter.setNotifyOnChange(true);
        practiceTabLangSpinner.setAdapter(adapter);
        practiceTabLangSpinner.setSelection(0);

        GlobalSettings.getInstance().addDataFileListChangedListener("PracticeTabFragmentListener", () -> {
            Log.d("PTFListener", "Change in data files detected. Updating adapter.");
            if(getContext() == null)
                return;

            viewModel.resetTransliterator(getContext());
            adapter.clear();
            adapter.addAll(getDownloadedLanguages());
            // While the spinner shows updated text, its (Spinner's) getSelectedView() was sometimes returning
            // a non-existant item (say, if the item is deleted). Resetting the adapter was the only way I could
            // think of to fix this
            Log.d("PTFListener", "Resetting spinner adapter");
            practiceTabLangSpinner.setAdapter(adapter);
        });

        practiceTabLangSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(logTag, "onItemSelected invoked by " + parent.toString());
                clearInput();

                String language = parent.getItemAtPosition(position).toString();
                viewModel.setTransliterator(language, getContext());

                // re-initialise the "practice in" spinner
                initialisePracticeTabPracticeInSpinner();
                initialisePracticeTabPracticeTypeSpinner();
                requireActivity().findViewById(R.id.PracticeTabInputTIET).setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void clearInput() {
        TextInputEditText textInputEditText = requireActivity().findViewById(R.id.PracticeTabInputTIET);
        if(textInputEditText == null)
            return;

        Log.d(logTag, "Clearing input edit text");
        ((TextInputEditText) requireActivity().findViewById(R.id.PracticeTabInputTIET))
                .removeTextChangedListener(textChangedListener);
        ((TextInputEditText) requireActivity().findViewById(R.id.PracticeTabInputTIET)).setText("");
        ((TextInputEditText) requireActivity().findViewById(R.id.PracticeTabInputTIET))
                .addTextChangedListener(textChangedListener);
    }

    public void initialisePracticeTabPracticeInSpinner() {
        clearInput();

        Spinner practiceTabPracticeInSpinner = requireActivity().findViewById(
                R.id.PracticeTabPracticeInSpinner);

        LabelledArrayAdapter<String> practiceInAdapter = new LabelledArrayAdapter<>(requireContext(),
                R.layout.spinner_item,
                R.id.spinnerItemTV,
                viewModel.getTransliterator().getLanguage().getSupportedLanguagesForTransliteration(),
                R.id.spinnerLabelTV, getString(R.string.practice_tab_practice_in_hint));
        practiceInAdapter.setDropDownViewResource(R.layout.spinner_drop_down);
        practiceTabPracticeInSpinner.setAdapter(practiceInAdapter);
        practiceTabPracticeInSpinner.setSelection(0);

        practiceTabPracticeInSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clearInput();

                viewModel.setTransLang(parent.getItemAtPosition(position).toString());
                ((TextInputLayout) requireActivity().findViewById(R.id.PracticeTabInputTIL))
                        .setHint(getString(R.string.practice_tab_practice_input_hint, viewModel.getTransLang()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void initialisePracticeTabPracticeTypeSpinner() {
        Spinner practiceTabPracticeTypeSpinner = requireActivity().findViewById(
                R.id.PracticeTabPracticeTypeSpinner);

        ArrayList<String> practiceTypes = new ArrayList<>();
        Set<String> categories = viewModel.getTransliterator().getLanguage().getLettersCategoryWise().keySet();
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
        if(viewModel.getTransliterator().getLanguage().areLigaturesAutoGeneratable())
            practiceTypes.add("Random Ligatures");
        practiceTypes.add("Random Words");

        LabelledArrayAdapter<String> practiceTypeAdapter = new LabelledArrayAdapter<>(requireContext(),
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
                viewModel.setPracticeType(parent.getItemAtPosition(position).toString());
                startPractice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void startPractice() {
        if(viewModel.getPracticeType() == null || viewModel.getPracticeType().equals(""))
            return;

        // get all letters of current language, category-wise
        ArrayList<String> vowels = viewModel.getTransliterator().getLanguage().getVowels();
        ArrayList<String> consonants = viewModel.getTransliterator().getLanguage().getConsonants();
        ArrayList<String> ligatures = viewModel.getTransliterator().getLanguage().getLigatures();
        ArrayList<String> signs = viewModel.getTransliterator().getLanguage().getDiacritics();
        ArrayList<String> chillu = viewModel.getTransliterator().getLanguage().getChillu();

        Random random = new Random();
        StringBuilder practiceString = new StringBuilder();

        // Special variable to hold the Virama.
        // Useful to detect chillu letters in Malayalam
        String virama = viewModel.getTransliterator().getLanguage().getVirama();

        switch(viewModel.getPracticeType().toLowerCase(Locale.ROOT)) {
            case "random words":
                // Let's construct a made-up word in current language
                // First letter can be a vowel or consonant (not a sign)
                // Second letter onwards can be a vowel sign or consonant (not a vowel)
                for(int i = 0; i < 5; i++) {
                    // What should be the length of a word?
                    int wordLength = random.nextInt(6) + 3;   // length is 3 to 6 + 3
                    Log.d(logTag, "Constructing word of length " + wordLength);

                    // Choose the first character. Vowel or consonant
                    if(random.nextBoolean())
                        practiceString.append(vowels.get(random.nextInt(vowels.size())));
                    else {
                        practiceString.append(consonants.get(random.nextInt(consonants.size())));
                    }

                    for(int j = 1; j < wordLength; j++) {
                        String nextChar;

                        String prevChar = practiceString.substring(practiceString.length() - 1,
                                practiceString.length());

                        // 20% chance that the next character is a joint letter
                        if(random.nextInt(100) < 21 && !prevChar.equals(virama)) {
                            // for malayalam, there is also a chance the next character is a chillu
                            if(viewModel.getLanguage().equalsIgnoreCase("malayalam")) {
                                if(random.nextInt(100) < 31)
                                    nextChar = chillu.get(random.nextInt(chillu.size()));
                                else
                                    // Since it's malayalam, we can just get one of the ligatures at random
                                    nextChar = ligatures.get(random.nextInt(ligatures.size()));
                            }
                            else
                                // construct ligature
                                nextChar = consonants.get(random.nextInt(consonants.size()))
                                        + virama
                                        + consonants.get(random.nextInt(consonants.size()));
                        }
                        // if previous letter was a vowel or a sign...
                        else if(vowels.contains(prevChar) || signs.contains(prevChar)) {
                            // ...next char must be a consonant
                            nextChar = consonants.get(random.nextInt(consonants.size()));
                        }
                        else {
                            // if previous character was a consonant, next character can be a
                            // consonant or a sign
                            ArrayList<String> randomChoice = random.nextBoolean() ? consonants : signs;
                            do {
                                nextChar = randomChoice.get(random.nextInt(randomChoice.size()));
                            } while (prevChar.equals(virama) && nextChar.equals(virama));
                        }

                        practiceString.append(nextChar);
                    }
                    practiceString.append(" ");
                }
                break;
            case "random ligatures":
                for(int i = 0; i < 10; i++) {
                    // choose a random consonant
                    practiceString.append(consonants.get(random.nextInt(consonants.size())))
                            .append(virama)
                            .append(consonants.get(random.nextInt(consonants.size())));
                    practiceString.append(" ");
                }
                break;
            case "signs":
                // predecessor is the consonant or ligature to combine the sign with
                String predecessor = "";
                for(int i = 0; i < 10; i++) {
                    // choose a consonant or a ligature to combine the sign with
                    switch (random.nextInt(2)) {
                        case 0:
                            predecessor = consonants.get(random.nextInt(consonants.size()));
                            break;

                        // choose a ligature
                        case 1:
                            String letter = ligatures.get(random.nextInt(ligatures.size()));
                            Log.d(logTag, "letter chosen: " + letter);

                            // Following for ligatures that have combining rules.
                            // Certain consonants (right now, only ರ್ in Kannada), form different types of
                            // ligatures before and after a consonant.
                            boolean isCombineAfter = viewModel.getTransliterator().getLanguage()
                                    .getLetterDefinition(letter).shouldCombineAfter();
                            boolean isCombineBefore = viewModel.getTransliterator().getLanguage()
                                    .getLetterDefinition(letter).shouldCombineBefore();

                            String base = viewModel.getTransliterator().getLanguage()
                                    .getLetterDefinition(letter).getBase();
                            // Find the base of this ligature, if any.
                            // "base" of a ligature is the actual consonant used for combining with
                            // the vowel sign
                            predecessor = base == null || base.isEmpty() ? letter : base;
                            Log.d(logTag, "base: " + predecessor);

                            if (isCombineAfter && isCombineBefore) {
                                // If letter can be combined before and after another letter,
                                // do one at random.
                                predecessor = random.nextBoolean() ?
                                        (consonants.get(random.nextInt(consonants.size())) +
                                                virama + predecessor) :
                                        (predecessor + virama + consonants.get(
                                                random.nextInt(consonants.size())));
                            } else if (isCombineAfter) {
                                predecessor = consonants.get(random.nextInt(consonants.size()))
                                        + virama + predecessor;
                            } else if (isCombineBefore) {
                                predecessor = predecessor + virama + consonants.get(random.nextInt(
                                        consonants.size()));
                            }
                            break;
                    }

                    String sign;
                    // what happens if sign selected is a virama?
                    sign = signs.get(random.nextInt(signs.size()));
                    practiceString.append(predecessor).append(sign).append(" ");
                }
                break;
            case "ligatures":
                for (int i = 0; i < 10; i++) {
                    String ligature = ligatures.get(random.nextInt(ligatures.size()));
                    Log.d(logTag, "Ligature obtained: " + ligature);
                    // nextChar is base char if a base exists in the data file.
                    // if there is no base in the data file, nextChar equals ligature (variable above)
                    String nextChar = viewModel.getTransliterator().getLanguage()
                            .getLetterDefinition(ligature).getBase();
                    nextChar = (nextChar == null) ? ligature : nextChar;

                    // get the rules for combining this letter if such rule exists
                    boolean isCombineAfter = viewModel.getTransliterator().getLanguage()
                            .getLetterDefinition(ligature).shouldCombineAfter();
                    boolean isCombineBefore = viewModel.getTransliterator().getLanguage()
                            .getLetterDefinition(ligature).shouldCombineBefore();

                    if (isCombineAfter && isCombineBefore) {
                        // randomly select either combineBefore or combineAfter
                        switch (random.nextInt(2)) {
                            //combineBefore
                            case 0:
                                practiceString.append(consonants.get(random.nextInt(consonants.size())))
                                        .append(virama).append(nextChar).append(" ");
                                break;
                            // combineAfter
                            case 1:
                                practiceString.append(nextChar).append(virama).append(
                                        consonants.get(random.nextInt(consonants.size()))).append(" ");
                                break;
                        }
                    } else if (isCombineAfter) {
                        practiceString.append(consonants.get(random.nextInt(consonants.size()))).append(virama)
                                .append(nextChar).append(" ");
                    } else if (isCombineBefore) {
                        practiceString.append(nextChar).append(virama).append(nextChar)
                                .append(consonants.get(random.nextInt(consonants.size())));
                    } else {
                        practiceString.append(nextChar).append(" ");
                    }
                }
                break;
            case "vowels":
                for(int i = 0; i < 10; i++)
                    practiceString.append(vowels.get(random.nextInt(vowels.size()))).append(" ");
                break;
            case "consonants":
                for(int i = 0; i < 10; i++)
                    practiceString.append(consonants.get(random.nextInt(consonants.size()))).append(" ");
                break;
            case "chillu":
                for(int i = 0; i < 10; i++)
                    practiceString.append(chillu.get(random.nextInt(chillu.size()))).append(" ");
                break;
        }

        // strip the last " " from practiceString
        practiceString = new StringBuilder(practiceString.substring(0,
                practiceString.length() - 1));

        this.practiceString = practiceString.toString();
        ((TextView) requireActivity().findViewById(R.id.PracticeTabPracticeTextTV))
                .setText(practiceString.toString());
    }
}