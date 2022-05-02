package in.digistorm.aksharam.activities.initialise;

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

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;

import java.io.IOException;

import in.digistorm.aksharam.R;
import in.digistorm.aksharam.activities.main.MainActivity;
import in.digistorm.aksharam.util.GlobalSettings;
import in.digistorm.aksharam.util.LangDataReader;
import in.digistorm.aksharam.util.LanguageDataDownloader;
import in.digistorm.aksharam.util.Log;
import in.digistorm.aksharam.util.OnRequestCompleted;

public class InitialiseAppActivity extends AppCompatActivity {
    private final String logTag = "InitialiseAppActivity";

    private LanguageDataDownloader languageDataDownloader;
    private LanguageDataFileListAdapter adapter;

    private AlertDialog dialog;

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
        if(LangDataReader.areDataFilesAvailable(this) == null) {
            Log.d(logTag, "No data files found!! Continuing initialisation activity");
            // we cannot start the main activity
            return false;
        }

        Log.d(logTag, "Starting MainActivity...");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        // Main activity is started
        return true;
    }

    public void proceed(View v) {
        Log.d(logTag, "Proceed button clicked!");

        if(adapter == null || adapter.getItemCount() <= 0)
            return ;

        JSONArray dataFileList = adapter.getDataFileList();
        for(int i = 0; i < dataFileList.length(); i++) {
            if(dataFileList.optJSONObject(i).optBoolean("selected", false))
                break;

            // if we have gone through all items in the list and not found a single item marked "selected"
            if(i == dataFileList.length() - 1) {
                // show a message
                ((TextView) findViewById(R.id.InitialiseAppHintTV))
                        .setText(R.string.initialisation_no_file_selected);
                // and return
                return ;
            }
        }

        v.setEnabled(false);
        findViewById(R.id.InitialiseAppProgressBar).setVisibility(View.VISIBLE);
        languageDataDownloader.download(adapter.getDataFileList(),
                this,
                new OnRequestCompleted() {
                    @Override
                    public void onDownloadCompleted() {
                        Log.d(logTag, "Download completed; starting MainActivity...");
                        startMainActivity();
                        Log.d(logTag, "InitialiseAppActivity finishing...");
                        finish();
                    }

                    @Override
                    public void onDownloadFailed(Exception e) {
                        v.setEnabled(true);
                        findViewById(R.id.InitialiseAppProgressBar).setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), R.string.could_not_download_file,
                                Toast.LENGTH_LONG).show();
                        Log.d(logTag, "Download failed due to exception " + e);
                        e.printStackTrace();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(logTag, "Activity destroying...");
    }

    public void showNoInternetDialog() {
        Log.d("NoInternetDialog", "Showing NoInternetDialog.");
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(R.string.could_not_download_file)
                .setTitle(R.string.no_internet)
                .setCancelable(false)
                .setNegativeButton(R.string.exit, (dialog, which) -> {
                    Log.d("NoInternetDialog", "Exit button was clicked");
                    dialog.dismiss();
                    finish();
                })
                .setPositiveButton(R.string.retry_download, (dialog, which) -> {
                    setUpActivity();
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Log.d("NoInternetDialog", "Dialog dismissed!");
                    }
                });
        dialog = dialogBuilder.create();
        dialog.show();
    }

    protected void setUpActivity() {
        // Attempt to start main activity
        boolean mainActivityStarted = startMainActivity();
        InitialiseAppActivity activity = this;
        if(!mainActivityStarted) {
            // if we are not able to start the main activity...
            // continue setting up the initialisation activity (current activity)
            setContentView(R.layout.initialise_app_activity);

            languageDataDownloader = new LanguageDataDownloader();

            GlobalSettings.getInstance().getThreadPoolExecutor().execute(() -> {
                try {
                    showLanguageDataSelectionList(languageDataDownloader.getLanguageDataFiles());
                }
                catch (IOException ie) {
                    Log.d(logTag, "IOException caught while downloading language list");
                    runOnUiThread(this::showNoInternetDialog);
                }
            });
            findViewById(R.id.InitialiseAppProceedButton).setOnClickListener(v -> proceed(v));
        }
        else
            finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(logTag, "Starting app...");
        GlobalSettings.createInstance(this);

        if(GlobalSettings.getInstance().getDarkMode()) {
            // which mode did the activity start in?
            int nightMode = AppCompatDelegate.getDefaultNightMode();
            // If dark mode is enabled, this causes activity to restart
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            // continue activity initialisation only if activity started in night mode
            if(nightMode == AppCompatDelegate.MODE_NIGHT_YES)
                setUpActivity();
        }
        else { // if light mode; nothing special, it should be light mode by default
            setUpActivity();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
