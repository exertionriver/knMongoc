package libmongoc

import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import platform.posix.fgets
import platform.posix.pclose
import platform.posix.popen

object Uuid {

    //https://stackoverflow.com/questions/57123836/kotlin-native-execute-command-and-get-the-output
    fun getNewUuid() : String {
        val command = "cat /proc/sys/kernel/random/uuid" //get UUID from system
        val fp = popen(command, "r") ?: throw Exception("failed to get new UUID")

        val stdout = buildString {
            val buffer = ByteArray(96)
            while (true) {
                val input = fgets(buffer.refTo(0), buffer.size, fp) ?: break
                append(input.toKString())
            }
        }
        val status = pclose(fp)
        if (status != 0) {
            throw Exception("Command `$command` failed with status $status: $stdout")
        }

        return stdout.trim()
    }
}