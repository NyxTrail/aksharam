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
package `in`.digistorm.aksharam.util

import retrofit2.http.GET
import kotlin.Throws
import org.json.JSONArray
import android.app.Activity
import android.content.Context
import okhttp3.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Retrofit
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class LanguageDataDownloader {
    private val logTag = javaClass.simpleName
    private val baseUrl = "https://api.github.com/repos/NyxTrail/aksharam-data/"
    private val okHttpClient: OkHttpClient

    private interface GitHubAPI {
        @get:GET("contents?ref=1.0")
        val contents: Call<ResponseBody?>
    }

    private inner class LoggingInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            logDebug(
                logTag, String.format(
                    "Sending request%n%s %s on %s%n%s", request.method(),
                    request.url(),
                    chain.connection(), request.headers()
                )
            )
            val response = chain.proceed(request)
            logDebug(
                logTag, String.format(
                    "Received response for %s %s:%n%s%n",
                    response.request().method(),
                    response.request().url(),
                    response.headers()
                )
            )
            return response
        }
    }

    // accepts multiple files to download and downloads them one by one
    // returns true on success and false on failure
    fun download(
        dataFileList: JSONArray,
        activity: Activity,
        callback: OnRequestCompleted
    ) {
        GlobalSettings.instance?.threadPoolExecutor?.execute {
            var downloadFailed = false
            for (i in 0 until dataFileList.length()) {
                val item = dataFileList.optJSONObject(i)
                if (item != null && item.optBoolean("selected", false)) {
                    val request = Request.Builder()
                        .url(item.optString("download_url"))
                        .method("GET", null)
                        .headers(
                            Headers.Builder()
                                .add("accept: */*")
                                .build()
                        )
                        .build()
                    val call = okHttpClient.newCall(request)
                    try {
                        val response = call.execute()
                        if (response.body() == null) throw IOException("Obtained empty body.")
                        val responseString = response.body()!!.string()
                        // Let's log only first 100 characters of the file here
                        logDebug(
                            logTag,
                            "Response received.\n" +
                            "Code: ${response.code()}\n" +
                            "Data: ${responseString.substring(0, 100)} ..."
                        )
                        if(response.code() == 200) {
                            val fos = activity.openFileOutput(
                                item.getString("name"),
                                Context.MODE_PRIVATE
                            )
                            logDebug(
                                logTag, "Saving file to " + activity.filesDir.name
                                        + "/" + item.getString("name")
                            )
                            fos.write(responseString.toByteArray(StandardCharsets.UTF_8))
                            fos.close()
                        }
                        else {
                            logDebug(logTag, "Error code received from server.")
                            callback.onDownloadFailed(Exception("Download failed with status: ${response.code()}"))
                        }
                    } catch (ie: IOException) {
                        logDebug(logTag, "IOException while downloading file")
                        ie.printStackTrace()
                        activity.runOnUiThread { callback.onDownloadFailed(ie) }
                        downloadFailed = true
                        break
                    } catch (je: JSONException) {
                        logDebug(logTag, "JSONException while downloading file")
                        je.printStackTrace()
                        activity.runOnUiThread { callback.onDownloadFailed(je) }
                        downloadFailed = true
                        break
                    }
                }
            }
            // If we successfully iterated through everything and still reached here,
            // our assigned tasks are complete (this does not necessarily mean we downloaded something
            // since it is possible that we were passed in a list were nothing was marked as "selected")
            // Still, we count this as success (if downloadFailed is not true) and call the success callback
            if (!downloadFailed) activity.runOnUiThread { callback.onDownloadCompleted() }
        }
    }

    // returns true on success and false on failure
    fun download(
        fileName: String, URL: String,
        activity: Activity,
        callback: OnRequestCompleted
    ) {
        val request = Request.Builder()
            .url(URL)
            .method("GET", null)
            .headers(
                Headers.Builder()
                    .add("accept: */*")
                    .build()
            )
            .build()
        val call = okHttpClient.newCall(request)
        GlobalSettings.instance?.threadPoolExecutor?.execute {
            try {
                val response = call.execute()
                if (response.body() == null) throw IOException("Obtained empty body.")
                val responseString = response.body()!!.string()
                // Let's log only first 100 characters of the file here
                logDebug(logTag, "Obtained response: " + responseString.substring(0, 100) + "...")
                val fos = activity.openFileOutput(fileName, Context.MODE_PRIVATE)
                logDebug(logTag, "Saving file to " + activity.filesDir.name + "/" + fileName)
                fos.write(responseString.toByteArray(StandardCharsets.UTF_8))
                fos.close()
                activity.runOnUiThread { callback.onDownloadCompleted() }
            } catch (fe: FileNotFoundException) {
                logDebug(logTag, "FileNotFoundException caught while trying to save file: ")
                fe.printStackTrace()
                activity.runOnUiThread { callback.onDownloadFailed(fe) }
            } catch (ie: IOException) {
                logDebug(logTag, "IOException while downloading $URL")
                ie.printStackTrace()
                activity.runOnUiThread { callback.onDownloadFailed(ie) }
            }
        }
    }// response body from okhttp can be consumed only once:

    // https://square.github.io/okhttp/4.x/okhttp/okhttp3/-response-body/#the-response-body-can-be-consumed-only-once
    // Fetch language data files from the repository
    @get:Throws(IOException::class)
    val languageDataFiles: JSONArray
        get() {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build()
            val gitHubAPI = retrofit.create(GitHubAPI::class.java)
            logDebug(logTag, "Fetching language data files from git repo.")
            val responseString: String
            val response: retrofit2.Response<ResponseBody?>
            return try {
                // response body from okhttp can be consumed only once:
                // https://square.github.io/okhttp/4.x/okhttp/okhttp3/-response-body/#the-response-body-can-be-consumed-only-once
                response = gitHubAPI.contents.execute()
                if (response.body() == null) throw IOException("Obtained empty body.")
                responseString = response.body()!!.string()
                logDebug(logTag, responseString)
                val jsonArray = JSONArray(responseString)
                for (i in 0 until jsonArray.length()) {
                    val fileName = jsonArray.getJSONObject(i).optString("name")
                    if (!fileName.endsWith(".json")) jsonArray.remove(i)
                }
                JSONArray(responseString)
            } catch (e: IOException) {
                logDebug(logTag, "IOException caught while fetching language data file list.")
                throw IOException("Could not download list of files from git.")
            } catch (e: JSONException) {
                logDebug(logTag, "IOException caught while fetching language data file list.")
                throw IOException("Could not download list of files from git.")
            }
        }

    init {
        okHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(LoggingInterceptor())
            .callTimeout(30, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()
    }
}