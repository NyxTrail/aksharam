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
import android.service.controls.Control;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

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
        // If there are no examples, hide this section
        if(letterExamples == null || letterExamples.equals(""))
            v.findViewById(R.id.letterInfoWordAndMeaningCL).setVisibility(View.GONE);
        else {
            for(Iterator<String> it = letterExamples.keys(); it.hasNext();) {
                String word = it.next();
                // Don't attach to root. If attached, we wouldn't be able to find the TextView ID's
                // below
                View wordsAndMeaningView = inflater.inflate(R.layout.word_and_meaning,
                        letterInfoWordAndMeaningLL, false);

                try {
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

                    // Check if extra info exists for this letter
                    String letterInfo = LangDataReader.getLetterInfo(currentLetter);
                    TextView letterInfoInfoTV = v.findViewById(R.id.letterInfoInfoTV);
                    if(letterInfo == null || letterInfo.isEmpty()) {
                        letterInfoInfoTV.setVisibility(View.GONE);
                    }
                    else {
                        letterInfoInfoTV.setText(Html.fromHtml(letterInfo));
                    }
                }
                catch (JSONException je) {
                    Log.d(logTag, "Exception caught while reading meaning for " + word +
                            ", language: "
                            + LettersTabFragment.getLettersTabFragmentTargetLanguage());
                    je.printStackTrace();
                }
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