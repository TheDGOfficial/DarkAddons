import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.github.sgtsilvio.gradle.proguard.ProguardTask
import dev.architectury.pack200.java.Pack200Adapter
import net.fabricmc.loom.task.RemapJarTask
import org.apache.commons.lang3.StringUtils
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
//import org.jetbrains.kotlin.gradle.dsl.JvmTarget
//import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
//import org.jetbrains.kotlin.gradle.tasks.CompileUsingKotlinDaemon
//import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.Locale
import java.nio.file.Files
import java.nio.charset.StandardCharsets
import java.util.Properties

plugins {
    //kotlin("jvm") version "2.1.20-dev-6370"
    id("com.gradleup.shadow") version "9.0.0-beta9"
    id("gg.essential.loom") version "1.9.31"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.ben-manes.versions") version "0.52.0"
    java
    idea
    signing
    `maven-publish`
    id("io.github.sgtsilvio.gradle.proguard") version "0.8.0"
    id("com.autonomousapps.dependency-analysis") version "2.19.0"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(24)

    withSourcesJar()
}

tasks.named<UpdateDaemonJvm>("updateDaemonJvm") {
    languageVersion = JavaLanguageVersion.of(24)
}

private val versionProperties = loadVersionProperties()

fun loadVersionProperties(): Properties {
    val properties = Properties()
    FileInputStream(File("versions.properties")).use(properties::load)

    return properties
}

private val defaultVersion = "unspecified"

private val darkaddonsVersion = versionProperties["darkaddons.version"] ?: defaultVersion
private val skytilsVersion = versionProperties["skytils.version"] ?: defaultVersion

private val guavaVersion = versionProperties["guava.version"] ?: defaultVersion
private val jetbrainsAnnotationsVersion = versionProperties["jetbrains.annotations.version"] ?: defaultVersion
private val commonsLang3Version = versionProperties["commons.lang3.version"] ?: defaultVersion

private val mixinVersion = versionProperties["mixin.version"] ?: defaultVersion

version = darkaddonsVersion
group = "gg.darkaddons"

description =
    "DarkAddons is a mod focused around Hypixel Skyblock, providing Quality of Life features/enchancements, along with other general performance enchancing features."

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.essential.gg/public/")
    maven("https://repo.hypixel.net/repository/Hypixel/")
    maven("https://repo.spongepowered.org/maven/")
    //maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
    maven("https://maven.dediamondpro.dev/releases/") {
        mavenContent {
            includeGroup("dev.dediamondpro")
        }
    }
}

