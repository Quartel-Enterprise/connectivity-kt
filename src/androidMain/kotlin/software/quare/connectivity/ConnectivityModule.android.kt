package software.quare.connectivity

import android.content.Context
import android.net.ConnectivityManager
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformConnectivityModule: Module = module {
    /*
     * Why: get<Context>() resolves what koin-android's androidContext() registers, without pulling
     * koin-android and its AndroidX dependencies into the library.
     */
    single { get<Context>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    singleOf(::AndroidNetworkConnectivityObserver).bind<NetworkConnectivityObserver>()
}
