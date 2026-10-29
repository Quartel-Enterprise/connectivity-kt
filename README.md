# Connectivity

A Kotlin Multiplatform observer of whether the device can reach the internet, ready to use with Koin.

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

Platforms: Android, JVM, JS, Wasm/JS, iOS, macOS and Linux (x64 and ARM64).

## Installation

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

In a multiplatform project, add it to `commonMain`. It brings `koin-core` and `kotlinx-coroutines-core` with it.

## Setup

Load `connectivityModule` when starting Koin:

```kotlin
startKoin {
    androidContext(application) // Android only
    modules(connectivityModule)
}
```

On Android the module reads the application context from Koin, and the library's manifest adds the
`ACCESS_NETWORK_STATE` permission to the app.

## Usage

The module provides two things:

```kotlin
val observer: NetworkConnectivityObserver = get()
observer.observe() // Flow<Boolean>: the current state first, then every change

val isConnected: IsConnected = get()
if (isConnected()) fetch() // one-off check
```

Both are `fun interface`s, so faking them in tests takes one line:

```kotlin
val alwaysOnline = NetworkConnectivityObserver { flowOf(true) }
val offline = IsConnected { false }
```

### What counts as connected

| Platform | Connected when | Updates |
| --- | --- | --- |
| Android | The active network has internet access and the system validated it (a captive portal doesn't count) | Pushed by `ConnectivityManager` |
| iOS, macOS | The `NWPathMonitor` path is satisfied | Pushed by the Network framework |
| JVM | A network interface other than loopback is up (a local network without internet counts) | Checked every 3 seconds |
| Linux | A network interface other than loopback has the `operstate` "up" in `/sys/class/net` (a local network without internet counts). If that directory can't be read, as in some sandboxes, it counts as connected | Checked every 3 seconds |
| JS, Wasm/JS | The browser's `navigator.onLine` is true | Checked every 3 seconds |

## Development

```bash
./gradlew allTests       # every target (Apple targets need macOS, the Linux tests a Linux host)
./scripts/ktlint.sh      # lint with the Quare ruleset (--format to fix)
./gradlew publishToMavenLocal  # try a change in an app before releasing it
```

Building needs the Android SDK: set `ANDROID_HOME` or `sdk.dir` in `local.properties`.

## Releasing

1. Bump `version` in `build.gradle.kts` and merge it to `main`.
2. Publish a GitHub release whose tag is that version (`1.2.3`). The `Publish` workflow checks the tag, runs the
   tests and releases the signed artifacts to Maven Central, where they show up a few minutes later.

## License

[Apache License 2.0](LICENSE)
