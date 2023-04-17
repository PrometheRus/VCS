import java.io.File
import java.lang.StringBuilder
import java.security.MessageDigest
import java.util.Scanner

fun main(args: Array<String>) {
    VersionControlSystem(args).runSystem()
}

class VersionControlSystem(private val args: Array<String>) {
    private val vcsDir = File("${System.getProperty("user.dir")}/vcs")
    private val commitsDir = File("$vcsDir/commits")
    init {
        if (!vcsDir.exists())       vcsDir.mkdir()
        if (!commitsDir.exists())   commitsDir.mkdir()
    }

    private val configFile = vcsDir.resolve("config.txt")
    private val indexFile = vcsDir.resolve("index.txt")
    private val logFile = vcsDir.resolve("log.txt")
    init {
        if (!configFile.exists())     { configFile.writeText("Default name") }
    }

    fun runSystem(){

        if (args.isEmpty()) {
            helpCommand()
        } else {
            when(args[0]) {
                "--help" ->     helpCommand()
                "config" ->     configCommand(args)
                "add" ->        addCommand(args)
                "reset" ->      reset(args)
                "commit" ->     commitCommand(args)
                "log" ->        logCommand()
                "checkout" ->   checkoutCommand(args)
                "delete" ->     deleteVCSDir()
                else -> println("'${args[0]}' is not a SVCS command.")
            }
        }
    }

    private fun helpCommand() {
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

        when (args.size) {
            1 -> {
                println(output)
            }
            else -> {
                println("Wrong input. Max one argument is allowed")
            }
        }
    }

    /**
     * config allows the user to set their own name or output an already existing name. If a user wants to set a new name, the program overwrites the old one.
     */
    private fun configCommand(args: Array<String>) {
        when (args.size) {
            2 -> {
                val newUserName = args[1]
                configFile.writeText(newUserName, charset = Charsets.UTF_8)
                println("The username is $newUserName.")
            }
            1 -> {
                println("The username is ${getUserName()}.")
            }
            else -> {
                println("Wrong input. Max two arguments are allowed")
            }
        }
    }

    /**
     * add allows the user to set the name of a file that they want to track or output the names of tracked files. If the file does not exist, the program informs a user that the file does not exist.
     */
    private fun addCommand(args: Array<String>) {
        if (args.size == 2) {

            val fileName = args[1]

            if (File(fileName).exists()) {

                if (!indexFile.exists()) {
                    indexFile.writeText(fileName)
                } else if (indexFile.readLines().isEmpty()){
                    indexFile.appendText(fileName)
                } else {
                    indexFile.appendText("\n$fileName")
                }

                println("The file '$fileName' is tracked.")

            } else {
                println("Can't find '$fileName'.")
            }

        } else if (args.size == 1 && !indexFile.exists()) {
            println("There are no tracked files yet")
        } else if (args.size == 1) {
            println("Tracked files:")
            indexFile.readLines().forEach { println(it) }
        }  else {
            println("Wrong input. Max two arguments are allowed")
        }
    }


    /**
     * Undo "add" command for uncommitted changes
     */
    private fun reset(args: Array<String>) {
        if (!indexFile.exists()) {
            println("index.txt is not created yet")
            return
        }

        if (indexFile.readLines().isEmpty()) {
            println("There are no uncommitted changes")
            return
        }

        when (args.size) {
            2 -> {
                val fileToUndo = args[1]

                if (!indexFile.readLines().contains(fileToUndo)) {
                    println("$fileToUndo is not tracked file")
                    return
                }

                val listWithFiles = mutableListOf<String>()
                val scanner = Scanner(indexFile)
                while (scanner.hasNext()) {
                    listWithFiles.add(scanner.next())
                }

                listWithFiles.forEach { if (it == fileToUndo) listWithFiles.remove(it) }
                indexFile.writeText("")
                val builder = StringBuilder()
                listWithFiles.forEach { builder.append(it).append("\n") }

                indexFile.writeText(builder.trim().toString())
                println("$fileToUndo's uncommitted changes are undone")
            }
            1 -> {
                indexFile.delete()
                indexFile.createNewFile()
                println("All uncommitted changes are undone")
            }
            else -> {
                println("Wrong input. Max two arguments are allowed")
            }
        }
    }

    /**
     * commit must be passed to the program along with a message. Each commit is assigned by a unique id. if there were no changes since the last commit, the program does not create a new commit. Just copies all the staged files to the commit folder every time.
     */
    private fun commitCommand(args: Array<String>) {
        if (args.size == 1) {
            println("Message was not passed. The message is required")
            return
        }

        val listOfFilesFromIndex = File("vcs/index.txt").readLines()

        val builder = StringBuilder()
        listOfFilesFromIndex.forEach {
            builder
                .append(File(it)
                    .readLines()
                    .joinToString(separator = " "))
                .append(" ").trim()
        }

        val hashSum = toSHA(builder.toString())

        val commitDir = File("vcs/commits/$hashSum")
        if (!commitDir.exists()) {
            commitDir.mkdir()
            listOfFilesFromIndex.forEach { File(it).copyTo(File("$commitDir/$it")) }

            if (!logFile.exists()) {

                logFile.writeText("commit $hashSum\nAuthor: ${getUserName()}\n${args[1]}")

            } else {

                val b = StringBuilder()
                val scanner = Scanner(logFile)
                while (scanner.hasNext()) {
                    b.append(scanner.nextLine()).append("\n")
                }
                logFile.writeText("commit $hashSum\nAuthor: ${getUserName()}\n${args[1]}\n\n")
                logFile.appendText(b.trim().toString())
            }

            println("Changes are committed $hashSum")

        } else {
            println("Nothing to commit.")
        }
    }

    /**
     * log shows all the commits in reverse order.
     */
    private fun logCommand() {
        if (!logFile.exists()) {

            logFile.createNewFile()
            println("No commits yet.")

        } else {

            val listFromLogFile = logFile.readLines()
            listFromLogFile.forEach { println(it) }

        }
    }

    /**
     * The checkout command is passed to the program together with the commit ID to indicate which commit should be used. If a commit with the given ID exists, the contents of the tracked file is restored in accordance with this commit.
     */
    private fun checkoutCommand(args: Array<String>) {

        if (args.size == 2) {

            val commitID = args[1]
            val commitDir = File("vcs/commits/${commitID}")
            if (commitDir.exists()) {

                commitDir.list()?.forEach { File("$commitDir/$it").copyTo(target = File(it), overwrite = true) }

                println("Switched to commit $commitID.")

            } else {
                println("Commit does not exist.")
            }
        } else {
            println("Commit id was not passed.")
        }
    }

    private fun getUserName(): String {
        return configFile.readText(charset = Charsets.UTF_8)
    }

    private fun toSHA(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun deleteVCSDir() {
        if (vcsDir.exists()) {
            println("Deleting VCS directory")
            vcsDir.deleteRecursively()
            println("VCS directory is deleted")
        } else {
            println("There is no VCS directory")
        }
    }
}
