package `in`.digistorm.aksharam.activities.main.fragments.letters

import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.junit.Before
import org.junit.Test
import `in`.digistorm.aksharam.R

class MalayalamToKannadaTest: LettersTabTest() {
    override val logTag: String = javaClass.simpleName
    private val categories = listOf("Vowels", "Consonants", "Signs", "Chillu", "Ligatures")

    @Before
    override fun initialise() {
        super.initialise()
        Log.d(logTag, "Selecting Malayalam as the source language.")
        chooseLanguage("Malayalam")

        chooseTransliterationLanguage("Kannada")
    }

    @Test
    fun singleClickTransliterations() {
        // Vowels
        scrollToCardAtPosition(position = 0)
        checkLetter("ഊ", "ಊ")
        checkLetter("ഋ", "ಋ")
        checkLetter("ഔ", "ಔ")
        checkLetter("അഃ", "ಅಃ")

        // Consonants
        scrollToCardAtPosition(position = 1)
        checkLetter("ണ", "ಣ")
        checkLetter("ഘ", "ಘ")
        checkLetter("റ", "ಱ")
        checkLetter("ള", "ಳ")
        checkLetter("ഡ", "ಡ")

        // Signs
        scrollToCardAtPosition(position = 2)
        checkLetter("ി", "ಿ")
        checkLetter("ം", "ಂ")
        checkLetter("്", "್")
        checkLetter("ൃ", "ೃ")


        // Chillu
        scrollToCardAtPosition(position = 3)
        checkLetter("ൻ", "ನ್")
        checkLetter("ൺ", "ಣ್")
        checkLetter("ൾ", "ಳ್")
        checkLetter("ൽ", "ಲ್")
        checkLetter("ർ", "ರ್")

        // Ligatures
        scrollToCardAtPosition(position = 4)
        checkLetter("ക്ഷ", "ಕ್ಷ")
        checkLetter("പ്പ", "ಪ್ಪ")
        checkLetter("ങ്ക", "ಙ್ಕ")
        checkLetter("ക്വ", "ಕ್ವ")
        checkLetter("മ്മ", "ಮ್ಮ")
    }

    @Test
    fun longClickInfo() {
        // Check vowels
        scrollToCardAtPosition(0)
        longClickLetter("ഇ")
        onView(withId(R.id.heading)).check(matches(withText("ഇ")))
        onView(withId(R.id.transliterated_heading)).check(matches(withText("ಇ")))
        checkLetterInfoHeadingAlignment()
        checkWordAndMeaningDisplayed("ഇല", "ಇಲ", "ಎಲೆ")
        onView(withId(R.id.letter_info_container)).perform(pressBack())

        // Check vowel signs
        scrollToCardAtPosition(2)
        longClickLetter("ു")
        checkLetterInfoHeading("ു", "ು")
        checkLetterInfoHeadingAlignment()
        checkWordAndMeaningHeadingHidden()
        checkInfoDisplayed("ഉ")
        checkDiacriticHint("ു with consonants and ligatures")
        checkCombineSignWithConsonants("ു", listOf("ന", "റ്റ", "ഞ", "ന്ത", "ഗ"))
        onView(withId(R.id.letter_info_container)).perform(pressBack())

        // Check consonants
        scrollToCardAtPosition(1)
        longClickLetter("ധ")
        checkLetterInfoHeading("ധ", "ಧ")
        checkLetterInfoHeadingAlignment()
        checkWordAndMeaningDisplayed("ധനം", "ಧನಂ", "ಧನ")
        checkInfoHidden()
        checkCombineConsonantWithVowelSigns("ധ", listOf("ാ", "ൈ", "ീ"))
        checkLigaturesWithLetterAsPrefixHidden()
        checkLigaturesWithLetterAsSuffixHidden()
        onView(withId(R.id.letter_info_container)).perform(pressBack())

        // Check ligatures
        scrollToCardAtPosition(4)
        longClickLetter("ത്ത")
        checkLetterInfoHeading("ത്ത", "ತ್ತ")
        checkLetterInfoHeadingAlignment()
        checkWordAndMeaningDisplayed("തത്ത", "ತತ್ತ", "ಗಿಳಿ")
        checkInfoDisplayed("ത്ത = ത് + ത")
        checkCombineConsonantWithVowelSigns("ത്ത", listOf("ോ", "െ", "്", "ൃ"))
        checkLigaturesWithLetterAsPrefixHidden()
        checkLigaturesWithLetterAsSuffixHidden()
        onView(withId(R.id.letter_info_container)).perform(pressBack())

        // Check chillu
        scrollToCardAtPosition(3)
        longClickLetter("ൾ")
        checkLetterInfoHeading("ൾ", "ಳ್")
        checkLetterInfoHeadingAlignment()
        checkWordAndMeaningDisplayed("ഇവൾ", "ಇವಳ್", "ಇವಳು")
        checkInfoDisplayed("ൾ is the chillu for the pure consonant ള്")
    }

    @Test
    fun singleClickTestAfterCollapsionExpansion() {
        clickCardCategory(0, categories[0]) // Collapse Vowels
        clickCardCategory(2, categories[2]) // Collapse Signs
        clickCardCategory(3, categories[3]) // Collapse Chillu
        clickCardCategory(2, categories[2]) // Expand Signs
        clickCardCategory(0, categories[0]) // Expand Vowels

        // Check a Vowel
        scrollToCardAtPosition(0)
        checkLetter("ഊ", "ಊ")
        // Check a consonant
        scrollToCardAtPosition(1)
        checkLetter("ബ", "ಬ")
        // Check a vowel sign
        scrollToCardAtPosition(2)
        checkLetter("േ", "ೇ")
        // Check a chillu
        scrollToCardAtPosition(3)
        checkLetterHidden("ൺ")

        // Collapse Everything
        clickCardCategory(0, categories[0])
        clickCardCategory(1, categories[1])
        clickCardCategory(2, categories[2])
        clickCardCategory(4, categories[4])

        clickCardCategory(1, categories[1]) // Expand consonants
        checkLetter("ഖ", "ಖ")
        checkLetter("ഴ", "ಳ")

        clickCardCategory(1, categories[1]) // Collapse consonants
        clickCardCategory(3, categories[3]) // Expand chillu
        checkLetter("ൺ", "ಣ್")
        checkLetter("ൽ", "ಲ್")

        chooseTransliterationLanguage("Hindi")
        // Expand everything
        clickCardCategory(0, categories[0])
        clickCardCategory(1, categories[1])
        clickCardCategory(2, categories[2])
        clickCardCategory(4, categories[4])
        MalayalamToHindiTest().singleClickTransliterations()
        chooseTransliterationLanguage("Kannada")
        singleClickTransliterations()
    }
}
