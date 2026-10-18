package software.quare.connectivity

import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame

internal class ConnectivityModuleTest {
    private lateinit var koinApplication: KoinApplication

    @Test
    fun `GIVEN the module is loaded WHEN resolving the observer twice THEN returns the same desktop instance`() {
        // When
        val first = koinApplication.koin.get<NetworkConnectivityObserver>()
        val second = koinApplication.koin.get<NetworkConnectivityObserver>()

        // Then
        assertIs<DesktopNetworkConnectivityObserver>(first)
        assertSame(first, second)
    }

    @Test
    fun `GIVEN the module is loaded WHEN resolving IsConnected THEN provides it`() {
        // When
        val isConnected = koinApplication.koin.getOrNull<IsConnected>()

        // Then
        assertNotNull(isConnected)
    }

    @BeforeTest
    fun setUp() {
        koinApplication = koinApplication { modules(connectivityModule) }
    }

    @AfterTest
    fun tearDown() {
        koinApplication.close()
    }
}
