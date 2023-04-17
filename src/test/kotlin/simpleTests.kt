import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.BeforeTest


class SimpleTests {
    private val vcsDir = File("vcs")

    @BeforeTest
    fun initClass() {
        val emptyInstance = VersionControlSystem(emptyArray())
        emptyInstance.runSystem()
    }

    @AfterEach
    fun deleteVCSDir() {
        if (vcsDir.exists()) {
            vcsDir.deleteRecursively()
        }
    }

    @Test
    fun checkIfConfigIsCreatedAndNotEmptyTest() {
        val file = File("vcs/config.txt")
        assertEquals(true, file.exists())
        assertEquals(false, file.readLines().isEmpty())
    }

    @Test
    fun checkIfCommitDirIsCreatedTest() {
        val dir = File("vcs/commits")
        assertEquals(true, dir.exists())
    }
}