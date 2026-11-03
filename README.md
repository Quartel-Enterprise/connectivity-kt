# Connectivity

A small Kotlin Multiplatform library that tells your app whether the device is online, and lets it react when that
changes. Use it to pause sync while offline, show an "offline" banner, or retry a request when the connection comes
back. It has no UI and doesn't depend on Compose. Its implementations are provided through a [Koin](https://insert-koin.io)
module (see [Without Koin](#without-koin) if your app doesn't use Koin).

```kotlin
class Synchronizer(
    private val networkConnectivityObserver: NetworkConnectivityObserver,
) {
    suspend fun pushWhenOnline() {
        networkConnectivityObserver.observe()
            .filter { isConnected -> isConnected }
            .collect { push() }
    }
}
```

"Online" means something slightly different on each platform. Only Android checks that the internet is actually
reachable; see [What counts as connected](#what-counts-as-connected).

## Platforms

| Platform | Targets | Minimum |
| --- | --- | --- |
| Android | `android` | minSdk 23 |
| JVM (desktop, server) | `jvm` | Java 11 |
| iOS | `iosArm64`, `iosSimulatorArm64` | |
| macOS | `macosArm64` | |
| Linux | `linuxX64`, `linuxArm64` | |
| Browser | `js`, `wasmJs` | |

There are no Intel Apple targets (`iosX64`, `macosX64`), no watchOS, tvOS or Windows. The `js` and `wasmJs` artifacts
also declare Node.js, but Node has no `navigator.onLine`, so the observer gives no meaningful answer there.

## Installation

The library is on Maven Central, so `mavenCentral()` in your repositories is enough.

```kotlin
dependencies {
    implementation("software.quare:connectivity:1.1.0")
}
```

Or with a version catalog:

```toml
[versions]
quare-connectivity = "1.1.0"

[libraries]
quare-connectivity = { module = "software.quare:connectivity", version.ref = "quare-connectivity" }
```

```kotlin
implementation(libs.quare.connectivity)
```

In a multiplatform project, add it to `commonMain`. It brings `koin-core` 4.2.2 and `kotlinx-coroutines-core` 1.11.0
with it as API dependencies, so your app gets at least those versions. It is built with Kotlin 2.4.21.

On Android there is nothing to add to your manifest: the library's manifest declares the `ACCESS_NETWORK_STATE`
permission and the build merges it into your app. It is a normal permission, so there is no runtime prompt.

## Setup

Load `connectivityModule` when starting Koin:

```kotlin
import software.quare.connectivity.connectivityModule

startKoin {
    androidContext(application) // Android only
    modules(connectivityModule)
}
```

On Android the module needs the application `Context` from Koin. `androidContext()` comes from
`io.insert-koin:koin-android`, which this library does not bring; add it to your Android app if you don't have it yet.

### Without Koin

The platform implementations are internal, so you still get them through `connectivityModule`, but you don't have to
use Koin in the rest of your app. Build a private Koin container once, without `startKoin`, and keep the instances you
need:

```kotlin
import org.koin.dsl.koinApplication
import software.quare.connectivity.IsConnected
import software.quare.connectivity.NetworkConnectivityObserver
import software.quare.connectivity.connectivityModule

private val connectivity = koinApplication { modules(connectivityModule) }.koin

val networkConnectivityObserver: NetworkConnectivityObserver = connectivity.get()
val isConnected: IsConnected = connectivity.get()
```

On Android, give that container the application context yourself, which avoids needing `koin-android`:

```kotlin
import android.content.Context
import org.koin.dsl.koinApplication
import org.koin.dsl.module

val connectivity = koinApplication {
    modules(
        module { single<Context> { applicationContext } },
        connectivityModule,
    )
}.koin
```

## Usage

The module provides two things, both in the `software.quare.connectivity` package:

```kotlin
val observer: NetworkConnectivityObserver = get()
observer.observe() // Flow<Boolean>: the current state first, then every change

val isConnected: IsConnected = get()
if (isConnected()) fetch() // one-off check; suspends until the platform reports the current state
```

`NetworkConnectivityObserver` is a single instance; `IsConnected` is created on each request and reads the first value
of `observe()`.

### In a ViewModel and in Compose

`observe()` returns a cold flow that never completes. Collect it in a scope that ends, or turn it into a `StateFlow`:

```kotlin
class SyncViewModel(
    networkConnectivityObserver: NetworkConnectivityObserver,
) : ViewModel() {
    val isOnline: StateFlow<Boolean> = networkConnectivityObserver
        .observe()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true, // assume online until the platform answers, so an offline banner doesn't flash
        )
}

@Composable
fun SyncScreen(viewModel: SyncViewModel) {
    val isOnline by viewModel.isOnline.collectAsState() // or collectAsStateWithLifecycle()
    if (!isOnline) OfflineBanner()
}
```

### Lifecycle and threading

- Nothing runs until you collect. Each collection starts its own listener (an Android network callback, an
  `NWPathMonitor`, or a polling loop) and stops it when the collecting coroutine is cancelled.
- Several collectors means several listeners. To share one, use `stateIn` or `shareIn` as above.
- Values are delivered in the collector's coroutine context. The polling loops (JVM, Linux, browser) also run there.
- The flow never emits the same value twice in a row.

### What counts as connected

| Platform | `true` when | Updates |
| --- | --- | --- |
| Android | A network has internet access and Android validated it (its own connectivity check passed). A captive portal or a Wi-Fi without internet counts as disconnected | Pushed by `ConnectivityManager` |
| iOS, macOS | The `NWPathMonitor` path is satisfied: there is a usable network route. It doesn't check that the internet is reachable | Pushed by the Network framework |
| JVM | Some network interface other than loopback is up. Virtual interfaces (VPN, Docker, VMs) count, and so does a local network without internet. If the interfaces can't be read, it counts as connected | Checked every 3 seconds |
| Linux | Some network interface other than loopback has the `operstate` "up" in `/sys/class/net`. A local network without internet counts. If that directory can't be read, as in some sandboxes, it counts as connected | Checked every 3 seconds |
| Browser (JS, Wasm/JS) | `navigator.onLine` is true, which the browser reports for any network, even one without internet | Checked every 3 seconds |

If you need to know that your own server answers, make the request and handle the failure; this library only tells
you when trying is pointless or when it is worth retrying.

## Testing

There is no separate testing artifact. Both types are `fun interface`s, so a fake takes one line:

```kotlin
val alwaysOnline = NetworkConnectivityObserver { flowOf(true) }
val offline = IsConnected { false }
```

To drive changes from a test, back the fake with a `MutableStateFlow`:

```kotlin
val online = MutableStateFlow(true)
val observer = NetworkConnectivityObserver { online }

online.value = false // collectors now see false
```

## Development

```bash
./gradlew allTests       # every target (Apple targets need macOS, the Linux tests a Linux host)
./scripts/ktlint.sh      # ktlint with github.com/Quartel-Enterprise/ktlint-rules (--format to fix)
./gradlew publishToMavenLocal  # try a change in an app before releasing it
```

Building needs the Android SDK: set `ANDROID_HOME` or `sdk.dir` in `local.properties`.

## Releasing

1. Bump `version` in `build.gradle.kts` and merge it to `main`.
2. Publish a GitHub release whose tag is that version (`1.2.3`). The `Publish` workflow checks the tag, runs the
   tests and releases the signed artifacts to Maven Central, where they show up a few minutes later.

## License

[Apache License 2.0](LICENSE)
