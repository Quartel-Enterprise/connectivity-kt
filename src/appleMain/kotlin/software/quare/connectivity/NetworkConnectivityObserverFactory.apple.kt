package software.quare.connectivity

/**
 * Creates the platform's [NetworkConnectivityObserver], for apps that don't use Koin.
 *
 * [connectivityModule] provides the same observer through Koin. Nothing runs until
 * [NetworkConnectivityObserver.observe] is collected.
 */
public fun createNetworkConnectivityObserver(): NetworkConnectivityObserver = AppleNetworkConnectivityObserver()
