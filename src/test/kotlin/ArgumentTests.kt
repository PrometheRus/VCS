import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream


class ArgumentTests {
    private val outContent = ByteArrayOutputStream()
    private val errContent = ByteArrayOutputStream()
    private val originalOut = System.out
    private val originalErr = System.err
    private val emptyInstance = VersionControlSystem(emptyArray())
    private val vcsDir = File("vcs")

    @BeforeEach
    fun setUpStreams() {
        System.setOut(PrintStream(outContent))
        System.setErr(PrintStream(errContent))
    }

    @AfterEach
    fun restoreStreams() {
        System.setOut(originalOut)
        System.setErr(originalErr)
    }

    @AfterEach
    fun deleteVCSDir() {
        if (vcsDir.exists()) {
            vcsDir.deleteRecursively()
        }
    }

    @Test
    fun runSystemWithoutArgumentsTest() {
        val output = """
            These are SVCS commands:
            config                  Get a username.
            config {username}       Set a username.
            add                     Get list of tracked files.
            add {filename}          Add a file to the index.txt.
            reset {filename}        Undo "add" command for uncommitted changes for specific file.
            reset                   Undo "add" command for all uncommitted changes
            log                     Show all the commits (commitID, author, comment) in reverse order.
            commit {comment}        Save changes.
            checkout {commitID}     Restore a file.
            delete                  Delete "VCS" directory
            """.trimIndent()
        emptyInstance.runSystem()
        assertEquals("$output\n", outContent.toString())
    }

    @Test
    fun helpOneArgTest() {
        VersionControlSystem(arrayOf("--help")).runSystem()
        val output = """
            These are SVCS commands:
            config                  Get a username.
            config {username}       Set a username.
            add                     Get list of tracked files.
            add {filename}          Add a file to the index.txt.
            reset {filename}        Undo "add" command for uncommitted changes for specific file.
            reset                   Undo "add" command for all uncommitted changes
            log                     Show all the commits (commitID, author, comment) in reverse order.
            commit {comment}        Save changes.
            checkout {commitID}     Restore a file.
            delete                  Delete "VCS" directory
            """.trimIndent()
        assertEquals("$output\n", outContent.toString())
    }

    @Test
    fun helpTwoArgsTest() {
        VersionControlSystem(arrayOf("--help", "")).runSystem()
        assertEquals("Wrong input. Max one argument is allowed\n", outContent.toString())
    }

    @Test
    fun configGetDefaultNameTest() {
        VersionControlSystem(arrayOf("config")).runSystem()
        val defaultUserName = File("vcs/config.txt").readLines()[0]
        assertEquals("The username is $defaultUserName.\n", outContent.toString())
    }

    @Test
    fun configNewNameTest() {
        VersionControlSystem(arrayOf("config", "Nikita")).runSystem()
        val newUserName = File("vcs/config.txt").readLines()[0]
        assertEquals("The username is $newUserName.\n", outContent.toString())
    }

}