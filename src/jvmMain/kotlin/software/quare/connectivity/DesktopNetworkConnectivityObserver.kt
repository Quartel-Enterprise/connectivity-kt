package software.quare.connectivity

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.NetworkInterface
import java.util.Collections
import kotlin.time.Duration

/*
 * Why: the JDK has no push for connectivity changes, so this polls for an up, non-loopback interface.
 * It only says whether trying the network is worth it, so a cheap check every few seconds is enough.
 */
internal class DesktopNetworkConnectivityObserver(
    private val pollInterval: Duration,
) : NetworkConnectivityObserver {
    override fun observe(): Flow<Boolean> = flow {
        var previous: Boolean? = null
        while (true) {
            val online = isOnline()
            if (online != previous) {
                emit(online)
                previous = online
            }
            delay(pollInterval)
        }
    }

    private fun isOnline(): Boolean = runCatching {
        Collections
            .list(NetworkInterface.getNetworkInterfaces())
            .any { it.isUp && !it.isLoopback }
    }.getOrDefault(true)
}
