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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class LangDataReader {
    private static final String logTag = "LangDataReader";
    // Data object from the langdata file
    private static JSONObject langData;
    // {"vowels": ["a", "e", "i"...], "consonants": ["b", "c", "d"...]...}
    private static LinkedHashMap<String, ArrayList<String>> categories;

    public static void initialise(String file, Context context) {
        Log.d(logTag, "initialising lang data file: " + file);
        langData = read(file, context);
        categories = new LinkedHashMap<>();

        findCategories();
    }

    private static void findCategories() {
        if (langData != null) {
            Iterator<String> keys = langData.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                String type = langData.optJSONObject(key)
                        .optString("type");
                if(!categories.containsKey(type))
                    categories.put(type, new ArrayList<>());
                categories.get(type).add(key);
            }
        }
    }

    private static JSONObject read(String file, Context context) {
        JSONObject langData = null;

        try {
            Log.d(logTag, "Reading language file " + "languages/" + file);
            InputStream is = context.getAssets().open("languages/" + file);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();

            String s;
            while ((s = br.readLine()) != null) {
                sb.append(s);
            }
            langData = (JSONObject) new JSONTokener(sb.toString()).nextValue();
        } catch (JSONException je) {
            Log.d (logTag, "Error reading JSON from lang data");
        } catch (IOException e) {
            Log.d(logTag, "Exception caught while trying to read lang file");
            e.printStackTrace();
        }

        return langData.optJSONObject("data");
    }

    public static JSONObject getLangData() {
        return langData;
    }

    // Read langdata from a file and return immediately
    public static JSONObject getLangData(String file, Context context) {
        return read(file, context);
    }

    public static HashMap<String, ArrayList<String>> getCategories() {
        return categories;
    }
}