package `in`.digistorm.aksharam.activities.main.language

data class TransliteratedLetters(
    // Language the letters are transliterated from
    val sourceLanguage: String,
    // Language the letters are transliterated to
    val targetLanguage: String,
    // Categories of letters: Vowels, Consonants, Signs, Ligatures, etc
    val categories: ArrayList<Category>
)

data class Category(
    // Name of the category: Vowels, Consonants, Signs, Ligatures, etc
    val name: String,
    val letterPairs: ArrayList<Pair<String, String>>
)