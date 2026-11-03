package software.quare.connectivity

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * The Koin module that provides [NetworkConnectivityObserver] (a single instance for the app) and [IsConnected].
 *
 * Load it in `startKoin { modules(connectivityModule) }`. On Android, Koin must also know the application context,
 * through `androidContext(application)`, and the library's manifest adds the `ACCESS_NETWORK_STATE` permission to the
 * app. Without Koin, use `createNetworkConnectivityObserver()` and [createIsConnected] instead.
 */
public val connectivityModule: Module = module {
    includes(platformConnectivityModule)
    factory { createIsConnected(get()) }
}

internal expect val platformConnectivityModule: Module
