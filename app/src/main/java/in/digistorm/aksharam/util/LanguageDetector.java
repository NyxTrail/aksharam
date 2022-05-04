package in.digistorm.aksharam.util;

import android.content.Context;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LanguageDetector {
    private String logTag = "LanguageDetector";
    private LangDataReader langDataReader;

    public LanguageDetector(Context context) {
        String file = LangDataReader.areDataFilesAvailable(context);
        if(file == null) {
            Log.d(logTag, "Could not find any data files.");
            return;
        }
        langDataReader = new LangDataReader(LangDataReader.areDataFilesAvailable(context), context);
    }

    public String detectLanguage(String input, Context context) {
        Log.d(logTag, "Detecting language for " + input);

        // create a hashmap to store the characters for all known languages
        HashMap<String, ArrayList<String>> languages = new HashMap<>();
        // add characters of currently loaded language into the list
        ArrayList<String> characters = new ArrayList<>();
        for (Iterator<String> it = langDataReader.getLangData().keys(); it.hasNext(); ) {
            characters.add(it.next());
        }
        languages.put(langDataReader.getCurrentLang(), new ArrayList<>(characters));

        JSONObject langData = null;
        Log.d(logTag, languages.toString());

        ArrayList<String> sourceLanguages = LangDataReader.getAvailableSourceLanguages(context);
        if(sourceLanguages == null) {
            Log.d(logTag, "Could not find any downloaded languages.");
            return null;
        }
        // for each lang file that is not currentFile, add its characters to the hashmap
        for(String file: sourceLanguages) {
            Log.d(logTag, "Reading file " + file);

            // ignore the currently loaded file
            if (file.equals(langDataReader.getCurrentFile()))
                continue;

            langDataReader = new LangDataReader(LangDataReader.getLangFile(file), context);

            langData = langDataReader.getLangData();
            characters.clear();
            for(Iterator<String> it = langData.keys(); it.hasNext(); ) {
                characters.add(it.next());
            }
            // associate the hashmap with a clone of the value so that changes to the value
            // in future iterations are not picked up in the hashmap
            languages.put(langDataReader.getCurrentLang(), new ArrayList<>(characters));
        }
        Log.d(logTag, "languages loaded: " + languages);
        // now, we attempt to detect the input language
        String[] langs = languages.keySet().toArray(new String[0]);
        HashMap<String, Integer> score = new HashMap<>();
        for (char ch: input.toCharArray()) {
            for (String lang: langs) {
                if (languages.get(lang).contains(ch + "")) {
                    Log.d(logTag, " " + ch + " matches a character in the " + lang + " set.");
                    if (score.containsKey(lang)) {
                        score.put(lang, score.get(lang) + 1);
                    }
                    else
                        score.put(lang, 1);
                }
            }
        }

        String langDetected = null;
        int max_score = 0;
        for (Map.Entry<String, Integer> entry: score.entrySet()) {
            if (langDetected == null) {
                langDetected = entry.getKey();
                max_score = entry.getValue();
                continue;
            }
            if (entry.getValue() > max_score) {
                langDetected = entry.getKey();
                max_score = entry.getValue();
            }
        }
        Log.d(logTag, "Detected " + langDetected + " in input string with score " + max_score);

        return langDetected;
    }
}
