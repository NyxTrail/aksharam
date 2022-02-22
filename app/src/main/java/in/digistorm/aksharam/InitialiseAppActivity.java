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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import in.digistorm.aksharam.util.LanguageDataDownloader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class InitialiseAppActivity extends AppCompatActivity {
    private final String logTag = InitialiseAppActivity.class.getName();

    // Remember, URLs must end in a '/' or Retrofit rebels
    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private final BlockingDeque<Runnable> workQueue = new LinkedBlockingDeque<>();
    // max idle time a thread is kept alive
    private static final int KEEP_ALIVE_TIME = 20;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            NUMBER_OF_CORES,
            NUMBER_OF_CORES,
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT,
            workQueue);

    private LanguageDataDownloader languageDataDownloader;
    private LanguageDataFileListAdapter adapter;

    private class SimpleThreadFactory implements ThreadFactory {
        private String name;

        SimpleThreadFactory(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, name);
        }
    }

    private void showLanguageDataSelectionList(JSONArray languageDataFiles) {
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
                ConstraintLayout constraintLayout = findViewById(R.id.InitialiseAppCL);
                RecyclerView languageDataListRV = new RecyclerView(getApplicationContext());
                languageDataListRV.setId("languageDataListRV".hashCode());
                languageDataListRV.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                adapter = new LanguageDataFileListAdapter(languageDataFiles);
                languageDataListRV.setAdapter(adapter);
                // RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(0 , 0);
                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(0, 0);

                int marginLeftRight = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        16,
                        getApplicationContext().getResources().getDisplayMetrics());
                int marginTopBottom = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        8,
                        getApplicationContext().getResources().getDisplayMetrics());
                layoutParams.setMargins(marginLeftRight, marginTopBottom, marginLeftRight, marginTopBottom);
                languageDataListRV.setLayoutParams(layoutParams);
                languageDataListRV.setVerticalScrollBarEnabled(true);
                constraintLayout.addView(languageDataListRV);

                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);
                // it is possible to add a margin here, as the last parameter
                constraintSet.connect(languageDataListRV.getId(), ConstraintSet.TOP, R.id.InitialiseAppProgressBar, ConstraintSet.BOTTOM);
                constraintSet.connect(languageDataListRV.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.connect(languageDataListRV.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                constraintSet.connect(languageDataListRV.getId(), ConstraintSet.BOTTOM, R.id.InitialiseAppButtonsLL, ConstraintSet.TOP);

                constraintSet.applyTo(constraintLayout);
                Log.d(logTag, languageDataListRV.getLayoutManager().toString());

                ((Button) findViewById(R.id.InitialiseAppProceedButton)).setEnabled(true);
            }
        });
    }

    private void startMainActivity() {
        Log.d(logTag, "Attempting to start main activity...");
        // First check if we have data files available
        // This cannot be null because, according to the doc list() returns null only if it is invoked
        // on a non-directory file
        if(getApplicationContext().getFilesDir().list().length <= 0) {
            Log.d(logTag, "No data files found!! Reverting to initialisation activity");
            return;
        }

        // Get a reference to current activity, to be passed to the intent
        InitialiseAppActivity activity = this;
        Log.d(logTag, "Creating intent...");
        Intent intent = new Intent(activity, MainActivity.class);
        startActivity(intent);
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

        Iterator<Integer> it = selectedFiles.keySet().iterator();
        while(it.hasNext()) {
            int index = it.next();
            try {
                if(selectedFiles.get(index)) {
                    JSONObject item = adapter.getItem(index);
                    Log.d(logTag, "Downloading " + item.getString("name"));
                    threadPoolExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                languageDataDownloader.download(item.getString("name"),
                                        item.getString("download_url"),
                                        getApplicationContext());
                                // start MainActivity
                                startMainActivity();
                            } catch (JSONException e) {
                                Log.d(logTag, "JSONException caught while reading file "
                                        + "list during download");
                                e.printStackTrace();
                            }
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
        // Attempt to start main activity
        startMainActivity();

        setContentView(R.layout.initialise_app_activity);

        languageDataDownloader = new LanguageDataDownloader();

        threadPoolExecutor.setThreadFactory(new SimpleThreadFactory("LanguageDataDownloader"));
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                showLanguageDataSelectionList(languageDataDownloader.getLanguageDataFiles());
            }
        });
        ((Button) findViewById(R.id.InitialiseAppProceedButton)).setOnClickListener(v -> proceed());
    }
}
