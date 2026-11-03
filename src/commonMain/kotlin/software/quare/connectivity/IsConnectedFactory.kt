package software.quare.connectivity

/**
 * Creates an [IsConnected] that reads the first value of [observer], for apps that don't use Koin.
 *
 * [connectivityModule] provides the same thing through Koin. Get the [observer] from
 * `createNetworkConnectivityObserver()`, which takes the application `Context` on Android.
 */
public fun createIsConnected(observer: NetworkConnectivityObserver): IsConnected = IsConnectedUseCase(observer)
