package in.digistorm.aksharam.util;

import android.content.Context;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

import in.digistorm.aksharam.BuildConfig;

public class LangDataReader {
    public static Language getLanguageData(String file, Context context) {
        String logTag = "LangDataReader";
        Log.d(logTag, "Initialising lang data file: " + file);
        if(file == null || file.length() == 0)
            return null;

        file = file.toLowerCase(Locale.ROOT);
        if(!file.endsWith(".json"))
            file = file + ".json";

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        try {
            Language language = mapper.readValue(context.openFileInput(file), Language.class);
            language.setLanguage(file.toLowerCase(Locale.ROOT).replace(".json", ""));
            if(BuildConfig.DEBUG) {
                Log.d(logTag, "File read and deserialized: " + file);
                Log.d(logTag, "Deserialised language: " + mapper.writeValueAsString(language));
            }
            return language;
        } catch (IOException e) {
            Log.d(logTag, "Read operation failed on file: " + logTag);
            e.printStackTrace();
            return null;
        }
    }

    public static LinkedHashMap<String, Language> getAllLanguages(Context context) {
        String logTag = "LangDataReader";
        ArrayList<String> files = new ArrayList<>();
        Collections.addAll(files, context.fileList());

        LinkedHashMap<String, Language> languageList = new LinkedHashMap<>();

        if(files.size() == 0) {
            return languageList;
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        for(String file: files) {
            try {
                Language language = mapper.readValue(context.openFileInput(file), Language.class);
                language.setLanguage(file.toLowerCase(Locale.ROOT).replace(".json", ""));
                languageList.put(file.toLowerCase(Locale.ROOT).replace(".json", ""), language);
            } catch (IOException e) {
                Log.d(logTag, "Read operation failed on file: " + logTag);
                e.printStackTrace();
                return languageList;
            }
        }
        return languageList;
    }

    /* Find downloaded languages. Return list of such languages with
       ".json" extension removed and first letter capitalised. Returns empty
        array list if no files found.
     */
    public static ArrayList<String> getDownloadedLanguages(Context context) {
        String logTag = "LangDataReader";
        Log.d(logTag, "finding all available lang data files");
        String[] files = context.getFilesDir().list();

        if(files == null || files.length == 0)
            return new ArrayList<>(); // return an empty array list

        ArrayList<String> sourceLangs = new ArrayList<>();
        for(String file: files) {
            Log.d(logTag, "found file " + file);
            // if file is not json, ignore it
            if(!file.toLowerCase(Locale.ROOT).contains(".json"))
                continue;
            file = file.replace(".json", "");
            sourceLangs.add(file.substring(0,1).toUpperCase(Locale.ROOT) + file.substring(1));
        }
        Log.d(logTag, "source languages found: " + sourceLangs);
        return sourceLangs;
    }

    public static ArrayList<String> getDownloadedFiles(Context context) {
        String logTag = "LangDataReader";
        Log.d(logTag, "finding all available lang data files");
        String[] files = context.getFilesDir().list();

        if(files == null || files.length == 0)
            return new ArrayList<>(); // return an empty array list

        ArrayList<String> fileList = new ArrayList<>();
        Collections.addAll(fileList, files);
        return fileList;
    }
}
