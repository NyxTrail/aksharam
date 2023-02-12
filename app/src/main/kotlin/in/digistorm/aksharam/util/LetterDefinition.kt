/**
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

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonAnySetter
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.ArrayList
import kotlin.collections.LinkedHashMap

class LetterDefinition(
    val type: String = "",

    val examples: LinkedHashMap<String, Map<String, String>> = LinkedHashMap(),
    val info: LinkedHashMap<String, String> = LinkedHashMap(),

    var isVirama: Boolean = false,

    var base: String = "",

    @JsonProperty("combine_after")
    val combineAfter: Boolean? = null,

    @JsonProperty("combine_before")
    val combineBefore: Boolean? = null,

    @JsonProperty("exclude_combi_examples")
    val excludeCombiExamples: Boolean? = null,

    // "ml": ['അ'], "hi":['अ']...
    var transliterationHints: MutableMap<String, ArrayList<String>>? = null,
) {

    /**
     * Jackson uses this method to serialise anything that we haven't explicitly defined.
     * We use this to assign transliteration hints correctly.
     * "ml": ['അ'], "hi":['अ']...
     */
    @JsonAnySetter
    fun setLetterDefinitions(key: String, list: ArrayList<String>) {
        if (transliterationHints == null)
            transliterationHints = LinkedHashMap()
        transliterationHints!![key] = list
    }

    fun shouldCombineAfter(): Boolean {
        return combineAfter ?: false
    }

    fun shouldCombineBefore(): Boolean {
        return combineBefore ?: false
    }

    fun shouldExcludeCombiExamples(): Boolean {
        return excludeCombiExamples ?: false
    }
}