import java.time.Instant

plugins {
    id("java")
    id("org.ajoberstar.grgit") version "5.2.1"
    id("maven-publish")
}

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21
java.toolchain.languageVersion = JavaLanguageVersion.of(21)
group = "ru.brominemc.srb.srb-core"
description = "A localization/translation library for BromineMC."

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("com.google.errorprone:error_prone_annotations:2.24.1")
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
                "Implementation-Title" to "SRB-Core",
                "Implementation-Version" to "git-${grgit.branch.current().name}-${grgit.head().abbreviatedId}-${Instant.now()}",
                "Implementation-Vendor" to "VidTu, threefusii"
        )
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<Javadoc> {
    options {
        this as StandardJavadocDocletOptions
        tags(
                "apiNote:a:API Note:",
                "implNote:a:Implementation Note:"
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "ru.brominemc.srb"
            artifactId = "srb-core"
            pom {
                name = "SRB (Core)"
                description = project.description
                url = "https://github.com/BromineMC/SRB"
                packaging = "jar"
                inceptionYear = "2023"
                organization {
                    name = "BromineMC"
                    url = "https://github.com/BromineMC"
                }
                scm {
                    connection = "git@github.com/BromineMC/SRB.git"
                    developerConnection = "git@github.com/BromineMC/SRB.git"
                    url = "https://github.com/BromineMC/SRB.git"
                }
                issueManagement {
                    system = "GitHub Issues"
                    url = "https://github.com/BromineMC/SRB/issues"
                }
                ciManagement {
                    system = "GitHub Actions"
                    url = "https://github.com/BromineMC/SRB/actions"
                }
                developers {
                    developer {
                        id = "vidtu"
                        name = "VidTu"
                        email = "imvidtu@proton.me"
                        url = "https://github.com/VidTu"
                        organization = "BromineMC"
                        organizationUrl = "https://github.com/BromineMC"
                    }
                    developer {
                        id = "threefusii"
                        name = "3fusii"
                        email = "threefusii@outlook.com"
                        url = "https://github.com/threefusii"
                        organization = "BromineMC"
                        organizationUrl = "https://github.com/BromineMC"
                    }
                }
                licenses {
                    license {
                        name = "Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
            }
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
