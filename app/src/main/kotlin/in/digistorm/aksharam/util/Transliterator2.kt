package `in`.digistorm.aksharam.util

import java.lang.StringBuilder

private const val logTag: String = "Transliterator2"

// TODO: Rename file to Transliterator (remove suffix '2') after re-implementing all dependencies
// Transliterate the input string using the mapping and return the transliterated string
// str is the string that needs to be converted
// targetLanguage is the language to which the string needs to be converted
fun transliterate(str: String, targetLanguage: String, languageData: Language): String {
    val targetLanguageLC = targetLanguage.lowercase()
    val targetLangCode = languageData.getLanguageCode(targetLanguageLC)
    logDebug(
        logTag, "Transliterating \"" + str
                + "\" (" + languageData.language + ") to " + targetLanguageLC
                + "(code: " + targetLangCode + ")"
    )
    var out = StringBuilder()
    var character: String

    // Process the string character by character
    for (ch in str.toCharArray()) {
        character = "" + ch // convert to string
        out =
            if (languageData.letterDefinitions.containsKey(character))
                if (languageData.getLetterDefinition(character)?.transliterationHints!!.containsKey(targetLangCode))
                    out.append(
                        languageData.getLetterDefinition(character)!!.transliterationHints!![targetLangCode]!![0])
                else {
                    logDebug(logTag, "Could not find transliteration hints for character: \""
                            + character + "\" of language: " + languageData.language
                            + "for transliteration to language: " + targetLanguageLC)
                    out.append(character)
                } else {
                logDebug(
                    logTag, "Could not find letter definition for letter: \""
                            + character + "\" in language: " + languageData.language
                )
                out.append(character)
            }
    }
    logDebug(logTag, "Constructed string: \"$out\"")
    return out.toString()
}
