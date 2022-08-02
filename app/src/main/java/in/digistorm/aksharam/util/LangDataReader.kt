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

import `in`.digistorm.aksharam.BuildConfig
import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.DeserializationFeature
import java.io.IOException
import java.util.*

private const val logTag = "LangDataReader"

fun getLanguageData(file: String?, context: Context): Language? {
    logDebug(logTag, "Initialising lang data file: $file")
    var fileLC = file?.lowercase() ?: return null

    if (!fileLC.endsWith(".json")) fileLC = "$fileLC.json"
    val mapper = ObjectMapper()
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    return try {

        // Log the entire data file in DEBUG mode; assume readable as string lines
        if (BuildConfig.DEBUG) {
            for (line in context.openFileInput(fileLC).bufferedReader().lines())
                logDebug(logTag, line)
        }

        val language = mapper.readValue(context.openFileInput(fileLC), Language::class.java)
        language.language = fileLC.lowercase().replace(".json", "")
        if (BuildConfig.DEBUG) {
            logDebug(logTag, "File read and deserialized: $fileLC")
            logDebug(logTag, "Deserialised language: " + mapper.writeValueAsString(language))
        }
        language
    } catch (e: IOException) {
        logDebug(logTag, "Read operation failed on file: $fileLC")
        e.printStackTrace()
        null
    }
}

fun getAllLanguages(context: Context): LinkedHashMap<String, Language> {
    val logTag = "LangDataReader"
    val files = ArrayList<String>()
    Collections.addAll(files, *context.fileList())
    val languageList = LinkedHashMap<String, Language>()
    if (files.size == 0) {
        return languageList
    }
    val mapper = ObjectMapper()
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    for (file in files) {
        try {
            val language = mapper.readValue(context.openFileInput(file), Language::class.java)
            language.language = file.lowercase().replace(".json", "")
            languageList[file.lowercase().replace(".json", "")] = language
        } catch (e: IOException) {
            logDebug(logTag, "Read operation failed on file: $logTag")
            e.printStackTrace()
            return languageList
        }
    }
    return languageList
}

/* Find downloaded languages. Return list of such languages with
   ".json" extension removed and first letter capitalised. Returns empty
    array list if no files found.
 */
fun getDownloadedLanguages(context: Context): ArrayList<String> {
    logDebug(logTag, "finding all available lang data files")
    val files = context.filesDir.list()
    if (files == null || files.isEmpty()) return ArrayList() // return an empty array list
    val sourceLangs = ArrayList<String>()
    for (file in files) {
        logDebug(logTag, "found file $file")
        // if file is not json, ignore it
        if (!file.lowercase().contains(".json")) continue
        val languageName = file.replace(".json", "")
        sourceLangs.add(languageName.substring(0, 1).uppercase() + languageName.substring(1))
    }
    logDebug(logTag, "source languages found: $sourceLangs")
    return sourceLangs
}

fun getDownloadedFiles(context: Context): ArrayList<String> {
    logDebug(logTag, "finding all available lang data files")
    val files = context.filesDir.list()
    if (files == null || files.isEmpty()) return ArrayList() // return an empty array list
    val fileList = ArrayList<String>()
    Collections.addAll(fileList, *files)
    return fileList
}