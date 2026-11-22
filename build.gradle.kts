allprojects {
    group = "io.github.natan307.crossui"
    version = "2.0.1"

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.codemc.io/repository/maven-releases/")
        maven("https://repo.codemc.io/repository/maven-snapshots/")
        maven("https://repo.opencollab.dev/main/")
    }
}

subprojects {
    apply(plugin = "java")

    // Bytecode is Java 21 so one jar runs on 1.21.x (Java 21) and 26.x (Java 25) servers.
    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}
