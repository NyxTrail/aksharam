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
package `in`.digistorm.aksharam.activities.main.language

import `in`.digistorm.aksharam.BuildConfig
import android.content.Context
import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import `in`.digistorm.aksharam.activities.main.helpers.upperCaseFirstLetter
import `in`.digistorm.aksharam.activities.main.util.getBufferedReader
import `in`.digistorm.aksharam.activities.main.util.logDebug
import `in`.digistorm.aksharam.activities.main.util.logError
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import kotlin.collections.LinkedHashMap

inline fun <reified T> readValue(file: String, context: Context): T  {
    val bufferedReader = getBufferedReader(file, context)
    val mapper = jacksonObjectMapper()
    return mapper.readValue(bufferedReader)
}

/**
 * file: Name of file. If file does not end with ".json", we add ".json" to the end
 * and then look for the file.
 * context: Application/activity context.
 *
 * We set the `language` field in the Language class to the name of the current language
 */
fun getLanguageData(file: String, context: Context): Language? {
    val logTag = "LangDataReader::getLanguageData"
    logDebug(logTag, "Initialising lang data file: $file")
    var fileLC = file.lowercase()

    if (!fileLC.endsWith(".json")) fileLC = "$fileLC.json"
    val mapper = jacksonObjectMapper()
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    return try {
        // Log the entire data file in DEBUG mode; assume readable as string lines
        if (BuildConfig.DEBUG) {
            getBufferedReader(fileLC, context).forEachLine { line -> logDebug(logTag, line) }
        }

        val language = readValue<Language>(fileLC, context)
        language.language = fileLC
            .lowercase()
            .replace(".json", "")
            .upperCaseFirstLetter()
        if (BuildConfig.DEBUG) {
            logDebug(logTag, "File read and deserialized: $fileLC")
            logDebug(logTag, "Deserialized language: " + mapper.writeValueAsString(language))
        }
        language
    } catch (e: FileNotFoundException) {
        logDebug(logTag, "File not found: $file\n" +
                "Generated file name: $fileLC")
        return null
    } catch (e: IOException) {
        logError(logTag, "Read operation failed on file: $fileLC")
        e.printStackTrace()
        throw e
    }
}

/**
 * Return a map of all download languages with key set to the name of the language and
 * value set set to its deserialized data.
 */
fun getAllLanguages(context: Context): LinkedHashMap<String, Language> {
    val logTag = "LangDataReader::getAllLanguages"
    val files = ArrayList<String>()
    Collections.addAll(files, *context.fileList())
    val languageList = LinkedHashMap<String, Language>()
    if (files.size == 0) {
        logDebug(logTag, "Could not find any downloaded files.")
        return languageList
    }
    val mapper = jacksonObjectMapper()
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    for (file in files) {
        try {
            logDebug(logTag, "Found file: $file")
            if (!file.endsWith(".json"))
                continue
            val language: Language = readValue(file, context)
            language.language = file
                .lowercase()
                .replace(".json", "")
                .upperCaseFirstLetter()
            languageList[language.language] = language
        } catch (e: JacksonException) {
            logError(logTag, "Invalid json file detected: $file")
            logError(logTag, "Trying next file")
        } catch (e: IOException) {
            logError(logTag, "Read operation failed on file: $file")
            e.printStackTrace()
        }
    }
    return languageList
}

/**
 * Find downloaded languages. Return list of such languages with
 * ".json" extension removed and first letter capitalised. Returns empty
 *  array list if no files found.
 */
fun getDownloadedLanguages(context: Context): ArrayList<String> {
    val logTag = "LangDataReader::getDownloadedLanguages"
    logDebug(logTag, "Finding all available lang data files")
    return arrayListOf<String>().apply { addAll(getAllLanguages(context).keys) }
}