loom {
    silentMojangMappingsLicense()
    runConfigs {
        getByName("client") {
            property("fml.coreMods.load", "gg.darkaddons.coremod.DarkAddonsLoadingPlugin")
            property("elementa.dev", "true")
            property("elementa.debug", "true")
            property("elementa.invalid_usage", "warn")
            property("asmhelper.verbose", "true")
            property("mixin.debug.verbose", "true")
            property("mixin.debug.export", "true")
            property("mixin.dumpTargetOnFailure", "true")
            property("legacy.debugClassLoading", "true")
            property("legacy.debugClassLoadingSave", "true")
            property("legacy.debugClassLoadingFiner", "true")
            //programArgs("--tweakClass", "gg.darkaddons.DarkAddonsTweaker")
            programArgs("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
            programArgs("--mixin", "mixins.darkaddons.json")

            isIdeConfigGenerated = true
        }
        remove(getByName("server"))
    }
    forge {
        pack200Provider = Pack200Adapter()
        mixinConfig("mixins.darkaddons.json")
        accessTransformer("src/main/resources/META-INF/darkaddons_at.cfg")
    }
    mixin {
        useLegacyMixinAp = true
        defaultRefmapName = "mixins.darkaddons.refmap.json"
    }
}

private val shadowMe: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

private val shadowMeMod: Configuration by configurations.creating {
    configurations.modImplementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    compileOnly("gg.essential:loader-launchwrapper:1.2.5") // TODO make it shadowMe
    compileOnly("gg.essential:essential-1.8.9-forge:17141+gd6f4cfd3a8") { // TODO make it back implementation
        exclude(module = "asm")
        exclude(module = "asm-commons")
        exclude(module = "asm-tree")
        exclude(module = "gson")
        exclude(module = "kotlin-stdlib")
        exclude(module = "elementa-1.8.9-forge")
        exclude(module = "universalcraft-1.8.9-forge")
        exclude(module = "vigilance-1.8.9-forge")
    }

    implementation("gg.essential:vigilance:306")
    api("gg.essential:elementa:710")
    implementation("gg.essential:universalcraft-1.8.9-forge:419")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.0")

    //annotationProcessor("io.github.llamalad7:mixinextras-common:0.3.2")!! // TODO make it shadowMe

    compileOnly("net.hypixel:mod-api-forge:1.0.1.2") {
        exclude(group = "me.djtheredstoner", module = "DevAuth-forge-legacy")
    }

    compileOnly("org.spongepowered:mixin:$mixinVersion")
    annotationProcessor("org.spongepowered:mixin:$mixinVersion:processor")

    compileOnly("com.google.guava:guava:$guavaVersion") // TODO make it back shadowMe

    compileOnly("org.apache.commons:commons-lang3:$commonsLang3Version") // TODO make it back shadowMe

    compileOnly("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:6.0.0-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter-api:6.0.0-SNAPSHOT")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.0-SNAPSHOT")

    compileOnly("gg.skytils:skytilsmod:$skytilsVersion") {
        exclude(module = "kotlin-stdlib")
    }

    proguardClasspath("com.guardsquare:proguard-base:7.7.0") {
        exclude(module = "proguard-core")
    }
    proguardClasspath("com.guardsquare:proguard-core:9.1.10")

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    annotationProcessor("com.pkware.jabel:jabel-javac-plugin:1.0.1-1")
    compileOnly("com.pkware.jabel:jabel-javac-plugin:1.0.1-1")

    // This only here so that we are notified of dependency updates, because we are not notified for version updates in the constraints block. If we are overriding the version, we should override it with the latest version.
    api("net.bytebuddy:byte-buddy:1.17.6")

    constraints {
        configurations.forEach { configuration ->
            if ((!configuration.isCanBeConsumed() && !configuration.isCanBeResolved()) || configuration.name.equals("annotationProcessor")) {
                add(configuration.name, "net.bytebuddy:byte-buddy-parent") {
                    version {
                        strictly("1.17.6")
                        because("Older version doesn't have Java 24 support")
                    }
                }
                add(configuration.name, "net.bytebuddy:byte-buddy") {
                    version {
                        strictly("1.17.6")
                        because("Older version doesn't have Java 24 support")
                    }
                }
                add(configuration.name, "net.bytebuddy:byte-buddy-agent") {
                    version {
                        strictly("1.17.6")
                        because("Older version doesn't have Java 24 support")
                    }
                }
            }
        }
    }
}

sourceSets {
    main {
        //output.resourcesDir = kotlin.classesDirectory
    }
}

tasks {
    processResources {
        val versionProvider = providers.provider { project.version.toString() }
        val descriptionProvider = providers.provider { project.description ?: "" }

        inputs.property("version", versionProvider)
        inputs.property("mcversion", "1.8.9")
        inputs.property("description", descriptionProvider)

        filesMatching("mcmod.info") {
            expand(mapOf("version" to versionProvider.get(), "mcversion" to "1.8.9", "description" to descriptionProvider.get()))
        }
        dependsOn(compileJava)
    }
    named<Jar>("jar") {
        manifest {
            attributes(
                mapOf(
                    "Multi-Release" to true,
                    "Name" to "gg/darkaddons/",
                    //"Sealed" to true,
                    "Main-Class" to "darkaddons.installer.DarkAddonsInstaller",
                    "Automatic-Module-Name" to "gg.darkaddons",
                    "Specification-Title" to "DarkAddons",
                    "Specification-Version" to project.version.toString(),
                    "Specification-Vendor" to "DarkAddons",
                    "Implementation-Title" to "DarkAddons",
                    "Implementation-Version" to project.version.toString(),
                    "Implementation-Vendor" to "DarkAddons",
                    "FMLCorePlugin" to "gg.darkaddons.coremod.DarkAddonsLoadingPlugin",
                    "FMLCorePluginContainsFMLMod" to true,
                    "ForceLoadAsMod" to true,
                    "MixinConfigs" to "mixins.darkaddons.json",
                    "ModSide" to "CLIENT",
                    "ModType" to "FML",
                    //"TweakClass" to "gg.darkaddons.tweaker.DarkAddonsTweaker",
                    "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
                    "TweakOrder" to "1",
                    "FMLAT" to "darkaddons_at.cfg"
                )
            )
        }
        dependsOn(shadowJar)
        enabled = false
    }
    named<RemapJarTask>("remapJar") {
        archiveBaseName = "DarkAddons"
        inputFile = shadowJar.get().archiveFile
        /*doLast {
            MessageDigest.getInstance("SHA-256").digest(archiveFile.get().asFile.readBytes())
                .let {
                    println("SHA-256: " + it.joinToString(separator = "") { "%02x".format(it) }.uppercase())
                }
        }*/
    }
    named<ShadowJar>("shadowJar") {
        archiveBaseName = "DarkAddons"
        archiveClassifier = "dev"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        configurations = listOf(shadowMe, shadowMeMod)

        // TODO make it darkaddons
        relocate("dev.falsehonesty.asmhelper", "gg.skytils.asmhelper")
        relocate("com.llamalad7.mixinextras", "gg.skytils.mixinextras")
        relocate("kotlinx.coroutines", "gg.skytils.ktx-coroutines")
        relocate("net.hypixel.modapi.tweaker", "gg.skytils.hypixel-net.modapi.tweaker")

        exclude(
            "**/LICENSE.md",
            "**/LICENSE.txt",
            "**/LICENSE",
            "**/LICENSE*",
            "**/NOTICE",
            "**/NOTICE.txt",
            "pack.mcmeta",
            "dummyThing",
            "**/module-info.class",
            "**/*.kotlin_module",
            "META-INF/proguard/**",
            "META-INF/maven/**",
            "META-INF/versions/**",
            "META-INF/com.android.tools/**",
            "fabric.mod.json"
        )
        mergeServiceFiles()
    }
    withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    //TODO javaCompile task never says UP-TO-DATE, figure why it doesnt work with build cache.
    withType<JavaCompile> {
        val path = File("${projectDir}/src/main/java/gg/darkaddons/Reference.java").toPath()
        val version = project.version.toString()

        doFirst {
            Files.write(
                path,
                StringUtils.replace(
                    String(Files.readAllBytes(path), StandardCharsets.UTF_8),
                    "@VERSION@",
                    version
                ).toByteArray(StandardCharsets.UTF_8)
            )
        }

        doLast {
            Files.write(
                path,
                StringUtils.replace(
                    String(Files.readAllBytes(path), StandardCharsets.UTF_8),
                    version,
                    "@VERSION@"
                ).toByteArray(StandardCharsets.UTF_8)
            )
        }

        options.encoding = "UTF-8"

        options.setIncremental(true)

        //options.deprecation = true
        options.release = 8
        sourceCompatibility = "24"

        options.compilerArgs.add("-g")
        //options.compilerArgs.add("-encoding UTF-8")
        options.forkOptions.jvmArgs!!.add("-Xmx2G")
        options.forkOptions.jvmArgs!!.add("-XX:+UnlockExperimentalVMOptions")
        options.forkOptions.jvmArgs!!.add("-XX:+IgnoreUnrecognizedVMOptions")
        options.forkOptions.jvmArgs!!.add("-XX:+EnableDynamicAgentLoading")
        options.forkOptions.jvmArgs!!.add("--enable-native-access=ALL-UNNAMED")
        options.compilerArgs.add("-parameters")
        options.compilerArgs.add("-Xlint:all,-options,-classfile,-processing,-overrides")
    }
    named<JavaCompile>("compileTestJava") {
        options.release.set(24)
    }
    /*withType<KotlinCompilationTask<KotlinJvmCompilerOptions>> {
        compilerOptions {
            javaParameters = true
            jvmTarget = JvmTarget.JVM_1_8

            val args = mutableListOf<String>()
            args.addAll(freeCompilerArgs.get())

            args.addAll(
                listOf(
                    "-Xjsr305=strict",
                    "-Xjvm-default=all",
                    "-Xno-param-assertions",
                    "-Xno-call-assertions",
                    "-Xno-receiver-assertions",
                    "-Xassertions=always-disable",
                    "-Xsuppress-missing-builtins-error",
                    "-Xabi-stability=stable",
                    "-Xemit-jvm-type-annotations",
                    "-Xlambdas=indy",
                    "-Xsam-conversions=indy",
                    "-Xbackend-threads=0",
                    "-Xuse-type-table",
                    "-Xjavac-arguments=[\"-g\", \"-encoding UTF-8\", \"-J-Xmx2G\", \"-parameters\", \"-Xlint:all,-options,-classfile,-processing,-overrides\"]",
                    "-Xno-source-debug-extension",
                    "-Xtype-enhancement-improvements-strict-mode",
                    "-Xenhance-type-parameter-types-to-def-not-null",
                    "-Xvalue-classes",
                    //"-Xjdk-release=8",
                    "-Xsuppress-version-warnings"
                )
            )

            freeCompilerArgs = args

            languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0
        }
    }
    withType<CompileUsingKotlinDaemon> {
        val args = mutableListOf<String>()
        args.addAll(kotlinDaemonJvmArguments.get())

        args.addAll(
            listOf(
                //TODO reduce RAM consumption of build
                "-Xmx2G",
                "-Dkotlin.enableCacheBuilding=true",
                "-Dkotlin.useParallelTasks=true",
                "-Dkotlin.enableFastIncremental=true",
                //"-Xbackend-threads=0"
            )
        )

        kotlinDaemonJvmArguments = args
    }*/
    withType<AbstractArchiveTask>().configureEach {
        //isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    register<Delete>("deleteClassloader") {
        delete(
            "${project.projectDir}/run/CLASSLOADER_TEMP",
            "${project.projectDir}/run/CLASSLOADER_TEMP1",
            "${project.projectDir}/run/CLASSLOADER_TEMP2",
            "${project.projectDir}/run/CLASSLOADER_TEMP3",
            "${project.projectDir}/run/CLASSLOADER_TEMP4",
            "${project.projectDir}/run/CLASSLOADER_TEMP5",
            "${project.projectDir}/run/CLASSLOADER_TEMP6",
            "${project.projectDir}/run/CLASSLOADER_TEMP7",
            "${project.projectDir}/run/CLASSLOADER_TEMP8",
            "${project.projectDir}/run/CLASSLOADER_TEMP9",
            "${project.projectDir}/run/CLASSLOADER_TEMP10"
        )
    }
    test {
        useJUnitPlatform()

        testLogging {
            exceptionFormat = TestExceptionFormat.FULL

            outputs.upToDateWhen { false }
            showStandardStreams = true
        }
    }
    configurations.testImplementation.get().extendsFrom(configurations.compileOnly.get())
    withType<Test>().configureEach {
        reports.html.required = false
        reports.junitXml.required = false
    }
}

signing {
    if (project.hasProperty("signing.gnupg.keyName")) {
        useGpgCmd()
        sign(tasks["remapJar"])
    }
    //else if (project.hasProperty("signing.keyId")) {
    //    sign(tasks["remapJar"])
    //}
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            groupId = project.group.toString()
            artifactId = project.name.lowercase(Locale.ROOT)
            version = project.version.toString()
            from(components["java"])
        }
    }
}

