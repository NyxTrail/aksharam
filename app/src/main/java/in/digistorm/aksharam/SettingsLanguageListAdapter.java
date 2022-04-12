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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import in.digistorm.aksharam.util.LanguageDataDownloader;
import in.digistorm.aksharam.util.Log;
import in.digistorm.aksharam.util.OnRequestCompleted;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Locale;

public class SettingsLanguageListAdapter extends RecyclerView.Adapter<SettingsLanguageListAdapter.ViewHolder> {
    private final String logTag = "SettingsLangListAdapter";
    private final ArrayList<String> fileList;
    private final JSONArray onlineFiles;
    private final Activity activity;

    private class DownloadClickListener implements View.OnClickListener {
        private final ViewHolder holder;
        private final SettingsLanguageListAdapter adapter;

        public DownloadClickListener(ViewHolder holder, SettingsLanguageListAdapter adapter) {
            this.holder = holder;
            this.adapter = adapter;
        }

        @Override
        public void onClick(View v) {
            try {
                String fileName = onlineFiles.getJSONObject(holder.getAdapterPosition()).getString("name");
                String URL = onlineFiles.getJSONObject(holder.getAdapterPosition()).getString("download_url");
                Log.d(logTag, "Downloading file " + fileName);
                LanguageDataDownloader dataDownloader = new LanguageDataDownloader();
                dataDownloader.download(fileName, URL, activity, new OnRequestCompleted() {
                    @Override
                    public void onDownloadCompleted() {
                        Log.d(logTag, "Download completed for file: " + fileName);
                        v.setVisibility(View.GONE);
                        ImageView deleteIV = holder.getParentConstraintLayout().findViewById(R.id.ManageLanguageDeleteIV);
                        deleteIV.setVisibility(View.VISIBLE);
                        DeleteClickListener deleteClickListener = new DeleteClickListener(holder, true, adapter);
                        deleteIV.setOnClickListener(deleteClickListener);
                        // transliterator.getLangDataReader().getAvailableSourceLanguages(activity);
                        GlobalSettings.getInstance().invokeDataFileListChangedListeners();
                    }

                    @Override
                    public void onDownloadFailed(Exception e) {
                        Toast.makeText(activity, R.string.could_not_download_file, Toast.LENGTH_LONG).show();
                        Log.d(logTag, "Download failed for file " + fileName + " due to exception: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch(JSONException je) {
                Log.d("DownloadClickListener", "JSONException while unpacking onlineFiles.");
                je.printStackTrace();
            }
        }
    }

    private class DeleteClickListener implements View.OnClickListener {
        private final ViewHolder holder;
        private final boolean onlineFilesAvailable;
        private final SettingsLanguageListAdapter adapter;

        public DeleteClickListener(ViewHolder holder, Boolean onlineFilesAvailable, SettingsLanguageListAdapter adapter) {
            this.holder = holder;
            this.onlineFilesAvailable = onlineFilesAvailable;
            this.adapter = adapter;
        }

        @Override
        public void onClick(View v) {
            if(!onlineFilesAvailable) {
                int pos = holder.getAdapterPosition();
                String fileName = fileList.get(pos);
                Log.d(logTag, "Deleting file " + fileName);
                activity.deleteFile(fileName);
                fileList.remove(fileName);
                adapter.notifyItemRemoved(pos);
                if(activity.getFilesDir().list().length  > 0)
                    GlobalSettings.getInstance().invokeDataFileListChangedListeners();
                else
                    GlobalSettings.getInstance().clearDataFileListChangedListeners();
                return;
            }

            try {
                String fileName = onlineFiles.getJSONObject(holder.getAdapterPosition()).getString("name");
                Log.d(logTag, "Deleting file " + fileName);
                activity.deleteFile(fileName);
                v.setVisibility(View.GONE);
                ImageView downloadIV = holder.getParentConstraintLayout().findViewById(R.id.ManageLanguageDownloadIV);
                downloadIV.setVisibility(View.VISIBLE);
                DownloadClickListener downloadClickListener = new DownloadClickListener(holder, adapter);
                downloadIV.setOnClickListener(downloadClickListener);
                // Transliterator.getLangDataReader().getAvailableSourceLanguages(activity);
                if(activity.getFilesDir().list().length  > 0)
                    GlobalSettings.getInstance().invokeDataFileListChangedListeners();
                else
                    GlobalSettings.getInstance().clearDataFileListChangedListeners();
            } catch(JSONException je) {
                Log.d("DeleteClickListener", "JSONException when fetching file from online files list");
                je.printStackTrace();
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(logTag, "Initialising adapter for managing language data file list...");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.settings_language_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView langItemTV = holder.getParentConstraintLayout().findViewById(R.id.ManageLanguageTV);

        try {
            if(onlineFiles != null) {
                Log.d(logTag, "Data file list from repo is available.");
                final String fileName = onlineFiles.getJSONObject(position).getString("name");
                final String URL = onlineFiles.getJSONObject(position).getString("download_url");
                // if we have downloaded this file, show the delete icon
                if(fileList.contains(fileName)) {
                    Log.d(logTag, "Found file " + fileName + " in the local download directory.");
                    ImageView deleteIV = holder.getParentConstraintLayout().findViewById(R.id.ManageLanguageDeleteIV);
                    deleteIV.setVisibility(View.VISIBLE);
                    DeleteClickListener deleteClickListener = new DeleteClickListener(holder, true, this);
                    deleteIV.setOnClickListener(deleteClickListener);
                }
                else {
                    Log.d(logTag, "Could not find file " + fileName + " in the local download directory.");
                    ImageView downloadIV = holder.getParentConstraintLayout().findViewById(R.id.ManageLanguageDownloadIV);
                    downloadIV.setVisibility(View.VISIBLE);
                    DownloadClickListener downloadClickListener = new DownloadClickListener(holder, this);
                    downloadIV.setOnClickListener(downloadClickListener);
                }
                String text = fileName.substring(0, 1).toUpperCase(Locale.ROOT) + fileName.substring(1, fileName.length() - 5);
                langItemTV.setText(text);
            }
            else if(fileList != null) {
                Log.d(logTag, "Could not get online files. Displaying local files only.");
                // if online files are not available, at least display the local files
                String text = fileList.get(position);
                text = text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1, text.length() - 5);
                langItemTV.setText(text);
                ImageView deleteIV = holder.getParentConstraintLayout().findViewById(R.id.ManageLanguageDeleteIV);
                deleteIV.setVisibility(View.VISIBLE);

                DeleteClickListener deleteClickListener = new DeleteClickListener(holder, false, this);
                deleteIV.setOnClickListener(deleteClickListener);
            }
            // else... what happens if we did not get file online and there are no files in device?
            // that should usually take us back to the initialisation activity (not tested)
        } catch(JSONException je) {
            Log.d(logTag, "JSONException caught while populating language item at position " + position);
            je.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if(onlineFiles != null)
            return onlineFiles.length();
        return fileList.size();
    }

    public SettingsLanguageListAdapter(ArrayList<String> fileList, JSONArray onlineFiles, Activity activity) {
        this.fileList = fileList;
        this.onlineFiles = onlineFiles;
        this.activity = activity;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout parentConstraintLayout;

        public ViewHolder(@NonNull View view) {
            super(view);
            parentConstraintLayout = view.findViewById(R.id.ManageLanguageListItemCL);
            // TODO: set the click listener
        }

        public ConstraintLayout getParentConstraintLayout() {
            return parentConstraintLayout;
        }
    }
}
