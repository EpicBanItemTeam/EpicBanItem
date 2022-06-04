import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin") version "2.0.2"
    id("org.spongepowered.gradle.vanilla") version "0.2"
    id("com.diffplug.spotless") version "6.6.1"
    id("io.github.nefilim.gradle.semver-plugin") version "0.3.13"
    id("com.github.johnrengelman.shadow") version "7.1.2"
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

dependencies {
    shadow("org.bstats:bstats-sponge:$bstatsVersion")

    testRuntimeOnly("org.spongepowered:spongeapi:$spongeApiVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    testImplementation("org.mockito:mockito-inline:$mockitoVersion")
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
        description("Restrict items with nbt tags")
        links {
            homepage("https://docs.ebi.team")
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
}

val javaTarget = 16
java {
    sourceCompatibility = JavaVersion.toVersion(javaTarget)
    targetCompatibility = JavaVersion.toVersion(javaTarget)
}

tasks.withType(JavaCompile::class).configureEach {
    options.apply {
        encoding = "utf-8" // Consistent source file encoding
        if (JavaVersion.current().isJava10Compatible) {
            release.set(javaTarget)
        }
    }
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}
artifacts {
    archives (tasks.shadowJar)
}

tasks {
    test {
        useJUnitPlatform()
    }
    runServer {
        jvmArgs("-XX:+AllowEnhancedClassRedefinition", "-XX:HotswapAgent=fatjar", "-Dlog4j.configurationFile=../log4j2.xml")
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