pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.essential.gg/public/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        maven("https://repo.spongepowered.org/maven/")
        //maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

gradle.settingsEvaluated {
    val gradleCacheDir = File(System.getProperty("user.home"), ".gradle/caches/essential-loom")
    gradleCacheDir.asFileTree.matching {
        exclude("mojang_versions_manifest.json")
    }
}

rootProject.name = "DarkAddons"
