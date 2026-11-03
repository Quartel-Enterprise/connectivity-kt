package software.quare.connectivity

import android.content.Context
import android.net.ConnectivityManager

/**
 * Creates the Android [NetworkConnectivityObserver], for apps that don't use Koin.
 *
 * [connectivityModule] provides the same observer through Koin. The system service is taken from the
 * application context, so passing an `Activity` doesn't leak it. Nothing runs until
 * [NetworkConnectivityObserver.observe] is collected. The app needs the `ACCESS_NETWORK_STATE` permission, which the library's manifest adds.
 */
public fun createNetworkConnectivityObserver(context: Context): NetworkConnectivityObserver =
    AndroidNetworkConnectivityObserver(
        connectivityManager = context.applicationContext.getConnectivityManager(),
    )

internal fun Context.getConnectivityManager(): ConnectivityManager =
    getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
