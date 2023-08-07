import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    idea
    `java-library`
    id("org.spongepowered.gradle.plugin") version "2.1.1"
    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
    id("com.diffplug.spotless") version "6.20.0"
    id("io.github.nefilim.gradle.semver-plugin") version "0.3.13"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val id: String by project
val group: String by project

semver {
    tagPrefix("v")
    initialVersion("1.0.0")
    findProperty("semver.overrideVersion")?.toString()?.let { overrideVersion(it) }
}

project.group = group
project.version = semver.version

repositories {
    mavenCentral()
}

val junitVersion: String by project
val mockitoVersion: String by project
val spongeApiVersion: String by project
val bstatsVersion: String by project
val jabelVersion: String by project

dependencies {
    shadow("org.bstats:bstats-sponge:$bstatsVersion")

    annotationProcessor("com.github.bsideup.jabel:jabel-javac-plugin:$jabelVersion")
    compileOnly("com.github.bsideup.jabel:jabel-javac-plugin:$jabelVersion")
    annotationProcessor("net.java.dev.jna:jna-platform:5.13.0")

    testRuntimeOnly("org.spongepowered:spongeapi:$spongeApiVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
}

sponge {
    apiVersion(spongeApiVersion)
    license("GPL-3.0")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin(id) {
        displayName("Epic Ban Item")
        entrypoint("team.ebi.epicbanitem.EpicBanItem")
        description("Modify objects in game with powerful rules")
        links {
            homepage("https://github.com/EpicBanItemTeam/EpicBanItem")
            source("https://github.com/EpicBanItemTeam/EpicBanItem")
            issues("https://github.com/EpicBanItemTeam/EpicBanItem/issues")
        }
        contributor("SettingDust") {
            description("Little Developer")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

minecraft {
    version("1.16.5")

    runs {
        server("runVanillaServer") {
            workingDirectory(file("run/server"))
        }
        client("runVanillaClient") {
            workingDirectory(file("run/client"))
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    options.apply {
        encoding = "utf-8" // Consistent source file encoding
        release.set(8)
    }

    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}
artifacts {
    archives(tasks.shadowJar)
}

tasks {
    test {
        useJUnitPlatform()
    }
    runServer {
        jvmArgs(
            "-XX:+AllowEnhancedClassRedefinition",
            "-XX:HotswapAgent=fatjar",
            "-Dlog4j.configurationFile=../log4j2.xml"
        )
    }
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
        archiveClassifier.set("")
        minimize()
    }
}

spotless {
    encoding(Charsets.UTF_8)
    java {
        palantirJavaFormat()
        importOrder("java", "javax", "org.spongepowered", "", "\\#")
        licenseHeader(
            """
           /*
            * Copyright ${'$'}YEAR EpicBanItem Team. All Rights Reserved.
            *
            * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
            */""".trimIndent()
        )
    }
}
