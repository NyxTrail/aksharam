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

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class LetterInfoFragment extends Fragment {
    private String logTag = getClass().getName();
    public static final String TAG = "LetterInfoFragment";

    private String currentLetter = "";

    public static LetterInfoFragment newInstance(String letter) {
        LetterInfoFragment letterInfoFragment = new LetterInfoFragment();

        Bundle args = new Bundle();
        args.putString("letter", letter);
        letterInfoFragment.setArguments(args);

        return letterInfoFragment;
    }

    // Set up the LetterInfo dialog
    @SuppressLint("SetTextI18n")
    private void setUp(View v, LayoutInflater inflater) {
        Log.d(logTag, "Showing info dialog for: " + getArguments().getString("letter"));
        currentLetter = getArguments().getString("letter");

        Transliterator tr = LettersTabFragment.getTransliterator();
        ((TextView) v.findViewById(R.id.letterInfoHeadingTV))
                .setText(currentLetter);
        ((TextView) v.findViewById(R.id.letterInfoTransliteratedHeadingTV))
                .setText(tr.transliterate(currentLetter,
                        LettersTabFragment.getLettersTabFragmentTargetLanguage()));

        // Get the examples for this letter.
        JSONObject letterExamples = LangDataReader.getLetterExamples(currentLetter);

        // We pack the examples into the Word and Meaning ConstraintLayout in
        // letter_info.xml layout file
        LinearLayout letterInfoWordAndMeaningLL =
                 v.findViewById(R.id.letterInfoWordAndMeaningLL);
        try {
            // If there are no examples, hide this section
            if (letterExamples == null || letterExamples.equals("")) {
                v.findViewById(R.id.letterInfoWordsTV).setVisibility(View.INVISIBLE);
                v.findViewById(R.id.letterInfoMeaningTV).setVisibility(View.INVISIBLE);
            } else {
                for (Iterator<String> it = letterExamples.keys(); it.hasNext(); ) {
                    String word = it.next();
                    // Don't attach to root. If attached, we wouldn't be able to find the TextView
                    // ID's below
                    View wordsAndMeaningView = inflater.inflate(R.layout.word_and_meaning,
                            letterInfoWordAndMeaningLL, false);

                    Log.d(logTag, ((TextView) wordsAndMeaningView.findViewById(
                            R.id.wordAndMeaningWordTV)).getText().toString());
                    String meaning = letterExamples.getJSONObject(word)
                            .getString(LettersTabFragment.getLettersTabFragmentTargetLanguage());

                    ((TextView) wordsAndMeaningView.findViewById(R.id.wordAndMeaningWordTV))
                            .setText(word);
                    ((TextView) wordsAndMeaningView.findViewById(R.id.wordAndMeaningMeaningTV))
                            .setText(meaning);
                    ((TextView) wordsAndMeaningView
                            .findViewById(R.id.wordAndMeaningTransliterationTV))
                            .setText(LettersTabFragment.getTransliterator().transliterate
                                    (word, LettersTabFragment
                                            .getLettersTabFragmentTargetLanguage()));
                    letterInfoWordAndMeaningLL.addView(wordsAndMeaningView);

                }
            }

            // Check if extra info exists for this letter
            String letterInfo = LangDataReader.getLetterInfo(currentLetter);
            TextView letterInfoInfoTV = v.findViewById(R.id.letterInfoInfoTV);
            if(letterInfo == null || letterInfo.isEmpty()) {
                Log.d(logTag, "No additional info for " + currentLetter
                        + " was found. Hiding UI element.");
                letterInfoInfoTV.setVisibility(View.INVISIBLE);
            }
            else {
                letterInfoInfoTV.setText(Html.fromHtml(letterInfo));
            }

            // For consonants and ligatures, show examples of how they can combine with
            // vowel diacritics.
            String category = LangDataReader.getCategory(currentLetter);
            boolean showDiacriticExamples = true;
            if(category != null && !LangDataReader.isExcludeCombiExamples(currentLetter)
                    && (category.equalsIgnoreCase("consonants")
                        || category.equalsIgnoreCase("ligatures"))) {
                displaySignConsonantCombinations(v, category);
            }
            else
                showDiacriticExamples = false;

            // For a sign, display how it combines with each consonant
            if(category != null && (category.equalsIgnoreCase("signs")))
                displaySignConsonantCombinations(v, category);
            else if(!showDiacriticExamples)
                v.findViewById(R.id.letterInfoDiacriticExamplesCL).setVisibility(View.GONE);
        }
        catch (JSONException je) {
            Log.d(logTag, "Exception caught while reading fetching info for "
                    + currentLetter + ", language: "
                    + LettersTabFragment.getLettersTabFragmentTargetLanguage());
            je.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    public void displaySignConsonantCombinations(View v, String type) {
        TextView diacriticSelectorHintTV = v.findViewById(R.id.diacriticSelectorHintTV);
        ArrayList<String> items = null;
        // We need to display examples for a sign
        if(type.equalsIgnoreCase("signs")) {
            diacriticSelectorHintTV.setText(getString(R.string.consonants_with_diacritic,
                    currentLetter));
            items = LangDataReader.getConsonants();
            items.addAll(LangDataReader.getLigatures());
        }
        // we need to display examples for a consonant/ligature
        else if(type.equalsIgnoreCase("consonants")
                || type.equalsIgnoreCase("ligatures")) {
            diacriticSelectorHintTV.setText(getString(R.string.diacritics_with_consonant));
            items = LangDataReader.getDiacritics();
        }

        if(items == null)
            return;

        Log.d(logTag, "Items obtained: " + items.toString());

        GridLayout diacriticExamplesGridLayout = v.findViewById(R.id.diacriticExamplesGL);
        diacriticExamplesGridLayout.removeAllViews();

        for(String item: items) {
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            try {
                display.getRealSize(size);
            } catch (NoSuchMethodError err) {
                display.getSize(size);
            }
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    size.x / 6,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(layoutParams);

            TextView textView = new TextView(getContext());
            if(type.equalsIgnoreCase("signs")
                    && !LangDataReader.isExcludeCombiExamples(item)) {
                    textView.setText(item + currentLetter);
            }
            else if((type.equalsIgnoreCase("consonants")
                    || type.equalsIgnoreCase("ligatures"))
                    && !LangDataReader.isExcludeCombiExamples(item))
                textView.setText(currentLetter + item);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

            ViewGroup.LayoutParams tvParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            textView.setLayoutParams(tvParams);

            linearLayout.addView(textView);
            diacriticExamplesGridLayout.addView(linearLayout);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.letter_info, container, false);

        setUp(v, inflater);

        return v;
    }
}