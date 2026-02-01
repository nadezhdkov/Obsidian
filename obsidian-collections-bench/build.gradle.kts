plugins {
    java
    id("me.champeau.jmh") version "0.7.2"
}

dependencies {
    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    jmh(project(":obsidian-collections"))
}

tasks.named<JavaCompile>("compileJmhJava") {
    dependsOn(":obsidian-collections:build")
}