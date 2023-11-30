import java.time.Instant

plugins {
    id("java-library")
    id("maven-publish")
    id("org.ajoberstar.grgit") version "5.2.1"
}

group = "ru.brominemc.srb.core"
java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

repositories {
    mavenCentral()
}

dependencies {
    compileOnlyApi("org.jetbrains:annotations:24.1.0")
    compileOnlyApi("com.google.errorprone:error_prone_annotations:2.23.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<Jar> {
    manifest {
        attributes(
                "Specification-Title" to "SRB",
                "Specification-Version" to project.version,
                "Specification-Vendor" to "BromineMC",
                "Implementation-Title" to "SpeakRussianBl**t",
                "Implementation-Version" to "git-${grgit.branch.current().name}-${grgit.head().abbreviatedId}-${Instant.now()}",
                "Implementation-Vendor" to project.properties["contributors"]
        )
    }
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "ru.brominemc.srb"
            artifactId = "srb-core"
        }
    }
    repositories {
        maven {
            name = "BromineMcReleases"
            url = uri("https://api.brominemc.ru/maven/releases")
            credentials {
                username = System.getenv("MAVEN_NAME")
                password = System.getenv("MAVEN_PASS")
            }
        }
    }
}