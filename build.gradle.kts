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
}

dependencies {
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
