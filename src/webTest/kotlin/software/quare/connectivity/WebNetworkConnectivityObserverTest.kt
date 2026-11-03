package software.quare.connectivity

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalWasmJsInterop::class)
internal class WebNetworkConnectivityObserverTest {
    private val pollInterval = 3.seconds

    @Test
    fun `GIVEN there is no navigator WHEN reading onLine THEN it is assumed online`() {
        // Given
        val navigator: JsAny? = null

        // When
        val isConnected = isOnLine(navigator)

        // Then
        assertTrue(isConnected)
    }

    @Test
    fun `GIVEN a navigator without onLine WHEN reading onLine THEN it is assumed online`() {
        // Given
        val navigator = createNavigatorWithoutOnLine()

        // When
        val isConnected = isOnLine(navigator)

        // Then
        assertTrue(isConnected)
    }

    @Test
    fun `GIVEN a navigator reporting offline WHEN reading onLine THEN it is offline`() {
        // Given
        val navigator = createNavigator(onLine = false)

        // When
        val isConnected = isOnLine(navigator)

        // Then
        assertFalse(isConnected)
    }

    @Test
    fun `GIVEN a navigator reporting online WHEN reading onLine THEN it is online`() {
        // Given
        val navigator = createNavigator(onLine = true)

        // When
        val isConnected = isOnLine(navigator)

        // Then
        assertTrue(isConnected)
    }

    @Test
    fun `GIVEN the real environment WHEN observing THEN emits without throwing`() = runTest {
        // Given
        val observer = WebNetworkConnectivityObserver(pollInterval)

        // When
        val isConnected = observer.observe().first()

        // Then
        assertEquals(isNavigatorOnline(), isConnected)
    }

    @Test
    fun `GIVEN the connection drops and returns WHEN polling THEN emits every change once`() = runTest {
        // Given
        var online = true
        val states = mutableListOf<Boolean>()
        val observer = WebNetworkConnectivityObserver(pollInterval) { online }
        backgroundScope.launch { observer.observe().collect(states::add) }
        advanceTimeBy(pollInterval * 2)

        // When
        online = false
        advanceTimeBy(pollInterval * 2)
        online = true
        advanceTimeBy(pollInterval)

        // Then
        assertEquals(listOf(true, false, true), states)
    }
}

@Suppress("ktlint:quare:unused-function-parameter")
@OptIn(ExperimentalWasmJsInterop::class)
private fun createNavigator(onLine: Boolean): JsAny = js("({ onLine: onLine })")

@OptIn(ExperimentalWasmJsInterop::class)
private fun createNavigatorWithoutOnLine(): JsAny = js("({})")
