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

package in.digistorm.aksharam;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LangDataReader {

    public static JSONObject read(String file, Context context) {
        JSONObject langdata = null;

        try {
            InputStream is = context.getAssets().open("languages/kannada.json");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            StringBuffer sb = new StringBuffer();

            String s;
            while ((s = br.readLine()) != null) {
                Log.d("TransliterateButton", s);
                sb.append(s);
            }

            langdata = (JSONObject) new JSONTokener(sb.toString()).nextValue();
        } catch (JSONException je) {
            Log.d ("TransliterateButton", "Error reading JSON from lang data");
        } catch (IOException e) {
            Log.d("TransliterateButton", "Exception caught while trying to read lang file");
            e.printStackTrace();
        }

        return langdata;
    }
}
