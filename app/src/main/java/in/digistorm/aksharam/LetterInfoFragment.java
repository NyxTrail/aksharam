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
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class LetterInfoFragment extends Fragment {
    private final String logTag = "LetterInfoFragment";

    private String currentLetter = "";
    private LettersTabFragment lettersTabFragment;

    public LetterInfoFragment(LettersTabFragment ltf) {
        super();
        lettersTabFragment = ltf;
    }

    public static LetterInfoFragment newInstance(String letter, LettersTabFragment ltf) {
        LetterInfoFragment letterInfoFragment = new LetterInfoFragment(ltf);

        Bundle args = new Bundle();
        args.putString("letter", letter);
        letterInfoFragment.setArguments(args);

        return letterInfoFragment;
    }

    // Set up the LetterInfo dialog
    @SuppressLint("SetTextI18n")
    private void setUp(View v, LayoutInflater inflater) {
        if(getArguments() != null)
            currentLetter = getArguments().getString("letter");
        else {
            Log.d(logTag, "Null arguments in Setup(View, LayoutInflater)");
            return ;
        }
        Log.d(logTag, "Showing info dialog for: " + currentLetter);
        Transliterator tr = lettersTabFragment.getTransliterator();
        ((TextView) v.findViewById(R.id.letterInfoHeadingTV))
                .setText(currentLetter);
        ((TextView) v.findViewById(R.id.letterInfoTransliteratedHeadingTV))
                .setText(tr.transliterate(currentLetter,
                        lettersTabFragment.getLettersTabFragmentTargetLanguage()));

        JSONObject letterExamples = Transliterator.getLangDataReader().getLetterExamples(currentLetter);

        // We pack the examples into the WordAndMeaning Layout in letter_info.xml layout file
        LinearLayout letterInfoWordAndMeaningLL =
                 v.findViewById(R.id.letterInfoWordAndMeaningLL);
        try {
            // If there are no examples, hide this section
            if (letterExamples == null || letterExamples.toString().equals("")) {
                v.findViewById(R.id.letterInfoWordsTV).setVisibility(View.GONE);
                v.findViewById(R.id.letterInfoMeaningTV).setVisibility(View.GONE);
            } else {
                for (Iterator<String> it = letterExamples.keys(); it.hasNext(); ) {
                    String word = it.next();
                    // Don't attach to root. If attached, we wouldn't be able to find the TextView
                    // ID's below
                    View wordsAndMeaningView = inflater.inflate(R.layout.word_and_meaning,
                            letterInfoWordAndMeaningLL, false);

                    int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 4,
                            getResources().getDisplayMetrics());
                    wordsAndMeaningView.setPadding(px, px, px, px);
                    Log.d(logTag, ((TextView) wordsAndMeaningView.findViewById(
                            R.id.wordAndMeaningWordTV)).getText().toString());
                    String meaning = letterExamples.getJSONObject(word)
                            .getString(Transliterator.getLangDataReader().getTargetLangCode(
                                    lettersTabFragment.getLettersTabFragmentTargetLanguage()));

                    ((TextView) wordsAndMeaningView.findViewById(R.id.wordAndMeaningWordTV))
                            .setText(word);
                    ((TextView) wordsAndMeaningView.findViewById(R.id.wordAndMeaningMeaningTV))
                            .setText(meaning);
                    ((TextView) wordsAndMeaningView
                            .findViewById(R.id.wordAndMeaningTransliterationTV))
                            .setText(lettersTabFragment.getTransliterator().transliterate
                                    (word, lettersTabFragment
                                            .getLettersTabFragmentTargetLanguage()));
                    letterInfoWordAndMeaningLL.addView(wordsAndMeaningView);
                }
            }

            // Check if extra info exists for this letter
            String letterInfo = Transliterator.getLangDataReader().getLetterInfo(currentLetter);
            TextView letterInfoInfoTV = v.findViewById(R.id.letterInfoInfoTV);
            if(letterInfo == null || letterInfo.isEmpty()) {
                Log.d(logTag, "No additional info for " + currentLetter
                        + " was found. Hiding UI element.");
                letterInfoInfoTV.setVisibility(View.GONE);
            }
            else {
                letterInfoInfoTV.setText(Html.fromHtml(letterInfo));
            }

            // For consonants and ligatures, show examples of how they can combine with
            // vowel diacritics.
            String category = Transliterator.getLangDataReader().getCategory(currentLetter);
            boolean showDiacriticExamples = true;
            if(category != null && !Transliterator.getLangDataReader().isExcludeCombiExamples(currentLetter)
                    && (category.equalsIgnoreCase("consonants")
                        || category.equalsIgnoreCase("ligatures"))) {
                displaySignConsonantCombinations(v, category);
                if(category.equalsIgnoreCase("consonants"))
                    displayLigatures(v);
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
                    + lettersTabFragment.getLettersTabFragmentTargetLanguage());
            je.printStackTrace();
        }
    }

    // Lets try to combine current letter with all letters
    public void displayLigatures(View v) {
        /* ligatureAfterHintTV, ligaturesGLAfter, linearLayoutAfter, etc are all for
         * ligatures formed when currentLetter appears *after* the virama.
         * ligatureBeforeHintTV, ligaturesGLBefore, linearLayoutBefore, etc are all for
         * ligatures formed when currentLetter appears *before* the virama.
         * TODO: some way to reduce code duplication?
         */
        TextView ligatureAfterHintTV = v.findViewById(R.id.letterInfoLigaturesAfterTV);
        TextView ligatureBeforeHintTV = v.findViewById(R.id.letterInfoLigaturesBeforeTV);

        ArrayList<String> items = null;
        items = Transliterator.getLangDataReader().getConsonants();
        String virama = Transliterator.getLangDataReader().getVirama();

        v.findViewById(R.id.letterInfoLigaturesCL).setVisibility(View.VISIBLE);
        GridLayout ligaturesGLBefore = v.findViewById(R.id.letterInfoLigaturesBeforeGL);
        ligaturesGLBefore.removeAllViews();
        GridLayout ligaturesGLAfter = v.findViewById(R.id.letterInfoLigaturesAfterGL);
        ligaturesGLAfter.removeAllViews();

        ligatureBeforeHintTV.setText(getString(R.string.letter_info_ligature_consonant_before,
                currentLetter, currentLetter, virama));
        ligatureAfterHintTV.setText(getString(R.string.letter_info_ligature_consonant_after,
                currentLetter, virama, currentLetter));

        for(String item: items) {
            String ligatureAfter = item + virama + currentLetter;
            String ligatureBefore = currentLetter + virama + item;

            // UI elements for ligatureBefore
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(20, 20, 20, 20);
            linearLayout.setLayoutParams(layoutParams);

            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            ViewGroup.LayoutParams tvLayoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            textView.setPadding(4, 4, 4,4);
            textView.setLayoutParams(tvLayoutParams);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            ViewGroup.LayoutParams tvParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            textView.setLayoutParams(tvParams);

            textView.setText(ligatureBefore);

            linearLayout.addView(textView);
            ligaturesGLBefore.addView(linearLayout);


            // UI elements for ligatureAfter
            linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setGravity(Gravity.CENTER);
            layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(20, 20, 20, 20);
            linearLayout.setLayoutParams(layoutParams);

            textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            tvLayoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            textView.setPadding(4, 4, 4,4);
            textView.setLayoutParams(tvLayoutParams);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            tvParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            textView.setLayoutParams(tvParams);

            textView.setText(ligatureAfter);

            linearLayout.addView(textView);
            ligaturesGLAfter.addView(linearLayout);
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
            items = Transliterator.getLangDataReader().getConsonants();
            items.addAll(Transliterator.getLangDataReader().getLigatures());
        }
        // we need to display examples for a consonant/ligature
        else if(type.equalsIgnoreCase("consonants")
                || type.equalsIgnoreCase("ligatures")) {
            diacriticSelectorHintTV.setText(getString(R.string.diacritics_with_consonant,
                    currentLetter));
            items = Transliterator.getLangDataReader().getDiacritics();
        }

        if(items == null)
            return;

        Log.d(logTag, "Items obtained: " + items);

        GridLayout diacriticExamplesGridLayout = v.findViewById(R.id.diacriticExamplesGL);
        diacriticExamplesGridLayout.removeAllViews();

        for(String item: items) {
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 10, 10, 10);
            linearLayout.setLayoutParams(layoutParams);

            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            ViewGroup.LayoutParams tvLayoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            textView.setPadding(4, 4, 4,4);
            textView.setLayoutParams(tvLayoutParams);
            if(type.equalsIgnoreCase("signs")
                    && !Transliterator.getLangDataReader().isExcludeCombiExamples(item)) {
                    textView.setText(item + currentLetter);
            }
            else if((type.equalsIgnoreCase("consonants")
                    || type.equalsIgnoreCase("ligatures"))
                    && !Transliterator.getLangDataReader().isExcludeCombiExamples(item))
                textView.setText(currentLetter + item);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            ViewGroup.LayoutParams tvParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            textView.setLayoutParams(tvParams);

            // Add the textView and its parent linear layout only if the textview has some content
            if(textView.getText() != null && !textView.getText().equals("")) {
                linearLayout.addView(textView);
                diacriticExamplesGridLayout.addView(linearLayout);
            }
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