private val proguardJar: TaskProvider<ProguardTask> by tasks.registering(proguard.taskClass) {
    addInput {
        classpath.from(tasks.remapJar)
    }
    addOutput {
        archiveFile = base.libsDirectory.file("${project.name}-${project.version}-proguarded.jar")
    }
    jdkModules.add("java.base")
    jdkModules.add("java.desktop")
    jdkModules.add("java.management")
    jdkModules.add("jdk.management")
    jdkModules.add("jdk.httpserver")
    addLibrary {
        classpath.from("libs/mcSrg.jar")
        classpath.from(project.configurations.compileClasspath)
    }
    //mappingFile = base.libsDirectory.file("${project.name}-${project.version}-mapping.txt")

    rulesFiles.from("DarkAddons.pro")

    maxHeapSize = "2G"
}

tasks.build {
    finalizedBy("proguardJar")
}

/*tasks.register("cacheToMavenLocal", Copy::class) {
    from(File(gradle.gradleUserHomeDir, "caches/modules-2/files-2.1"))
    into(repositories.mavenLocal().url)

    eachFile {
        val parts: List<String> = path.split("/")
        path = listOf(StringUtils.replaceChars(parts[0], '.', '/'), parts[1], parts[2], parts[4]).joinToString("/")
    }

    includeEmptyDirs = false
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.build {
    finalizedBy("cacheToMavenLocal")
}*/

