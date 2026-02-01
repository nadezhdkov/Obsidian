plugins {
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.34.0"
    id("signing")
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.matching { it.name == "generateMetadataFileForMavenPublication" }.configureEach {
    dependsOn(tasks.matching { it.name == "plainJavadocJar" })
    dependsOn(tasks.matching { it.name == "plainSourcesJar" })
}

dependencies {
    implementation(libs.annotations)

    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitJupiter)
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        project.group.toString(),
        "obsidian",
        project.version.toString()
    )

    pom {
        name.set("Obsidian")
        description.set("Obsidian is a modern, modular Java framework focused on developer ergonomics, offering advanced reflection, flexible configuration, concurrency abstractions, and high-quality utility APIs.")
        inceptionYear.set("2026")
        url.set("https://github.com/nadezhdkov/Obsidian")

        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("nadezhdkov")
                name.set("Nadezhdkov")
                url.set("https://github.com/nadezhdkov")
            }
        }

        scm {
            url.set("https://github.com/nadezhdkov/Obsidian")
            connection.set("scm:git:https://github.com/nadezhdkov/Obsidian.git")
            developerConnection.set("scm:git:ssh://git@github.com/nadezhdkov/Obsidian.git")
        }
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.vanniktech.maven.publish")
    apply(plugin = "signing")

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    tasks.matching { it.name == "generateMetadataFileForMavenPublication" }.configureEach {
        dependsOn(tasks.matching { it.name == "plainJavadocJar" })
        dependsOn(tasks.matching { it.name == "plainSourcesJar" })
    }

    dependencies {
        testImplementation(platform(rootProject.libs.junitBom))
        testImplementation(rootProject.libs.junitJupiter)

        compileOnly(rootProject.libs.lombok)
        annotationProcessor(rootProject.libs.lombok)

        implementation(rootProject.libs.annotations)
    }

    mavenPublishing {
        publishToMavenCentral()
        signAllPublications()

        coordinates(
            project.group.toString(),
            project.name,
            project.version.toString()
        )

        pom {
            name.set("Obsidian - ${project.name}")
            description.set("Obsidian module: ${project.name}.")
            url.set("https://github.com/nadezhdkov/Obsidian")

            licenses {
                license {
                    name.set("Apache License 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    distribution.set("repo")
                }
            }

            developers {
                developer {
                    id.set("nadezhdkov")
                    name.set("Nadezhdkov")
                    url.set("https://github.com/nadezhdkov")
                }
            }

            scm {
                url.set("https://github.com/nadezhdkov/Obsidian")
                connection.set("scm:git:https://github.com/nadezhdkov/Obsidian.git")
                developerConnection.set("scm:git:ssh://git@github.com/nadezhdkov/Obsidian.git")
            }
        }
    }
}
