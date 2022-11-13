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
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.BufferedReader
import java.io.IOException
import java.util.*

fun getBufferedReader(file: String, context: Context): BufferedReader {
    return context.openFileInput(file).bufferedReader()
}

inline fun <reified T> readValue(file: String, context: Context): T  {
    val bufferedReader = getBufferedReader(file, context)
    val mapper = jacksonObjectMapper()
    return mapper.readValue(bufferedReader)
}

fun getLanguageData(file: String?, context: Context): Language? {
    val logTag = "LangDataReader::getLanguageData"
    logDebug(logTag, "Initialising lang data file: $file")
    var fileLC = file?.lowercase() ?: return null

    if (!fileLC.endsWith(".json")) fileLC = "$fileLC.json"
    val mapper = jacksonObjectMapper()
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    return try {
        // Log the entire data file in DEBUG mode; assume readable as string lines
        if (BuildConfig.DEBUG) {
            getBufferedReader(fileLC, context).forEachLine { line -> logDebug(logTag, line) }
        }

        val language = readValue<Language>(fileLC, context)
        language.language = fileLC.lowercase().replace(".json", "")
        if (BuildConfig.DEBUG) {
            logDebug(logTag, "File read and deserialized: $fileLC")
            logDebug(logTag, "Deserialized language: " + mapper.writeValueAsString(language))
        }
        language
    } catch (e: IOException) {
        logDebug(logTag, "Read operation failed on file: $fileLC")
        e.printStackTrace()
        null
    }
}

fun getAllLanguages(context: Context): LinkedHashMap<String, Language> {
    val logTag = "LangDataReader::getAllLanguages"
    val files = ArrayList<String>()
    Collections.addAll(files, *context.fileList())
    val languageList = LinkedHashMap<String, Language>()
    if (files.size == 0) {
        return languageList
    }
    val mapper = jacksonObjectMapper()
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    for (file in files) {
        try {
            val language: Language = readValue(file, context)
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
    val logTag = "LangDataReader::getDownloadedLanguages"
    logDebug(logTag, "finding all available lang data files")
    val files = context.filesDir.list()
    if (files == null || files.isEmpty()) return ArrayList() // return an empty array list
    val sourceLanguages = ArrayList<String>()
    for (file in files) {
        logDebug(logTag, "found file $file")
        // if file is not json, ignore it
        if (!file.lowercase().contains(".json")) continue
        val languageName = file.replace(".json", "")
        sourceLanguages.add(languageName.substring(0, 1).uppercase() + languageName.substring(1))
    }
    logDebug(logTag, "source languages found: $sourceLanguages")
    return sourceLanguages
}

fun getDownloadedFiles(context: Context): ArrayList<String> {
    val logTag = "LangDataReader::getDownloadedFiles"
    logDebug(logTag, "Finding all available lang data files")
    val files = context.filesDir.list()
    if (files == null || files.isEmpty()) return ArrayList() // return an empty array list
    val fileList = ArrayList<String>()
    Collections.addAll(fileList, *files)
    return fileList
}