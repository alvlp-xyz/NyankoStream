pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") // Ensure JitPack is included
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") // Ensure JitPack is included
    }
}

rootProject.name = "NyankoStream"
include(":app")
