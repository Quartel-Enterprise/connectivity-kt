package software.quare.connectivity

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

/*
 * Why: Linux has no push for connectivity changes that a plain process can use without D-Bus, so this
 * polls the kernel's view of the interfaces: one other than loopback with the operstate "up".
 * [readInterfaceStates] is a parameter so tests need no network.
 */
internal class LinuxNetworkConnectivityObserver(
    private val pollInterval: Duration,
    private val readInterfaceStates: () -> Map<String, String>? = ::readSysClassNet,
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

    // Why: a sandbox without /sys can't tell, and refusing to try would keep the app offline for good.
    private fun isOnline(): Boolean {
        val states = readInterfaceStates() ?: return true
        return states.any { (name, state) -> name != LOOPBACK && state == UP }
    }

    private companion object {
        const val LOOPBACK = "lo"
        const val UP = "up"
    }
}
