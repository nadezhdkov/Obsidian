plugins {
    id("java")
    id("com.vanniktech.maven.publish") version "0.34.0"
    id("signing")
}

group = "obsidian.lib"
version = "1.0.0"

java {
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }
}

allprojects {
    group   = "obsidian.lib"
    version = "1.0.0"
}

dependencies {
    testImplementation  (platform(libs.junit))
    testImplementation  (libs.junitjupiter)

    compileOnly         (libs.lombok)
    annotationProcessor (libs.lombok)

    implementation      (libs.gson)
    implementation      (libs.yaml)

    implementation      (libs.annotations)

    implementation      (project(":obsidian-configuration"))
    implementation      (project(":obsidian-reflection")   )
    implementation      (project(":obsidian-promise")      )
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {

    coordinates(
        group.toString(),
        "obsidian",
        version.toString()
    )

    pom {
        name.set("Obsidian")
        description.set(
            "Obsidian é uma biblioteca Java modular focada em produtividade, reflexão avançada, configuração dinâmica e utilitários modernos."
        )

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