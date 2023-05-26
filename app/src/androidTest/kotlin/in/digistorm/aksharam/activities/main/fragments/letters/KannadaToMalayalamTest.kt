package `in`.digistorm.aksharam.activities.main.fragments.letters

import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.junit.Test
import `in`.digistorm.aksharam.R
import org.junit.Before

class KannadaToMalayalamTest: LettersTabTest() {
    override val logTag: String = javaClass.simpleName
    private val categories = listOf("Vowels", "Consonants", "Signs", "Ligatures")

    override fun initialise() {
        super.initialise()
        Log.d(logTag, "Selecting Kannada as the source language.")
        chooseLanguage("Kannada")

        chooseTransliterationLanguage("Malayalam")
    }

    // Exposed so sibling classes can run these tests easily.
    private fun singleClickTest() {
        initialise()

        // Vowels
        scrollToCardAtPosition(position = 0)
        checkLetter("ಅ", "അ")
        checkLetter("ಅಂ", "അം")
        checkLetter("ಅಃ", "അഃ")

        // Consonants
        scrollToCardAtPosition(position = 1)
        checkLetter("ಕ", "ക")
        checkLetter("ಳ", "ള")

        // Signs
        scrollToCardAtPosition(position = 2)
        checkLetter("್", "്")
        checkLetter("ೃ", "ൃ")

        // Ligatures
        scrollToCardAtPosition(position = 3)
        checkLetter("ಕ್ತ", "ക്ത")
        checkLetter("ರ್", "ര്")
    }

    @Test
    fun singleClickTransliterations() {
        runMainActivityTest {
            singleClickTest()
        }
    }

    @Test
    fun singleClickTestAfterCollapsionExpansion() {
        runMainActivityTest {
            initialise()

            clickCardCategory(0, categories[0]) // Collapse Vowels
            clickCardCategory(2, categories[2]) // Collapse Signs
            clickCardCategory(2, categories[2]) // Expand Signs
            clickCardCategory(0, categories[0]) // Expand Vowels

            checkLetter("ಇ", "ഇ")
            scrollToCardAtPosition(1)
            checkLetter("ನ", "ന")
            scrollToCardAtPosition(2)
            checkLetter("ೀ", "ീ")
            // Single click after collapsion/expansion
            // Collapse Everything
            clickCardCategory(0, categories[0])
            clickCardCategory(1, categories[1])
            clickCardCategory(2, categories[2])
            clickCardCategory(3, categories[3])

            clickCardCategory(1, categories[1]) // Expand consonants
            checkLetter("ಙ", "ങ")
            checkLetter("ಲ", "ല")

            clickCardCategory(1, categories[1]) // Collapse consonants
            clickCardCategory(3, categories[3]) // Expand ligatures
            checkLetter("ನ್ಯ", "ന്യ")
            checkLetter("ತ್ಸ", "ത്സ")

            chooseTransliterationLanguage("Hindi")
            // Expand everything
            clickCardCategory(0, categories[0])
            clickCardCategory(1, categories[1])
            clickCardCategory(2, categories[2])

            chooseTransliterationLanguage("Malayalam")
            singleClickTest()
        }
    }

    @Test
    fun longClickInfo() {
        runMainActivityTest {
            initialise()

            // Check vowels
            scrollToCardAtPosition(0)
            longClickLetter("ಅ")
            onView(withId(R.id.heading)).check(matches(withText("ಅ")))
            onView(withId(R.id.transliterated_heading)).check(matches(withText("അ")))
            checkLetterInfoHeadingAlignment()
            checkWordAndMeaningDisplayed("ಅರ್ಧ", "അര്ധ", "അർദ്ധം, പകുതി")
            onView(withId(R.id.letter_info_container)).perform(pressBack())

            // Check vowel signs
            scrollToCardAtPosition(2)
            longClickLetter("ಾ")
            checkLetterInfoHeading("ಾ", "ാ")
            checkLetterInfoHeadingAlignment()
            checkWordAndMeaningHeadingHidden()
            checkInfoDisplayed("ಆ")
            checkDiacriticHint("ಾ with consonants and ligatures:")
            checkCombineSignWithConsonants("ಾ", listOf("ಕ", "ಬ", "ಕ್ತ"))
            onView(withId(R.id.letter_info_container)).perform(pressBack())

            // Check consonants
            scrollToCardAtPosition(1)
            longClickLetter("ದ")
            checkLetterInfoHeading("ದ", "ദ")
            checkLetterInfoHeadingAlignment()
            checkWordAndMeaningDisplayed("ದಕ್ಷಿಣ", "ദക്ഷിണ", "തെക്ക്, ദക്ഷിണ ദിക്ക്")
            checkInfoHidden()
            checkCombineConsonantWithVowelSigns("ದ", listOf("ೊ", "ಾ", "ೈ", "ೀ"))
            checkConsonantAsPrefix("ದ", "್", listOf("ಕ", "ರ", "ದ", "ಖ", "ವ"))
            checkConsonantAsSuffix("ದ", "್", listOf("ರ", "ಙ", "ಯ", "ಮ", "ದ"))
            onView(withId(R.id.letter_info_container)).perform(pressBack())

            // Check ligatures
            scrollToCardAtPosition(3)
            longClickLetter("ಕ್ತ")
            checkLetterInfoHeading("ಕ್ತ", "ക്ത")
            checkLetterInfoHeadingAlignment()
            checkWordAndMeaningDisplayed("ರಕ್ತ", "രക്ത", "രക്തം")
            checkInfoDisplayed("Examples")
            checkCombineConsonantWithVowelSigns("ಕ್ತ", listOf("ೆ", "ೇ", "ೃ", "ೀ", "ೌ"))
        }
    }
}
