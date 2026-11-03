package software.quare.connectivity

import android.Manifest
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission

/*
 * Why: any network counts, not only the active one, because ConnectivityCallback counts any network too;
 * with the active network alone the first value could be false and flip to true as soon as the callbacks
 * arrive. allNetworks is deprecated for callbacks, but a callback reports nothing when there is no
 * network, so the first value can't wait for one.
 */
@Suppress("DEPRECATION")
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
internal fun ConnectivityManager.isCurrentlyConnected(): Boolean = allNetworks.any { network ->
    getNetworkCapabilities(network)?.hasInternet() == true
}

/*
 * Why: VALIDATED is required so a connected-but-no-internet link such as a captive
 * portal does not count.
 */
internal fun NetworkCapabilities.hasInternet(): Boolean = hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
    hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
