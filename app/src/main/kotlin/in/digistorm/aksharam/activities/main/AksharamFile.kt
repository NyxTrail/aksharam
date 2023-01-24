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

package `in`.digistorm.aksharam.activities.main

import `in`.digistorm.aksharam.activities.main.util.LanguageFile

/**
 * Hold information about a file.
 * For files online: LanguageFile holds the name and download url
 * For local files: AksharamFile holds the file name
 * isDownloaded
 */
data class AksharamFile (
    val onlineLanguageFile: LanguageFile? = null,
    val localFileName: String? = null,
    var isDownloaded: Boolean,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AksharamFile

        if (onlineLanguageFile != other.onlineLanguageFile) return false
        if (localFileName != other.localFileName) return false
        if (isDownloaded != other.isDownloaded) return false

        return true
    }

    override fun hashCode(): Int {
        var result = onlineLanguageFile?.hashCode() ?: 0
        result = 31 * result + (localFileName?.hashCode() ?: 0)
        result = 31 * result + isDownloaded.hashCode()
        return result
    }

    fun getLanguage(): String? {
        return onlineLanguageFile?.name ?: localFileName
    }

    fun displayName(): String? {
        return (localFileName ?: onlineLanguageFile?.name)?.removeSuffix(".json")?.replaceFirstChar {
            it.uppercase()
        }
    }
}