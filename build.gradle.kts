plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.gradleup.shadow") version "9.4.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    // Compile against the oldest supported release (1.20.6) so the resulting JAR also
    // remains binary-compatible with newer Paper servers up to 26.x.
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9") {
        isTransitive = false
    }
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.15") {
        isTransitive = false
    }
    compileOnly("com.sk89q.worldedit:worldedit-core:7.2.15") {
        isTransitive = false
    }
    compileOnly("com.sk89q.worldguard:worldguard-core:7.0.9") {
        isTransitive = false
    }
    implementation("org.bstats:bstats-bukkit:3.2.1")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    runServer {
        minecraftVersion(providers.gradleProperty("mcVersion").getOrElse("1.21.4"))
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    shadowJar {
        archiveClassifier.set("")
        
        dependencies {
            include(dependency("org.bstats:bstats-bukkit:3.2.1"))
            include(dependency("org.bstats:bstats-base:3.2.1"))
        }

        relocate("org.bstats", "${project.group}.bstats")
    }

    jar {
        enabled = false
        dependsOn(shadowJar)
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        val props = mapOf("version" to version, "description" to project.description)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
