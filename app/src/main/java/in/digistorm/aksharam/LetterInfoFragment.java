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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

public class LetterInfoFragment extends DialogFragment {
    private String logTag = getClass().getName();
    public static final String TAG = "LetterInfoFragment";

    public static LetterInfoFragment newInstance(String letter) {
        LetterInfoFragment letterInfoFragment = new LetterInfoFragment();

        Bundle args = new Bundle();
        args.putString("letter", letter);
        letterInfoFragment.setArguments(args);

        return letterInfoFragment;
    }

    // Set up the LetterInfo dialog
    private void setUp(View v) {
        Log.d(logTag, "Showing info dialog for: " + getArguments().getString("letter"));
        ((TextView) v.findViewById(R.id.letterInfoHeadingTV))
                .setText(getArguments().getString("letter"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.letter_info, container, false);
        setUp(v);

        return v;
    }
}
