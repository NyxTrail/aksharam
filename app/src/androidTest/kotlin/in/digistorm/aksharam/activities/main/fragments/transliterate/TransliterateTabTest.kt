package `in`.digistorm.aksharam.activities.main.fragments.transliterate

import android.util.Log
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import `in`.digistorm.aksharam.AksharamTestBaseExp
import `in`.digistorm.aksharam.DELAYS
import `in`.digistorm.aksharam.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransliterateTabTest: AksharamTestBaseExp() {
    override val logTag: String = this.javaClass.simpleName

    @Before
    fun beforeTest() {
        downloadLanguageDataBeforeTest()
    }

    fun initialise() {
        Log.d(logTag, "Selecting \"Transliterate\" tab.")
        onView(withText(R.string.transliterate_tab_header)).perform(click())
    }

    @Test
    fun initialScreen() {
        runMainActivityTest {
            initialise()

            onView(withId(R.id.language_spinner)).perform(click())
            checkLanguageSpinnerDisplaysHint(R.string.transliterate_tab_info_default)
            checkInputTextFieldMatchesString("")
            checkOutputTextFieldMatchesString("")
        }
    }

    private fun checkLanguageSpinnerDisplaysHint(hint: Int) {
        onView(withId(R.id.language_spinner))
            .check(matches(
                hasDescendant(withHint(hint)),
            ))
    }

    private fun checkInputTextFieldMatchesString(stringToMatch: String) {
        onView(withId(R.id.text_input_edit_text))
            .check(matches(
                allOf(
                    withHint(R.string.transliteration_input_hint),
                    withText(stringToMatch))
                )
            )
    }

    private fun checkOutputTextFieldMatchesString(stringToMatch: String) {
        onView(withId(R.id.output_text_view))
            .check(matches(
                allOf(
                    withHint(R.string.transliteration),
                    withText(stringToMatch)
                )
            ))

    }

    private fun checkCouldNotDetectLanguageHintDisplayed() {
        onView(withId(R.id.language_spinner))
            .check(matches(
                hasDescendant(withHint(R.string.could_not_detect_language)),
            ))
    }

    private fun inputText(text: String) {
        // Simulate typing by replacing the string character by character with
        // a small delay before a new character is input into the edit text.
        for(i in 1 .. text.length) {
            onView(withId(R.id.text_input_edit_text))
                .perform(replaceText(text.substring(0, i)))
            DELAYS.TYPING_DELAY.waitVeryShortDuration()
        }
        DELAYS.UI_WAIT_TIME.waitLongDuration()
    }

    private fun clearText() {
        onView(withId(R.id.text_input_edit_text))
            .perform(replaceText(""))
        DELAYS.UI_WAIT_TIME.waitShortDuration()
    }

    private fun checkTransliterationMatches(string: String) {
        onView(withId(R.id.output_text_view)).check(matches(withText(string)))
    }

    private fun chooseTransliterationLanguage(language: String): ViewInteraction {
        onView(withId(R.id.language_spinner)).perform(click())
        return onData(`is`(language)).inRoot(RootMatchers.isPlatformPopup()).perform(click())
    }

    @Test
    fun inputLanguageIsUndetectable() {
        runMainActivityTest {
            initialise()

            val testCases = listOf(
                "A sentence in English.",
                "1 + 2 = 5",
                "         "
            )

            for (testCase in testCases) {
                inputText(testCase)
                checkInputTextFieldMatchesString(testCase)
                checkOutputTextFieldMatchesString("")
                checkCouldNotDetectLanguageHintDisplayed()
            }
        }
    }

    @Test
    fun checkKannadaInput() {
        runMainActivityTest {
            initialise()

            val testCases = listOf(
                "ಕನ್ನಡ ಲಿಪಿ 10-12ನೆಯ ಶತಮಾನದಲ್ಲಿ ಕಲ್ಯಾಣಿ ಚಾಳುಕ್ಯರ ಶಾಸನಗಳಲ್ಲಿ ವಿಶೇಷ ಬದಲಾವಣೆಯನ್ನು ಹೊಂದಿ ಒಂದು ಮುಖ್ಯವಾದ ಹಂತವನ್ನು ಸೂಚಿಸುತ್ತಿವೆ.",
                "ಭಾರತದಲ್ಲಿನ ಟ್ವೆಂಟಿ 20 ಕ್ರಿಕೆಟ್ ಪಂದ್ಯಾವಳಿಗಳ ಒಂದು ವೃತ್ತಿಪರ ಲೀಗ್ ಆಗಿದೆ.",
                "ಯ ರ ಲ ವ ಳ 1 2 3",
                "ಕ ಖ ಗ ಘ ಙ അ ആ",
                "ಕ ಖ ಗ ಘ ಙ And more text in English",
            )
            val transliterationSets = listOf(
                listOf(
                    "Malayalam" to "കന്നഡ ലിപി 10-12നെയ ശതമാനദല്ലി കല്യാണി ചാളുക്യര ശാസനഗളല്ലി വിശേഷ ബദലാവണെയന്നു ഹൊംദി ഒംദു മുഖ്യവാദ ഹംതവന്നു സൂചിസുത്തിവെ.",
                    "Hindi" to "कन्नड लिपि 10-12नॆय शतमानदल्लि कल्याणि चाळुक्यर शासनगळल्लि विशेष बदलावणॆयन्नु हॊंदि ऒंदु मुख्यवाद हंतवन्नु सूचिसुत्तिवॆ."
                ),
                listOf(
                    "Malayalam" to "ഭാരതദല്ലിന ട്വെംടി 20 ക്രികെട് പംദ്യാവളിഗള ഒംദു വൃത്തിപര ലീഗ് ആഗിദെ.",
                    "Hindi" to "भारतदल्लिन ट्वॆंटि 20 क्रिकॆट् पंद्यावळिगळ ऒंदु वृत्तिपर लीग् आगिदॆ."
                ),
                listOf(
                    "Malayalam" to "യ ര ല വ ള 1 2 3",
                    "Hindi" to "य र ल व ळ 1 2 3"
                ),
                listOf(
                    "Malayalam" to "ക ഖ ഗ ഘ ങ അ ആ",
                    "Hindi" to "क ख ग घ ङ അ ആ"
                ),
            )

            for ((i, testCase) in testCases.withIndex()) {
                checkLanguageSpinnerDisplaysHint(R.string.transliterate_tab_info_default)
                inputText(testCase)
                checkLanguageSpinnerDisplaysHint(R.string.letters_tab_trans_hint)
                checkTransliterationMatches(testCase)
                transliterationSets.getOrNull(i)?.forEach {
                    chooseTransliterationLanguage(it.first)
                    checkTransliterationMatches(it.second)
                }
                clearText()
            }
        }
    }

    @Test
    fun checkMalayalamInput() {
        runMainActivityTest {
            initialise()

            val testCases = listOf(
                "ഇന്ത്യൻ നഗരമായ മുംബൈയെ അതിൻ്റെ ഉപഗ്രഹ നഗരമായ നവി മുംബൈയുമായി ബന്ധിപ്പിക്കുന്ന 21.8 കിലോമീറ്റർ (13.5 മൈൽ) നീളമുള്ള ഒരു പാലമാണ് ഗ്രേറ്റ് മുംബൈ ട്രാൻസ് ഹാർബർ ലിങ്ക്.",
                "വെസ്റ്റ് ഇൻഡീസിലേക്കുള്ള തന്റെ നാലാമത്തേയും അവസാനത്തേതുമായ യാത്രക്ക് ക്രിസ്റ്റഫർ കൊളംബസ് തുടക്കം കുറിച്ചു",
                "യ ര ല വ ള 1 2 3",
                "ക ഖ ഗ ഘ ങ अ आ",
                "ക ഖ ഗ ഘ ങ And more text in English",
            )
            val transliterationSets = listOf(
                listOf(
                    "Kannada" to "ಇನ್ತ್ಯನ್ ನಗರಮಾಯ ಮುಂಬೈಯೆ ಅತಿನ್್ಱೆ ಉಪಗ್ರಹ ನಗರಮಾಯ ನವಿ ಮುಂಬೈಯುಮಾಯಿ ಬನ್ಧಿಪ್ಪಿಕ್ಕುನ್ನ 21.8 ಕಿಲೋಮೀಱ್ಱರ್ (13.5 ಮೈಲ್) ನೀಳಮುಳ್ಳ ಒರು ಪಾಲಮಾಣ್ ಗ್ರೇಱ್ಱ್ ಮುಂಬೈ ಟ್ರಾನ್ಸ್ ಹಾರ್ಬರ್ ಲಿಙ್ಕ್.",
                    "Hindi" to "इन्त्यन् नगरमाय मुंबैयॆ अतिन््ऱॆ उपग्रह नगरमाय नवि मुंबैयुमायि बन्धिप्पिक्कुन्न 21.8 किलोमीऱ्ऱर् (13.5 मैल्) नीळमुळ्ळ ऒरु पालमाण् ग्रेऱ्ऱ् मुंबै ट्रान्स् हार्बर् लिङ्क्."
                ),
                listOf(
                    "Kannada" to "ವೆಸ್ಱ್ಱ್ ಇನ್ಡೀಸಿಲೇಕ್ಕುಳ್ಳ ತನ್ಱೆ ನಾಲಾಮತ್ತೇಯುಂ ಅವಸಾನತ್ತೇತುಮಾಯ ಯಾತ್ರಕ್ಕ್ ಕ್ರಿಸ್ಱ್ಱಫರ್ ಕೊಳಂಬಸ್ ತುಟಕ್ಕಂ ಕುಱಿಚ್ಚು",
                    "Hindi" to "वॆस्ऱ्ऱ् इन्डीसिलेक्कुळ्ळ तन्ऱॆ नालामत्तेयुं अवसानत्तेतुमाय यात्रक्क् क्रिस्ऱ्ऱफर् कॊळंबस् तुटक्कं कुऱिच्चु"
                ),
                listOf(
                    "Kannada" to "ಯ ರ ಲ ವ ಳ 1 2 3",
                    "Hindi" to "य र ल व ळ 1 2 3",
                ),
                listOf(
                    "Kannada" to "ಕ ಖ ಗ ಘ ಙ अ आ",
                    "Hindi" to "क ख ग घ ङ अ आ",
                ),
            )

            for ((i, testCase) in testCases.withIndex()) {
                checkLanguageSpinnerDisplaysHint(R.string.transliterate_tab_info_default)
                inputText(testCase)
                checkLanguageSpinnerDisplaysHint(R.string.letters_tab_trans_hint)
                checkTransliterationMatches(testCase)
                transliterationSets.getOrNull(i)?.forEach {
                    chooseTransliterationLanguage(it.first)
                    checkTransliterationMatches(it.second)
                }
                clearText()
            }
        }
    }

    @Test
    fun checkHindiInput() {
        runMainActivityTest {
            initialise()

            val testCases = listOf(
                "बुलबुल, शाखाशायी गण के पिकनोनॉटिडी कुल (Pycnonotidae) का पक्षी है।",
                "\"सज्जनों का खेल\" क्रिकेट एक बल्ले और गेंद का दलीय खेल है जो १०० से अधिक देशों में खेला जाता है।",
                "य र ल व न 1 2 3",
                "क ख ग घ ङ ಅ ಆ",
                "क ख ग घ ङ And more text in English",
            )
            val transliterationSets = listOf(
                listOf(
                    "Kannada" to "ಬುಲಬುಲ, ಶಾಖಾಶಾಯೀ ಗಣ ಕೇ ಪಿಕನೊನೋಟಿಡೀ ಕುಲ (Pycnonotidae) ಕಾ ಪಕ್ಷೀ ಹೈ।",
                    "Malayalam" to "ബുലബുല, ശാഖാശായീ ഗണ കേ പികനൊനോടിഡീ കുല (Pycnonotidae) കാ പക്ഷീ ഹൈ।"
                ),
                listOf(
                    "Kannada" to "\"ಸಜ್ಜನೊಂ ಕಾ ಖೇಲ\" ಕ್ರಿಕೇಟ ಏಕ ಬಲ್ಲೇ ಔರ ಗೇಂದ ಕಾ ದಲೀಯ ಖೇಲ ಹೈ ಜೊ १०० ಸೇ ಅಧಿಕ ದೇಶೊಂ ಮೇಂ ಖೇಲಾ ಜಾತಾ ಹೈ।",
                    "Malayalam" to "\"സജ്ജനൊം കാ ഖേല\" ക്രികേട ഏക ബല്ലേ ഔര ഗേംദ കാ ദലീയ ഖേല ഹൈ ജൊ १०० സേ അധിക ദേശൊം മേം ഖേലാ ജാതാ ഹൈ।"
                ),
                listOf(
                    "Kannada" to "ಯ ರ ಲ ವ ನ 1 2 3",
                    "Malayalam" to "യ ര ല വ ന 1 2 3"
                ),
                listOf(
                    "Kannada" to "ಕ ಖ ಗ ಘ ಙ ಅ ಆ",
                    "Malayalam" to "ക ഖ ഗ ഘ ങ ಅ ಆ"
                ),
            )

            for ((i, testCase) in testCases.withIndex()) {
                checkLanguageSpinnerDisplaysHint(R.string.transliterate_tab_info_default)
                inputText(testCase)
                checkLanguageSpinnerDisplaysHint(R.string.letters_tab_trans_hint)
                checkTransliterationMatches(testCase)
                transliterationSets.getOrNull(i)?.forEach {
                    chooseTransliterationLanguage(it.first)
                    checkTransliterationMatches(it.second)
                }
                clearText()
            }
        }
    }
}