package software.quare.connectivity

/**
 * Checks once whether the device is connected right now, by the same rules as [NetworkConnectivityObserver].
 *
 * Use it before a one-off request; to react to changes, collect [NetworkConnectivityObserver.observe] instead.
 * [connectivityModule] provides it.
 */
public fun interface IsConnected {
    /**
     * Returns `true` when the device is connected, `false` otherwise. Suspends only until the platform reports the
     * current state.
     */
    public suspend operator fun invoke(): Boolean
}
