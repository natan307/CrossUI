plugins {
    `java-library`
    `maven-publish`
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    // Only the standard Spigot API: compatible with Spigot, Paper, Purpur and Folia.
    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
        encoding = "UTF-8"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.natan307"
            artifactId = "crossui-api"
            version = project.version.toString()
            from(components["java"])

            pom {
                name.set("CrossUI API")
                description.set("Public API of CrossUI: one builder for Bedrock Forms, Java Native Dialogs and chest GUIs.")
                url.set("https://github.com/natan307/CrossUI")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("natan307")
                        url.set("https://github.com/natan307")
                    }
                }
                scm {
                    url.set("https://github.com/natan307/CrossUI")
                    connection.set("scm:git:https://github.com/natan307/CrossUI.git")
                }
            }
        }
    }
}
