pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net")
        maven("https://maven.minecraftforge.net/")
        maven("https://repo.essential.gg/public/")
        maven("https://jitpack.io") {
            mavenContent {
                includeGroupAndSubgroups("com.github")
            }
        }
        //maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "DarkAddons"
