@file:OptIn(ExperimentalForeignApi::class)

package software.quare.connectivity

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import platform.posix.closedir
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen
import platform.posix.opendir
import platform.posix.readdir

private const val NET_DIRECTORY = "/sys/class/net"
private const val BUFFER_SIZE = 32

/*
 * Why: /sys/class/net/<interface>/operstate is where the kernel publishes each interface's state
 * (up, down, unknown...). Null when the directory can't be opened. Entries without an operstate, such as
 * the bonding_masters file, are left out.
 */
internal fun readSysClassNet(directory: String = NET_DIRECTORY): Map<String, String>? {
    val handle = opendir(directory) ?: return null
    val states = mutableMapOf<String, String>()
    try {
        while (true) {
            val entry = readdir(handle) ?: break
            val name = entry.pointed.d_name.toKString()
            if (name.startsWith('.')) continue
            readFirstLine("$directory/$name/operstate")?.let { state -> states[name] = state }
        }
    } finally {
        closedir(handle)
    }
    return states
}

private fun readFirstLine(path: String): String? {
    val file = fopen(path, "r") ?: return null
    return try {
        memScoped {
            val buffer = allocArray<ByteVar>(BUFFER_SIZE)
            fgets(buffer, BUFFER_SIZE, file)?.toKString()?.trim()
        }
    } finally {
        fclose(file)
    }
}
