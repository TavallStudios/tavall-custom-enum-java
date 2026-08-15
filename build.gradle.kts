plugins {
    `java-library`
    `maven-publish`
}

group = "org.tavall"
extra["versionTagPrefix"] = "tavall-java-utils"
apply(from = "gradle/git-version.gradle.kts")
version = extra["gitVersion"] as String

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    val token = providers.environmentVariable("GITHUB_TOKEN")
    if (token.isPresent) {
        maven {
            name = "TavallDiPackages"
            url = uri("https://maven.pkg.github.com/TavallStudios/tavall-di")
            credentials {
                username = providers.environmentVariable("GITHUB_ACTOR").orElse("github").get()
                password = token.get()
            }
        }
    }
}

dependencies {
    // tavall-java-utils is a Tavall-owned Java consumer, not one of the nine tavall-java-tools implementation repos.
    api("org.tavall:tavall-di:1.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "tavall-java-utils"
        }
    }
    repositories {
        val token = providers.environmentVariable("GITHUB_TOKEN")
        if (token.isPresent) {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/TavallStudios/tavall-java-utils")
                credentials {
                    username = providers.environmentVariable("GITHUB_ACTOR").orNull
                    password = token.get()
                }
            }
        }
    }
}
