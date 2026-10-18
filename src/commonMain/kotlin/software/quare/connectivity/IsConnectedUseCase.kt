package software.quare.connectivity

import kotlinx.coroutines.flow.first

internal class IsConnectedUseCase(
    private val networkConnectivityObserver: NetworkConnectivityObserver,
) : IsConnected {
    override suspend fun invoke(): Boolean = networkConnectivityObserver.observe().first()
}
