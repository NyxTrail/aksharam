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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LanguageDataFileListAdapter extends RecyclerView.Adapter<LanguageDataFileListAdapter.ViewHolder> {
    // apparently log tag can at most be 23 characters
    private final String logTag = "LangDataFileListAdapter";
    private JSONArray dataFileList;
    // map is filename, true/false depending on whether file is selected
    private final Map<Integer, Boolean> selectedFiles = new HashMap<>();

    public LanguageDataFileListAdapter(JSONArray dataFileList) {
        this.dataFileList = dataFileList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(logTag, "Initialising adapter for Language Data File List...");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.language_data_file_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(logTag, "binding checkedtextview at position: " + position);
        try {
            String language = dataFileList.getJSONObject(position).getString("name");
            language = language.substring(0, 1).toUpperCase(Locale.ROOT)
                    + language.substring(1, language.length() - ".json".length());
            holder.getCheckedTextView().setText(language);
            holder.getCheckedTextView().setOnClickListener(v -> {
                CheckedTextView checkedTextView = holder.getCheckedTextView();
                checkedTextView.setChecked(!checkedTextView.isChecked());
                selectedFiles.put(position, checkedTextView.isChecked());
            });
        } catch (JSONException e) {
            Log.d(logTag, "JSONException caught while processing data file list obtained from server");
            e.printStackTrace();
        }
    }

    public JSONObject getItem(int position) throws JSONException {
        return dataFileList.getJSONObject(position);
    }

    public Map<Integer, Boolean> getSelectedFiles() {
        return selectedFiles;
    }

    @Override
    public int getItemCount() {
        return dataFileList.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckedTextView checkedTextView;

        public ViewHolder(View view) {
            super(view);
            checkedTextView = view.findViewById(R.id.LanguageDataFileListCTV);
            checkedTextView.setOnClickListener(
                    v -> checkedTextView.setChecked(!checkedTextView.isChecked()));
        }

        public CheckedTextView getCheckedTextView() {
            return checkedTextView;
        }
    }
}
