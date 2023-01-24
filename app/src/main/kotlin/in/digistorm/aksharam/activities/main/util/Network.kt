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
package `in`.digistorm.aksharam.activities.main.util

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://api.github.com/repos/NyxTrail/aksharam-data/"

private val objectMapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

private val retrofit = Retrofit.Builder()
    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
    .baseUrl(BASE_URL)
    .build()

private val okHttpClient = OkHttpClient.Builder()
    // .addNetworkInterceptor(LoggingInterceptor())
    .callTimeout(30, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .writeTimeout(20, TimeUnit.SECONDS)
    .connectTimeout(10, TimeUnit.SECONDS)
    .build()

/**
 * The GitHub api returns a lot of useful properties. But we need just the name and the download url.
*/
data class LanguageFile(
    val name: String,
    val download_url: String,
)

interface GitHubApi {
    @GET("contents?ref=1.0")
    suspend fun getContents(): List<LanguageFile>
}

// Assumption: 'file' matches at the end of 'url'.
suspend fun downloadFile(
    url: String,
    errorString: String = "Download failed."
): String? {
    return withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .method("GET", null)
            .headers(
                Headers.Builder()
                    .add("accept: */*")
                    .build()
            )
            .build()
        val response = okHttpClient.newCall(request).execute()
        if (response.code() == 200) {
            return@withContext response.body()?.string()
        } else {
            throw Exception(errorString)
        }
    }
}

object Network {
    val onlineFiles: GitHubApi by lazy {
        retrofit.create(GitHubApi::class.java)
    }
}