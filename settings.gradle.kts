plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        id("com.android.application") version "8.1.1"
        id("com.android.library") version "8.1.1"
        id("org.jetbrains.kotlin.android") version "1.9.10"
    }
}

rootProject.name = "shell"
include(":app")
