import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.PluginDependency

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin") version "1.1.1"
}

group = "team.ebi"
version = "0.5.0"

repositories {
    mavenCentral()
}

sponge {
    apiVersion("8.0.0")
    plugin("epicbanitem") {
        loader(PluginLoaders.JAVA_PLAIN)
        displayName("EpicBanItem")
        mainClass("team.ebi.epicbanitem.EpicBanItem")
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

val javaTarget = 8 // Sponge targets a minimum of Java 8
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