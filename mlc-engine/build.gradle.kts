plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.pocketpalai.mlcengine"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
        targetSdk = 34
    }
    sourceSets["main"].jniLibs.srcDirs("libs")
}

// Repositories are defined in settings.gradle.kts; no project repositories here

dependencies {
    // No external dependencies needed for the stub.
}
