package software.quare.connectivity

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformConnectivityModule: Module = module {
    singleOf(::AppleNetworkConnectivityObserver).bind<NetworkConnectivityObserver>()
}
