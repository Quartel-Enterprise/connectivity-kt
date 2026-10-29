package software.quare.connectivity

import kotlinx.coroutines.flow.Flow

/**
 * Watches whether the device can reach the internet.
 *
 * Get the platform implementation from Koin after loading [connectivityModule]. Being a `fun interface`, it is also
 * easy to fake in tests: `NetworkConnectivityObserver { flowOf(true) }`.
 *
 * What counts as connected depends on the platform:
 * - Android: the active network has internet access and the system validated it, so a captive portal or a Wi-Fi
 *   without internet counts as disconnected.
 * - iOS and macOS: the `NWPathMonitor` path is satisfied.
 * - JVM: some network interface other than loopback is up, checked every 3 seconds. A local network without
 *   internet counts as connected.
 * - Linux: some network interface other than loopback has the operstate "up", checked every 3 seconds. A local
 *   network without internet counts as connected, and so does a system whose `/sys/class/net` can't be read.
 * - JS and Wasm/JS: the browser's `navigator.onLine`, checked every 3 seconds.
 */
public fun interface NetworkConnectivityObserver {
    /**
     * Returns a cold flow of the connectivity: `true` while the device is connected, `false` otherwise.
     *
     * Each collection emits the current state first and then every change, never the same value twice in a row. It
     * never completes, so collect it in a scope that ends, or take only what you need, as [IsConnected] does with
     * the first value.
     */
    public fun observe(): Flow<Boolean>
}
