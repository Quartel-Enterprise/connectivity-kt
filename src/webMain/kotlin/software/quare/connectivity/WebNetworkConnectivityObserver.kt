package software.quare.connectivity

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

// Why: [isOnline] is a parameter so tests can change the answer between polls.
internal class WebNetworkConnectivityObserver(
    private val pollInterval: Duration,
    private val isOnline: () -> Boolean = ::isNavigatorOnline,
) : NetworkConnectivityObserver {
    override fun observe(): Flow<Boolean> = flow {
        while (true) {
            emit(isOnline())
            delay(pollInterval)
        }
    }.distinctUntilChanged()
}

@OptIn(ExperimentalWasmJsInterop::class)
internal fun isNavigatorOnline(): Boolean = isOnLine(findNavigator())

/*
 * Why: Node.js before 21 has no navigator at all and Node 21+ has one without onLine, so a missing answer
 * counts as connected, as on the other platforms that can't tell, instead of keeping the app offline for good.
 * The parameter looks unused to ktlint because only the js() code reads it.
 */
@Suppress("ktlint:quare:unused-function-parameter")
@OptIn(ExperimentalWasmJsInterop::class)
internal fun isOnLine(navigator: JsAny?): Boolean =
    js("navigator == null || typeof navigator.onLine !== 'boolean' || navigator.onLine")

@OptIn(ExperimentalWasmJsInterop::class)
private fun findNavigator(): JsAny? = js("typeof navigator === 'undefined' ? null : navigator")
