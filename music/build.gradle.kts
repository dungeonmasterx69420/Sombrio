plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
}

java { toolchain { languageVersion = Versions.JAVA } }

// The Music tab: the activity sombrAMP runs in, and what sombrAMP's Android app declares around it. sombrAMP itself
// comes from music/module, see music/build-module.sh.
android {
    namespace = "wtf.sombra.sombrio.music"
    compileSdk = Versions.COMPILE_SDK
    buildToolsVersion = Versions.BUILD_TOOLS

    defaultConfig {
        minSdk = Versions.MIN_SDK
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        named("release") { isMinifyEnabled = false }
        register("staging") { initWith(getByName("release")) }
    }
}

dependencies {
    implementation(projects.data)
    implementation(projects.settings)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.mediarouter)
    implementation(libs.androidx.room3.runtime)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.jellyfin.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)
    api(libs.sombramp)
}
