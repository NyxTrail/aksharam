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

import android.content.Context;

import androidx.lifecycle.ViewModel;

import java.util.Locale;

public class LettersTabViewModel extends ViewModel {
    private String targetLanguage;
    private String language;
    private Transliterator transliterator;
    private LabelledArrayAdapter<String> adapter;

    public Transliterator getTransliterator(Context context) {
        if(transliterator == null)
            transliterator = Transliterator.getDefaultTransliterator(context);

        return transliterator;
    }

    public Transliterator getTransliterator(String language, Context context) {
        if(transliterator != null) {
            if (transliterator.getCurrentLang().toLowerCase(Locale.ROOT)
                    .equals(language.toLowerCase(Locale.ROOT)))
                return transliterator;
        }
        transliterator = new Transliterator(language, context);
        return transliterator;
    }

    public Transliterator getTransliterator() {
        return transliterator;
    }

    public void setTargetLanguage(String lang) {
        targetLanguage = lang;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setLanguage(String lang) {
        language = lang;
    }

    public String getLanguage() {
        return language;
    }

    public LabelledArrayAdapter<String> getAdapter() {
        return adapter;
    }

    public void setAdapter(LabelledArrayAdapter<String> adapter) {
        this.adapter = adapter;
    }
}
