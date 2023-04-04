package `in`.digistorm.aksharam.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import `in`.digistorm.aksharam.activities.main.helpers.upperCaseFirstLetter
import `in`.digistorm.aksharam.activities.main.util.writeTofile
import kotlinx.coroutines.runBlocking
import org.junit.Before
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.Path
import kotlin.io.path.name

open class AksharamTestBase {

    protected var files = arrayListOf<String>()

    @Before
    fun copyDataFiles() {
        val sourcePath = Path(
            System.getProperty("user.dir")!!
                    + "/../../aksharam-data/"
        )
        val destinationFolder = ApplicationProvider.getApplicationContext<Context>().filesDir.toPath()
        val fileList: Stream<Path> = Files.list(sourcePath).filter { file ->
            file.name.endsWith(".json")
        }
        for (file in fileList) {
            files.add(file.name.replace(".json", "").upperCaseFirstLetter())
            val destinationFilePath = Path(destinationFolder.toString(), file.name)
            if (Files.notExists(destinationFilePath)) {
                Files.copy(file, destinationFilePath)
            }
        }
        // throw Exception("Temporary End!")
    }

    /**
     * Create some files at app's storage location. These files should not be picked up by
     * the app.
     */
    @Before
    fun createNonsenseFiles() {
        val destinationFolder = ApplicationProvider.getApplicationContext<Context>().filesDir.toPath()
        val textFile = Path(destinationFolder.toString(), "TextFile.txt")
        val fileWithoutExtension = Path(destinationFolder.toString(), "FileWithoutExtension")
        val invalidJson = Path(destinationFolder.toString(), "InvalidJson.json")
        val randomJson = Path(destinationFolder.toString(), "RandomJson.json")
        Files.createFile(textFile)
        Files.createFile(fileWithoutExtension)
        Files.createFile(invalidJson)
        Files.createFile(randomJson)
        runBlocking {
            writeTofile(
                fileName = randomJson.name,
                content = "{\"name\": \"aksharam\"}",
                context = ApplicationProvider.getApplicationContext()
            )
        }
    }
}