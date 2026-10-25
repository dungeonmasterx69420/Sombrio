enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "sombrio"

include(":app:phone")
include(":app:tv")
include(":core")
include(":data")
include(":player:core")
include(":player:local")
include(":setup")
include(":modes:film")
include(":settings")

// The Music tab: sombrAMP, built as a Flutter module by music/build-module.sh. Without it, Sombrio builds without
// music, unless -Psombrio.music=required asks for it (as the release builds do).
val musicRepository = file("music/module/build/host/outputs/repo")
if (musicRepository.resolve("wtf/sombra/sombramp/flutter_release").isDirectory) {
    include(":music")
} else if (providers.gradleProperty("sombrio.music").orNull == "required") {
    throw GradleException("The Music tab isn't built: run music/build-module.sh first")
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}