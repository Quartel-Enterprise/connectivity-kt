package software.quare.connectivity

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

internal class LinuxNetworkConnectivityObserverTest {
    private val pollInterval = 3.seconds

    @Test
    fun `GIVEN only the loopback is up WHEN observing THEN it is offline`() = runTest {
        // Given
        val observer = observerOf(mapOf("lo" to "unknown"))

        // When
        val isConnected = observer.observe().first()

        // Then
        assertEquals(false, isConnected)
    }

    @Test
    fun `GIVEN an interface other than the loopback is up WHEN observing THEN it is online`() = runTest {
        // Given
        val observer = observerOf(mapOf("lo" to "unknown", "wlp3s0" to "up"))

        // When
        val isConnected = observer.observe().first()

        // Then
        assertEquals(true, isConnected)
    }

    @Test
    fun `GIVEN the only interface is down WHEN observing THEN it is offline`() = runTest {
        // Given
        val observer = observerOf(mapOf("lo" to "unknown", "eth0" to "down"))

        // When
        val isConnected = observer.observe().first()

        // Then
        assertEquals(false, isConnected)
    }

    @Test
    fun `GIVEN the interfaces cannot be read WHEN observing THEN it is assumed online`() = runTest {
        // Given
        val observer = LinuxNetworkConnectivityObserver(pollInterval) { null }

        // When
        val isConnected = observer.observe().first()

        // Then
        assertEquals(true, isConnected)
    }

    @Test
    fun `GIVEN a stable network WHEN polling several times THEN emits the state only once`() = runTest {
        // Given
        val states = mutableListOf<Boolean>()
        val observer = observerOf(mapOf("eth0" to "up"))
        backgroundScope.launch { observer.observe().collect(states::add) }

        // When
        advanceTimeBy(pollInterval * 3)

        // Then
        assertEquals(listOf(true), states)
    }

    @Test
    fun `GIVEN the connection drops and returns WHEN polling THEN emits every change`() = runTest {
        // Given
        var interfaces = mapOf("eth0" to "up")
        val states = mutableListOf<Boolean>()
        val observer = LinuxNetworkConnectivityObserver(pollInterval) { interfaces }
        backgroundScope.launch { observer.observe().collect(states::add) }
        advanceTimeBy(pollInterval)

        // When
        interfaces = mapOf("eth0" to "down")
        advanceTimeBy(pollInterval)
        interfaces = mapOf("eth0" to "up")
        advanceTimeBy(pollInterval)

        // Then
        assertEquals(listOf(true, false, true), states)
    }

    private fun observerOf(interfaces: Map<String, String>): LinuxNetworkConnectivityObserver =
        LinuxNetworkConnectivityObserver(pollInterval) { interfaces }
}
