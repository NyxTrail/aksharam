package in.digistorm.aksharam.util;

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
import android.util.Log;

import androidx.annotation.NonNull;

import in.digistorm.aksharam.GlobalSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;

public class LanguageDataDownloader {
    private final String baseUrl = "https://git.digistorm.in/api/v1/repos/alan/aksharam-data/";
    private final String logTag = "LanguageDataDownloader";

    private final OkHttpClient okHttpClient;

    private interface GiteaAPI {
        @GET("contents")
        Call<ResponseBody> getContents();
    }

    private class LoggingInterceptor implements Interceptor {
        @NonNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            Log.d(logTag, String.format("Sending request%n%s %s on %s%n%s", request.method(),
                    request.url(),
                    chain.connection(), request.headers()));

            Response response = chain.proceed(request);
            Log.d(logTag, String.format("Received response for %s %s:%n%s%n",
                    response.request().method(),
                    response.request().url(),
                    response.headers()));

            return response;
        }
    }

    // accepts multiple files to download and downloads them one by one
    // returns true on success and false on failure
    public void download(JSONArray dataFileList,
                         @NonNull Activity activity,
                         OnRequestCompleted callback) {

        GlobalSettings.getInstance().getThreadPoolExecutor().execute(() -> {
            boolean downloadFailed = false;
            for (int i = 0; i < dataFileList.length(); i++) {
                JSONObject item = dataFileList.optJSONObject(i);
                if (item != null && item.optBoolean("selected", false)) {
                    Request request = new Request.Builder()
                            .url(item.optString("download_url"))
                            .method("GET", null)
                            .headers(new Headers.Builder()
                                    .add("accept: */*")
                                    .build())
                            .build();
                    okhttp3.Call call = okHttpClient.newCall(request);
                    try {
                        Response response = call.execute();
                        if (response.body() == null)
                            throw new IOException("Obtained empty body.");
                        String responseString = response.body().string();
                        // Let's log only first 100 characters of the file here
                        Log.d(logTag, "Obtained response: " + responseString.substring(0, 100) + "...");

                        FileOutputStream fos = activity.openFileOutput(item.getString("name"),
                                Context.MODE_PRIVATE);
                        Log.d(logTag, "Saving file to " + activity.getFilesDir().getName()
                                + "/" + item.getString("name"));
                        fos.write(responseString.getBytes(StandardCharsets.UTF_8));
                        fos.close();
                    } catch (IOException ie) {
                        Log.d(logTag, "IOException while downloading file");
                        ie.printStackTrace();
                        activity.runOnUiThread(() -> callback.onDownloadFailed(ie));
                        downloadFailed = true;
                        break;
                    } catch (JSONException je) {
                        Log.d(logTag, "JSONException while downloading file");
                        je.printStackTrace();
                        activity.runOnUiThread(() -> callback.onDownloadFailed(je));
                        downloadFailed = true;
                        break;
                    }
                }
            }
            // If we successfully iterated through everything and still reached here,
            // our assigned tasks are complete (this does not necessarily mean we downloaded something
            // since it is possible that we were passed in a list were nothing was marked as "selected")
            // Still, we count this as success (if downloadFailed is not true) and call the success callback
            if(!downloadFailed)
                activity.runOnUiThread(callback::onDownloadCompleted);
        });
    }

    // returns true on success and false on failure
    public void download(String fileName, String URL,
                            @NonNull Activity activity,
                            OnRequestCompleted callback) {
        Request request = new Request.Builder()
                .url(URL)
                .method("GET",  null)
                .headers(new Headers.Builder()
                        .add("accept: */*")
                        .build())
                .build();
        okhttp3.Call call = okHttpClient.newCall(request);

        GlobalSettings.getInstance().getThreadPoolExecutor().execute(() -> {
            try {
                Response response = call.execute();
                if(response.body() == null)
                    throw new IOException("Obtained empty body.");
                String responseString = response.body().string();
                // Let's log only first 100 characters of the file here
                Log.d(logTag, "Obtained response: " + responseString.substring(0, 100) + "...");

                FileOutputStream fos = activity.openFileOutput(fileName, Context.MODE_PRIVATE);
                Log.d(logTag, "Saving file to " + activity.getFilesDir().getName() + "/" + fileName);
                fos.write(responseString.getBytes(StandardCharsets.UTF_8));
                fos.close();

                activity.runOnUiThread(callback::onDownloadCompleted);
            } catch (FileNotFoundException fe) {
                Log.d(logTag, "FileNotFoundException caught while trying to save file: ");
                fe.printStackTrace();
                activity.runOnUiThread(() -> callback.onDownloadFailed(fe));
            } catch (IOException ie) {
                Log.d(logTag, "IOException while downloading " + URL);
                ie.printStackTrace();
                activity.runOnUiThread(() -> callback.onDownloadFailed(ie));
            }
        });
    }

    public LanguageDataDownloader() {
        okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new LoggingInterceptor())
                .callTimeout(30, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    // Fetch language data files from the repository
    public JSONArray getLanguageDataFiles() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build();
        GiteaAPI giteaAPI = retrofit.create(GiteaAPI.class);

        Log.d(logTag, " Fetching language data files from git repo.");
        String responseString;
        retrofit2.Response<ResponseBody> response = null;
        try {
            // response body from okhttp can be consumed only once:
            // https://square.github.io/okhttp/4.x/okhttp/okhttp3/-response-body/#the-response-body-can-be-consumed-only-once
            response = giteaAPI.getContents().execute();
            if(response.body() == null)
                throw new IOException("Obtained empty body.");
            responseString = response.body().string();
            Log.d(logTag, responseString);
            return new JSONArray(responseString);
        } catch (IOException | JSONException e) {
            if(e instanceof IOException)
                Log.d(logTag, "IOException caught while fetching language data file list.");
            else
                Log.d(logTag, "JSONException caught while fetching data file list.");
            throw new IOException("Could not download list of files from git.");
        }
    }
}
