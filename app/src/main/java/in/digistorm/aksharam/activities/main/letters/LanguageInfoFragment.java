package in.digistorm.aksharam.activities.main.letters;

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
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import in.digistorm.aksharam.R;
import in.digistorm.aksharam.util.Log;

public class LanguageInfoFragment extends Fragment {
    private final String logTag = getClass().getSimpleName();

    public static LanguageInfoFragment newInstance(String info) {
        LanguageInfoFragment lif = new LanguageInfoFragment();

        Bundle args = new Bundle();
        args.putString("info", info);
        lif.setArguments(args);

        return lif;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        return inflater.inflate(R.layout.language_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String info = null;
        if(getArguments() != null)
            info = getArguments().getString("info");

        if(info == null)
            Log.d(logTag, "Info is null");

        ((TextView) view.findViewById(R.id.languageInfoTV)).setText(Html.fromHtml(info));
    }
}
