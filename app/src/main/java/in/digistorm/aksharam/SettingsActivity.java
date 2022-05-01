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
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import in.digistorm.aksharam.util.LanguageDataDownloader;
import in.digistorm.aksharam.util.Log;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class SettingsActivity extends AppCompatActivity {
    private final String logTag = "SettingsActivity";

    private void populateLanguageList() {
        RecyclerView manageLanguagesRV = findViewById(R.id.SettingsActivityManageLanguagesRV);

        // get downloaded files
        String[] files = Transliterator.getCurrentTransliterator()
                .getLangDataReader().getDataFiles(this);
        ArrayList<String> filesList = new ArrayList<>(Arrays.asList(files));
        Log.d(logTag, "List of files available: " + filesList);

        SettingsActivity self = this;

        // get files online
        GlobalSettings.getInstance().getThreadPoolExecutor().execute(() -> {
            LanguageDataDownloader languageDataDownloader = new LanguageDataDownloader();
            final JSONArray onlineFiles;
            try {
                onlineFiles = languageDataDownloader.getLanguageDataFiles();
                runOnUiThread(() -> {
                    if (onlineFiles == null) {
                        // show Toast saying we could not download the files online
                        Toast.makeText(self, R.string.could_not_download_file_list, Toast.LENGTH_LONG).show();
                    }
                    // hide the progress bar
                    findViewById(R.id.SettingsActivityManageLanguagesPB).setVisibility(View.GONE);
                    manageLanguagesRV.setVisibility(View.VISIBLE);
                    Log.d(logTag, "Setting adapter for language list");
                    manageLanguagesRV.setLayoutManager(new LinearLayoutManager(self));
                    SettingsLanguageListAdapter manageLanguageListAdapter = new SettingsLanguageListAdapter(filesList, onlineFiles, this);
                    manageLanguagesRV.setAdapter(manageLanguageListAdapter);
                });
            }
            catch (IOException ie) {
                runOnUiThread(() -> {
                    Toast.makeText(self, R.string.could_not_download_file_list, Toast.LENGTH_LONG).show();
                    // hide the progress bar
                    findViewById(R.id.SettingsActivityManageLanguagesPB).setVisibility(View.GONE);
                    manageLanguagesRV.setVisibility(View.VISIBLE);
                    Log.d(logTag, "Setting adapter for language list");
                    manageLanguagesRV.setLayoutManager(new LinearLayoutManager(self));
                    SettingsLanguageListAdapter manageLanguageListAdapter = new SettingsLanguageListAdapter(filesList, null, this);
                    manageLanguagesRV.setAdapter(manageLanguageListAdapter);
                });
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(logTag, "Setting up the settings activity...");
        setContentView(R.layout.activity_settings);

        populateLanguageList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(logTag, "Clearing listeners before being destroyed.");
        if(LangDataReader.areDataFilesAvailable(this) == null)
            GlobalSettings.getInstance().clearDataFileListChangedListeners();
    }
}
