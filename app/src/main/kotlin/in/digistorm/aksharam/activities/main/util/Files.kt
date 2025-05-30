/*
 * Copyright (c) 2022-2025 Alan M Varghese <alan@digistorm.in>
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

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.nio.charset.StandardCharsets

private const val logTag = "Files"

suspend fun writeTofile(
    fileName: String,
    content: String,
    context: Context
) {
    withContext(Dispatchers.IO) {
        logDebug(logTag, "Writing $fileName to ${context.filesDir}...")
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(content.toByteArray(StandardCharsets.UTF_8))
        }
    }
}

suspend fun downloadFile(file: LanguageFile, context: Context) {
    logDebug(logTag, "Downloading file: ${file.name} from: ${file.download_url}")
    val fileContents = downloadFile(file.download_url)
    writeTofile(file.name, fileContents!!, context)
}

suspend fun getLocalFiles(context: Context): List<String> {
    return withContext(Dispatchers.IO) {
        logDebug(logTag, "Finding all available lang data files")
        // If filesDir is null, add an empty array
        val list = context.filesDir.list()?.filter { it.endsWith(".json") } ?: listOf<String>()
        logDebug(logTag, "Local files found: $list")
        return@withContext list
    }
}

suspend fun deleteFile(fileName: String, context: Context) {
    withContext(Dispatchers.IO) {
        context.deleteFile(fileName)
    }
}

fun getBufferedReader(file: String, context: Context): BufferedReader {
    return context.openFileInput(file).bufferedReader()
}

