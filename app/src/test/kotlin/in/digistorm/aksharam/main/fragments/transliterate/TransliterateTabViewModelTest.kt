package `in`.digistorm.aksharam.main.fragments.transliterate

import `in`.digistorm.aksharam.activities.main.fragments.transliterate.TransliterateTabViewModel
import `in`.digistorm.aksharam.util.waitAndGetValue
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import `in`.digistorm.aksharam.util.AksharamTestBase
import kotlinx.coroutines.*
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream

@RunWith(AndroidJUnit4::class)
class TransliterateTabViewModelTest: AksharamTestBase() {

    // This rule is useful if we need to postValue from tests.
    @get:Rule
    val rule = InstantTaskExecutorRule()

    companion object {
        /* Clean up files after test completion. */
        @AfterClass
        @JvmStatic
        fun deleteFiles() {
            val destPath = ApplicationProvider.getApplicationContext<Context>().filesDir.toPath()
            val fileList: Stream<Path> = Files.list(destPath)
            for(file in fileList) {
                Files.delete(file)
            }
        }
    }

    @Test
    fun `is view model instantiation correct`() {
        val viewModel = TransliterateTabViewModel(
            ApplicationProvider.getApplicationContext()
        )

        assertNull(viewModel.currentInput.value)
        assertNull(viewModel.detectedLanguage.value)
        assertNull(viewModel.language.value)
        assertNull(viewModel.selectableLanguages.value)
        assertNull(viewModel.targetLanguageSelected.value)
        assertNull(viewModel.transliteratedString.value)
        println(viewModel.javaClass.simpleName + " instantiates correctly.")
    }

    @Test
    fun `is view model initialisation correct`() {
        val viewModel = initialiseViewModel()

        assertNull(viewModel.currentInput.value)
        println(ApplicationProvider.getApplicationContext<Context?>().filesDir.toString())
        println(viewModel.javaClass.simpleName + " initialises correctly.")
    }

    @Test
    fun `is kannada detected`() {
        val viewModel = initialiseViewModel()

        viewModel.currentInput.postValue("ಕನ್ನಡ ಲಿಪಿ")
        assertEquals("Kannada", viewModel.detectedLanguage.waitAndGetValue())
    }

    @Test
    fun `is malayalam detected`() {
        val viewModel = initialiseViewModel()

        viewModel.currentInput.postValue("മലയാളം ലിപി")
        assertEquals("Malayalam", viewModel.detectedLanguage.waitAndGetValue())
    }

    @Test
    fun `is hindi detected`() {
        val viewModel = initialiseViewModel()

        viewModel.currentInput.postValue("हिन्दी लिपि")
        assertEquals("Hindi", viewModel.detectedLanguage.waitAndGetValue())
    }

    @Test
    fun `languages hindi can be transliterated to`() {
        val viewModel = initialiseViewModel()

        viewModel.currentInput.postValue("हिन्दी लिपि")
        val selectableLanguages = viewModel.selectableLanguages.waitAndGetValue()
        files.filter {
            it != "Hindi"
        }.forEach { file ->
            if(file !in selectableLanguages)
                fail("Expected: $files Actual: $selectableLanguages")
        }
    }

    @Test
    fun `languages kannada can be transliterated to`() {
        val viewModel = initialiseViewModel()

        viewModel.currentInput.postValue("ಕನ್ನಡ ಲಿಪಿ")
        val selectableLanguages = viewModel.selectableLanguages.waitAndGetValue()
        files.filter {
            it != "Kannada"
        }.forEach { file ->
            if(file !in selectableLanguages)
                fail("Expected: $files Actual: $selectableLanguages")
        }
    }

    @Test
    fun `languages malayalam can be transliterated to`() {
        val viewModel = initialiseViewModel()

        viewModel.currentInput.postValue("മലയാളം ലിപി")
        val selectableLanguages = viewModel.selectableLanguages.waitAndGetValue()
        files.filter {
            it != "Malayalam"
        }.forEach { file ->
            if(file !in selectableLanguages)
                fail("Expected: $files Actual: $selectableLanguages")
        }
    }

    private fun initialiseViewModel(): TransliterateTabViewModel {
        return TransliterateTabViewModel(
            ApplicationProvider.getApplicationContext()
        ).apply { initialise() }
    }
}