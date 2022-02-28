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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import in.digistorm.aksharam.util.LanguageDataDownloader;
import in.digistorm.aksharam.util.OnRequestCompleted;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

public class InitialiseAppActivity extends AppCompatActivity {
    private final String logTag = InitialiseAppActivity.class.getName();

    private LanguageDataDownloader languageDataDownloader;
    private LanguageDataFileListAdapter adapter;

    private void showLanguageDataSelectionList(JSONArray languageDataFiles) {
        InitialiseAppActivity self = this;

        // This is called from the background thread; views can be updated only in the threads that
        // created them
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(logTag, "Updating RecyclerView in UI thread");
                // First, hide the progress bar
                findViewById(R.id.InitialiseAppProgressBar).setVisibility(View.INVISIBLE);
                // Set the appropriate hint
                ((TextView) findViewById(R.id.InitialiseAppHintTV)).setText(R.string.initialisation_choice_hint);

                RecyclerView languageListRV = findViewById(R.id.InitialiseAppLangListRV);
                adapter = new LanguageDataFileListAdapter(languageDataFiles);
                languageListRV.setLayoutManager(new LinearLayoutManager(self));
                languageListRV.setAdapter(adapter);

                ((Button) findViewById(R.id.InitialiseAppProceedButton)).setEnabled(true);
            }
        });
    }

    private boolean startMainActivity() {
        Log.d(logTag, "Attempting to start main activity...");
        // First check if we have data files available
        // This cannot be null because, according to the doc list() returns null only if it is invoked
        // on a non-directory file
        if(getFilesDir().list().length <= 0) {
            Log.d(logTag, "No data files found!! Reverting to initialisation activity");
            // we cannot start the main activity
            return false;
        }

        // Get a reference to current activity, to be passed to the intent
        InitialiseAppActivity activity = this;
        Log.d(logTag, "Creating intent...");
        Intent intent = new Intent(activity, MainActivity.class);
        startActivity(intent);
        // Main activity is started
        return true;
    }

    public void proceed() {
        Log.d(logTag, "Proceed button clicked!");

        if(adapter == null || adapter.getItemCount() <= 0)
            return ;

        Map<Integer, Boolean> selectedFiles = adapter.getSelectedFiles();
        if(selectedFiles.size() <= 0) {
            ((TextView) findViewById(R.id.InitialiseAppHintTV))
                    .setText(R.string.initialisation_no_file_selected);
            return ;
        }

        for (int index : selectedFiles.keySet()) {
            try {
                if (selectedFiles.get(index)) {
                    JSONObject item = adapter.getItem(index);
                    Log.d(logTag, "Downloading " + item.getString("name"));
                    Activity activity = this;
                    languageDataDownloader.download(item.getString("name"),
                            item.getString("download_url"),
                            this, new OnRequestCompleted() {
                                @Override
                                public void onDownloadCompleted() {
                                    startMainActivity();
                                    finish();
                                }

                                @Override
                                public void onDownloadFailed(Exception e) {
                                    Toast.makeText(activity, R.string.could_not_download_file, Toast.LENGTH_LONG).show();
                                    Log.d(logTag, "Download failed due to exception: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            });
                }
            } catch (JSONException e) {
                Log.d(logTag, "JSONException caught while reading file list during download");
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(logTag, "Starting app...");
        GlobalSettings.createInstance(this);
        if(GlobalSettings.getInstance().getDarkMode())
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Attempt to start main activity
        boolean attempt = startMainActivity();
        if(!attempt) {
            // if we are not able to start the main activity...
            // continue setting up the initialisation activity (current activity)
            setContentView(R.layout.initialise_app_activity);

            languageDataDownloader = new LanguageDataDownloader();

            GlobalSettings.getInstance().getThreadPoolExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    showLanguageDataSelectionList(languageDataDownloader.getLanguageDataFiles());
                }
            });
            ((Button) findViewById(R.id.InitialiseAppProceedButton)).setOnClickListener(v -> proceed());
        }
        else
            finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
