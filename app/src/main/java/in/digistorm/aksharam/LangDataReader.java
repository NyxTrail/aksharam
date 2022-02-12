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

import org.json.JSONArray;
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
import java.util.Locale;
import java.util.Map;

public class LangDataReader {
    private final String logTag = "LangDataReader";
    // Data object from the lang data file
    private JSONObject langData;

    // metadata about current file
    private String currentLang;
    private String currentFile;
    // code for the current lang data file
    private String langCode;
    private boolean randomiseLigatures;
    private final ArrayList<String> sourceLangs = new ArrayList<>();
    // transliteration languages suppported by the current data file
    private final ArrayList<String> transLangs = new ArrayList<>();
    // codes for the languages above
    private final Map<String, String> transLangCodes = new HashMap<>();

    // {"vowels": ["a", "e", "i"...], "consonants": ["b", "c", "d"...]...}
    private LinkedHashMap<String, ArrayList<String>> categories;

    public String getCurrentFile() {
        return currentFile;
    }

    public LangDataReader(Context context) {
        this("kannada.json", context);
    }

    public LangDataReader(String file, Context context) {
        Log.d(logTag, "Initialising lang data file: " + file);
        if(file == null)
            return;
        langData = read(file, true, context);
        categories = new LinkedHashMap<>();

        getAvailableSourceLanguages(context);
        findCategories();
    }

    private void findCategories() {
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

    // reset: reset class variables
    private JSONObject read(String file, boolean reset, Context context) {
        JSONObject langData = null;

        try {
            if (reset)
                currentFile = file;
            Log.d(logTag, "Reading language file " + "languages/" + file);
            InputStream is = context.getAssets().open("languages/" + file);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();

            String s;
            while ((s = br.readLine()) != null) {
                sb.append(s);
            }
            br.close();
            is.close();
            langData = (JSONObject) new JSONTokener(sb.toString()).nextValue();
            Log.d(logTag, "Lang data file " + file + " read: " + langData.toString());

            // populate metadata about current file
            if (reset) {
                currentLang = currentFile.replace(".json", "")
                        .toLowerCase(Locale.ROOT);
                transLangs.clear();
                transLangCodes.clear();
                if(langData.has("randomise_ligatures"))
                    randomiseLigatures = langData.getBoolean("randomise_ligatures");
                else
                    randomiseLigatures = false;
                langCode = langData.getString("code");
                JSONArray transLangJSONArray = langData.getJSONArray("trans_langs");
                for (int i = 0; i < transLangJSONArray.length(); i++) {
                    String lang = ((JSONObject) transLangJSONArray.get(i)).keys().next();
                    transLangs.add(lang.substring(0, 1).toUpperCase(Locale.ROOT) + lang.substring(1));
                    transLangCodes.put(lang, ((JSONObject) transLangJSONArray.get(i)).getString(lang));
                }
            }
            return langData.optJSONObject("data");
        } catch (JSONException je) {
            Log.d (logTag, "Error reading JSON from lang data");
            je.printStackTrace();
        } catch (IOException e) {
            Log.d(logTag, "Exception caught while trying to read lang file");
            e.printStackTrace();
        }

        // This should not happen
        Log.d(logTag, "Something bad happened");
        return null;
    }

    public JSONObject getLetterExamples(String letter) {
        try {
            Log.d(logTag, "Getting examples for " + letter);
            return langData.getJSONObject(letter).getJSONObject("examples");
        } catch (JSONException je) {
            Log.d(logTag, "JSON error reading examples from lang data for: " + letter);
            return null;
        }
    }

    public String getCategory(String letter) {
        try {
            return langData.getJSONObject(letter).getString("type");
        } catch (JSONException je) {
            Log.d(logTag, "JSON error when finding category for: " + letter);
            return null;
        }
    }

    private ArrayList<String> getAllOfType(String type) {
        ArrayList<String> allOfType = new ArrayList<>();

        for(Iterator<String> letters = langData.keys(); letters.hasNext();) {
            String letter = letters.next();
            try {
                String langData_type = langData.getJSONObject(letter).getString("type");
                if(langData_type.equalsIgnoreCase(type)) {
                    allOfType.add(letter);
                }
            } catch (JSONException je) {
                Log.d(logTag, "Error when getting " + type + " for " + currentLang);
                return null;
            }
        }
        return allOfType;
    }

    // JSONException can occur frequently here because this option is mentioned
    // only for letters or letter combinations that cannot form any meaningful combinations
    public boolean isExcludeCombiExamples(String letter) {
        try {
            return langData.getJSONObject(letter).getBoolean("excludeCombiExamples");
        } catch(JSONException je) {
            return false;
        }
    }

    public String getCurrentLang() {
        return currentLang;
    }

    public ArrayList<String> getAvailableSourceLanguages(Context context) {
        Log.d(logTag, "finding all available lang data files");
        try {
            String[] files = context.getAssets().list("languages/");
            sourceLangs.clear();
            for(String file: files) {
                Log.d(logTag, "found file " + file);
                file = file.replace(".json", "");
                sourceLangs.add(file.substring(0,1).toUpperCase(Locale.ROOT) + file.substring(1));
            }
            Log.d(logTag, "source languages found: " + sourceLangs);
            return sourceLangs;
        } catch (IOException e) {
            Log.d(logTag, "Exception while iterating through lang data files:");
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<String> getAvailableSourceLanguages() {
        return sourceLangs;
    }

    public ArrayList<String> getDiacritics() {
        return getAllOfType("signs");
    }

    public ArrayList<String> getConsonants() {
        return getAllOfType("consonants");
    }

    public ArrayList<String> getLigatures() {
        return getAllOfType("ligatures");
    }

    public String getLetterInfo(String letter) {
        try {
            Log.d(logTag, "Getting info text for " + letter);
            return langData.getJSONObject(letter).getJSONObject("info").getString("en");
        } catch(JSONException je) {
            Log.d(logTag, "JSON error reading info from lang data for: " + letter);
            return null;
        }
    }

    public boolean isRandomiseLigatures() {
        return randomiseLigatures;
    }

    public JSONObject getLangData() {
        return langData;
    }

    public ArrayList<String> getTransLangs() { return transLangs; }


    public String getLangCode() {
        return langCode;
    }

    public String getTargetLangCode(String name) {
        Log.d(logTag, "Getting code for " + name);
        Log.d(logTag, "lang codes: " + transLangCodes);
        if(name == null)
            return null;
        if(name.endsWith(".json"))
            name = name.replace(".json", "");
        return transLangCodes.get(name.toLowerCase(Locale.ROOT));
    }

    // This needs to be changed when interface for data file addition is added
    public static String getLangFile(String langName) {
        if(langName == null)
            return null;
        return  langName.toLowerCase(Locale.ROOT) + ".json";
    }

    // get Virama for the current language
    // returns empty string on error
    public String getVirama() {
        if(categories.isEmpty())
            return "";
        // Virama is a "sign"
        ArrayList<String> signs = categories.get("signs");
        try {
            for (String sign : signs) {
                if (langData.getJSONObject(sign).has("isVirama"))
                    if (langData.getJSONObject(sign).getBoolean("isVirama"))
                        return sign;
            }
        } catch(JSONException je) {
            Log.d(logTag, "JSONException while trying to find the Virama");
            return "";
        }

        return "";
    }

    // Read langdata from a file and return immediately
    /*
    public JSONObject getLangData(String file, Context context) {
        return read(file, false, context);
    }
    */

    public HashMap<String, ArrayList<String>> getCategories() {
        return categories;
    }
}
