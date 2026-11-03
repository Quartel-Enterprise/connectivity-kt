package software.quare.connectivity

import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformConnectivityModule: Module = module {
    /*
     * Why: get<Context>() resolves what koin-android's androidContext() registers, without pulling
     * koin-android and its AndroidX dependencies into the library.
     */
    single { createNetworkConnectivityObserver(get<Context>()) }
    // Why: 1.1.0 registered the ConnectivityManager too, and apps may resolve it from this module.
    single { get<Context>().getConnectivityManager() }
}
