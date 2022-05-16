import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin") version "2.0.2"
}

val name: String by project
val group: String by project
val version: String by project


project.group = group
project.version = version

repositories {
    mavenCentral()
}

val junitVersion: String by project
val spongeApiVersion: String by project

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

sponge {
    apiVersion(spongeApiVersion)
    license("GPL-3.0")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin(name) {
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