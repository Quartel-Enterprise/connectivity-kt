package software.quare.connectivity

import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds

internal actual val platformConnectivityModule: Module = module {
    single<NetworkConnectivityObserver> { WebNetworkConnectivityObserver(pollInterval = 3.seconds) }
}
