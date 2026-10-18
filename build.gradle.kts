import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.maven.publish)
}

group = "software.quare"
version = "1.0.0"

kotlin {
    explicitApi()

    android {
        namespace = "software.quare.connectivity"
        compileSdk = libs.versions.android.compileSdk
            .get()
            .toInt()
        minSdk = libs.versions.android.minSdk
            .get()
            .toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    js {
        browser()
        nodejs()
    }
    wasmJs {
        browser()
        nodejs()
    }
    iosArm64()
    iosSimulatorArm64()
    macosArm64()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.androidx.annotation)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    coordinates(
        groupId = group.toString(),
        artifactId = "connectivity",
        version = version.toString(),
    )

    pom {
        name = "Connectivity"
        description = "A Kotlin Multiplatform observer of whether the device can reach the internet, wired with Koin."
        inceptionYear = "2026"
        url = "https://github.com/Quartel-Enterprise/connectivity-kt"

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }

        developers {
            developer {
                id = "Quartel-Enterprise"
                name = "Quartel Enterprise"
                email = "quare.software@gmail.com"
            }
        }

        scm {
            connection = "scm:git:github.com/Quartel-Enterprise/connectivity-kt.git"
            developerConnection = "scm:git:ssh://github.com/Quartel-Enterprise/connectivity-kt.git"
            url = "https://github.com/Quartel-Enterprise/connectivity-kt/tree/main"
        }
    }
}
