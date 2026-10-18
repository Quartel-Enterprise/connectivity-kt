package software.quare.connectivity

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * The Koin module that provides [NetworkConnectivityObserver] (a single instance for the app) and [IsConnected].
 *
 * Load it in `startKoin { modules(connectivityModule) }`. On Android, Koin must also know the application context,
 * through `androidContext(application)`, and the library's manifest adds the `ACCESS_NETWORK_STATE` permission to the
 * app.
 */
public val connectivityModule: Module = module {
    includes(platformConnectivityModule)
    factoryOf(::IsConnectedUseCase).bind<IsConnected>()
}

internal expect val platformConnectivityModule: Module
