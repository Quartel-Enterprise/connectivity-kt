@file:OptIn(ExperimentalForeignApi::class)

package software.quare.connectivity

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.posix.S_IRWXU
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fputs
import platform.posix.mkdir
import platform.posix.mkdtemp
import platform.posix.remove
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class SysClassNetTest {
    private val created = mutableListOf<String>()

    @Test
    fun `GIVEN interfaces with an operstate WHEN reading THEN maps each one to its state`() {
        // Given
        val directory = createDirectory()
        newInterface(
            directory,
            name = "lo",
            state = "unknown",
        )
        newInterface(
            directory,
            name = "eth0",
            state = "up\n",
        )

        // When
        val states = readSysClassNet(directory)

        // Then
        assertEquals(mapOf("lo" to "unknown", "eth0" to "up"), states)
    }

    @Test
    fun `GIVEN an entry without an operstate WHEN reading THEN leaves it out`() {
        // Given
        val directory = createDirectory()
        newInterface(
            directory,
            name = "eth0",
            state = "up",
        )
        writeFile("$directory/bonding_masters", "bond0")

        // When
        val states = readSysClassNet(directory)

        // Then
        assertEquals(mapOf("eth0" to "up"), states)
    }

    @Test
    fun `GIVEN a directory that does not exist WHEN reading THEN returns null`() {
        // Given
        val directory = "/this/directory/does/not/exist"

        // When
        val states = readSysClassNet(directory)

        // Then
        assertNull(states)
    }

    @Test
    fun `GIVEN the real system WHEN reading THEN finds the loopback`() {
        // Given
        val directory = "/sys/class/net"

        // When
        val states = readSysClassNet(directory)

        // Then
        assertNotNull(states)
        assertTrue("lo" in states)
    }

    @AfterTest
    fun tearDown() {
        // Why: remove deletes a file or an empty directory, and the newest path is always inside the older ones.
        created.asReversed().forEach(::remove)
    }

    private fun createDirectory(): String = memScoped {
        val directory = requireNotNull(mkdtemp("/tmp/connectivity-test-XXXXXX".cstr.ptr)).toKString()
        created += directory
        directory
    }

    private fun newInterface(
        directory: String,
        name: String,
        state: String,
    ) {
        val path = "$directory/$name"
        mkdir(path, S_IRWXU.toUInt())
        created += path
        writeFile("$path/operstate", state)
    }

    private fun writeFile(
        path: String,
        content: String,
    ) {
        val file = requireNotNull(fopen(path, "w"))
        fputs(content, file)
        fclose(file)
        created += path
    }
}
