import java.io.File
import java.lang.StringBuilder
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.Scanner

fun main(args: Array<String>) {
    VersionControlSystem(args).runSystem()
}

class VersionControlSystem(private val args: Array<String>) {
    private val myDir = File("${System.getProperty("user.dir")}/vcs")
    private val commitsDir = File("$myDir/commits")
    init {
        if (!myDir.exists())            myDir.mkdir()
        if (!commitsDir.exists())       commitsDir.mkdir()
    }

    private val myConfigFile = myDir.resolve("config.txt")
    private val myIndexFile = myDir.resolve("index.txt")
    private val logFile = myDir.resolve("log.txt")
    init {
        if (!myConfigFile.exists())     {
            myConfigFile.createNewFile()
            myConfigFile.writeText("Username")
        }
        if (!myIndexFile.exists())      myIndexFile.createNewFile()
        if (!logFile.exists())          logFile.createNewFile()
    }

    fun runSystem(){

        if (args.isEmpty()) {
            helpCommand()
        } else {
            when(args[0]) {
                "--help" ->     helpCommand()
                "config" ->     configCommand(args)
                "add" ->        addCommand(args)
                "commit" ->     commitCommand(args)
                "log" ->        logCommand()
                "checkout" ->   checkoutCommand(args)
                else -> println("'${args[0]}' is not a SVCS command.")
            }
        }
    }

    private fun helpCommand() {
        println("""
            These are SVCS commands:
            config                  Get a username.
            config {username}       Set a username.
            add                     Get list of tracked files.
            add {filename}          Add a file to the index.txt.
            log                     Show all the commits (commitID, author, comment) in reverse order.
            commit {comment}        Save changes.
            checkout {commitID}     Restore a file.
            """.trimIndent())
    }

    /**
     * config should allow the user to set their own name or output an already existing name. If a user wants to set a new name, the program must overwrite the old one.
     */
    private fun configCommand(args: Array<String>) {
        when (args.size) {
            2 -> {
                val newUserName = args[1]
                myConfigFile.writeText(newUserName, charset = Charsets.UTF_8)
                println("The username is $newUserName.")
            }
            1 -> {
                println("The username is ${getUserName()}.")
            }
            else -> {
                println("Something is wrong")
            }
        }
    }

    /**
     * add should allow the user to set the name of a file that they want to track or output the names of tracked files. If the file does not exist, the program should inform a user that the file does not exist.
     */
    private fun addCommand(args: Array<String>) {
        if (args.size == 2) {

            val fileName = args[1]

            if (File(fileName).exists()) {

                if (myIndexFile.readLines().isEmpty()) {
                    myIndexFile.appendText(fileName)
                } else {
                    myIndexFile.appendText("\n$fileName")
                }

                println("The file '$fileName' is tracked.")

            } else {
                println("Can't find '$fileName'.")
            }
        } else if (args.size == 1 && myIndexFile.readLines().isEmpty()) {
            println("Add a filename to the input.")
        } else if (args.size == 1) {
            println("Tracked files:")
            myIndexFile.readLines().forEach { println(it) }
        }  else {
            println("Something wrong")
        }
    }

    /**
     * commit must be passed to the program along with a message. Save all changes. Each commit must be assigned a unique id. if there were no changes since the last commit, do not create a new commit. You don't need to optimize the storage of changes, just copy all the staged files to the commit folder every time.
     */
    private fun commitCommand(args: Array<String>) {
        if (args.size == 1) {
            println("Message was not passed.")
            return
        }

        val listOfFilesFromIndex = File("vcs/index.txt").readLines()

        val builder = StringBuilder()
        listOfFilesFromIndex.forEach { builder.append(File(it)
            .readLines()
            .joinToString(separator = " ")).append(" ").trim()
        }

        val hashSum = toSHA(builder.toString())

        val commitDir = File("vcs/commits/$hashSum")
        if (!commitDir.exists()) {
            commitDir.mkdir()
            listOfFilesFromIndex.forEach { File(it).copyTo(File("vcs/commits/${hashSum}/$it")) }

            if (logFile.readLines().isEmpty()) {

                logFile.writeText("commit $hashSum\nAuthor: ${getUserName()}\n${args[1]}")

            } else {
                val b = StringBuilder()
                val scanner = Scanner(logFile)
                while (scanner.hasNext()) { b.append(scanner.nextLine()).append("\n") }
                logFile.writeText("commit $hashSum\nAuthor: ${getUserName()}\n${args[1]}\n\n")
                logFile.appendText(b.trim().toString())

            }

            println("Changes are committed $hashSum")
        } else {
            println("Nothing to commit.")
        }
    }

    /**
     * log should show all the commits in reverse order.
     */
    private fun logCommand() {
        if (logFile.readLines().isEmpty()) {
            println("No commits yet.")
        } else {
            val listFromLogFile = logFile.readLines()
            listFromLogFile.forEach { println(it) }
        }
    }

    /**
     * The checkout command must be passed to the program together with the commit ID to indicate which commit should be used. If a commit with the given ID exists, the contents of the tracked file should be restored in accordance with this commit.
     */
    private fun checkoutCommand(args: Array<String>) {

        if (args.size == 2) {

            val commitID = args[1]
            val commitDir = File("vcs/commits/${commitID}")
            if (commitDir.exists()) {

                val listOfFilesFromCommitDirByCommitID = File("vcs/index.txt").readLines()
                listOfFilesFromCommitDirByCommitID.forEach {
                    File("${File(System.getProperty("user.dir"))}/vcs/commits/$commitID/$it")
                        .copyTo(target = File(it), overwrite = true)
                }
                println("Switched to commit $commitID.")

            } else {
                println("Commit does not exist.")
            }
        } else {
            println("Commit id was not passed.")
        }
    }

    private fun getUserName(): String {
        return myConfigFile.readText(charset = Charsets.UTF_8)
    }

    private fun toSHA(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun deleteFiles() {
        val configPath = "/Users/nikitavalkovki/JetBrains/IntelliJ/KotlinProject/config.txt"
        File(configPath).delete()
        val indexPath = "/Users/nikitavalkovki/JetBrains/IntelliJ/KotlinProject/index.txt"
        File(indexPath).delete()
    }
}
