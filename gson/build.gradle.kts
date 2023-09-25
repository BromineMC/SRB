plugins {
    id("java-library")
    id("maven-publish")
}

group = "ru.brominemc.srb.srb-gson"
java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")
    compileOnly("com.google.errorprone:error_prone_annotations:2.22.0")
    api(project(":"))
    api("com.google.code.gson:gson:2.10.1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "ru.brominemc.srb"
            artifactId = "srb-gson"
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