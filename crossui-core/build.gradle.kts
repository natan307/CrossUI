import java.util.zip.ZipFile

plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.3.1"
}

dependencies {
    implementation(project(":crossui-api"))
    implementation("org.bstats:bstats-bukkit:3.2.1")

    // PacketEvents is shaded and relocated into CrossUI.jar, so admins do not install it separately.
    implementation("com.github.retrooper:packetevents-spigot:2.14.0")
    implementation("net.kyori:adventure-api:4.26.1")

    // spigot-api as the base: everything compiled against it also runs on Paper, Purpur and Folia.
    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.1.72.Final")
    // Optional Bedrock bridges, provided by the server when present.
    compileOnly("org.geysermc.geyser:api:2.11.2-SNAPSHOT")
    compileOnly("org.geysermc.floodgate:api:2.2.5-SNAPSHOT")
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.jar {
    archiveClassifier.set("plain")
}

tasks.shadowJar {
    archiveBaseName.set("CrossUI")
    archiveClassifier.set("")
    archiveVersion.set(project.version.toString())

    // CrossUI's own files are added first, so its plugin.yml wins over the one inside PacketEvents.
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "module-info.class")
    exclude("io/github/retrooper/packetevents/PacketEventsPlugin*.class")
    mergeServiceFiles()

    // Relocate every bundled library so it can never clash with another plugin or with the
    // PacketEvents plugin if an admin has it installed anyway.
    val libs = "io.github.natan307.crossui.libs"
    relocate("com.github.retrooper.packetevents", "$libs.packetevents.api")
    relocate("io.github.retrooper.packetevents", "$libs.packetevents.impl")
    relocate("net.kyori", "$libs.kyori")
    relocate("org.bstats", "$libs.bstats")

    // Safety check: the final jar must contain CrossUI's plugin.yml, not PacketEvents'.
    val jarFile = archiveFile
    doLast {
        ZipFile(jarFile.get().asFile).use { zip ->
            val entry = zip.getEntry("plugin.yml") ?: error("plugin.yml is missing from the jar")
            val text = zip.getInputStream(entry).bufferedReader().readText()
            val name = text.lineSequence().firstOrNull { it.startsWith("name:") }.orEmpty()
            check(name.contains("CrossUI")) { "Wrong plugin.yml in the jar (found '$name')" }
        }
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
