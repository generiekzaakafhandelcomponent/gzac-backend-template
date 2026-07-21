@file:Suppress("UNCHECKED_CAST")

import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun
import java.util.Properties

plugins {
    // Idea
    idea
    id("org.jetbrains.gradle.plugin.idea-ext")

    // Spring
    id("org.springframework.boot")
    id("io.spring.dependency-management")

    // Kotlin
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    kotlin("plugin.allopen")

    // Checkstyle
    id("org.jlleitschuh.gradle.ktlint")
    id("com.diffplug.spotless")

    // Other
    id("com.avast.gradle.docker-compose")
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/releases/") }
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
    // Valtimo backend libraries are moving from Sonatype Central to S3 (OVHcloud).
    // Sonatype publishing stops on 10 August 2026; after that they resolve only from S3.
    // TODO: replace the placeholder host below with the real OVHcloud bucket URL.
    maven {
        url = uri("https://valtimo-releases.s3.placeholder.example.com/")
        // Only resolve Valtimo artifacts from S3; everything else stays on Maven Central.
        content { includeGroup("com.ritense.valtimo") }
    }
}

val valtimoVersion: String by project
val mockitoKotlinVersion: String by project
val camundaBpmAssertVersion: String by project

dependencies {
    implementation(platform("com.ritense.valtimo:valtimo-dependency-versions:$valtimoVersion"))

    implementation("com.ritense.valtimo:valtimo-gzac-dependencies")

    implementation("com.ritense.valtimo:local-mail")

    implementation("org.postgresql:postgresql")

    // Plugins
    implementation("com.ritense.valtimoplugins:freemarker:6.0.1")
    implementation("com.ritense.valtimoplugins:smtpmail:1.0.2")

    // Kotlin logger
    implementation("io.github.microutils:kotlin-logging")

    // Testing
    testImplementation("com.ritense.valtimo:test-utils-common")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.camunda.bpm.assert:camunda-bpm-assert:$camundaBpmAssertVersion")
    testImplementation("org.camunda.bpm.extension:camunda-bpm-junit5:1.1.0")
    testImplementation("org.camunda.bpm.extension:camunda-bpm-assert:1.2")
    testImplementation("org.camunda.bpm.extension:camunda-bpm-assert-scenario:1.1.1")
    testImplementation("org.camunda.bpm.extension.mockito:camunda-bpm-mockito:5.16.0")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

apply(plugin = "docker-compose")
apply(from = "gradle/dockerComposeGzac.gradle.kts")

dockerCompose {
    setProjectName("gzac-docker-compose")
    useDockerComposeV2 = true
    useComposeFiles.add(
        layout.buildDirectory
            .dir("docker/extract/gzac-docker-compose-v-12/docker-compose.yaml")
            .get()
            .asFile
            .absolutePath,
    )
    composeAdditionalArgs.addAll("--profile", "zgw")
    stopContainers = false
    removeContainers = false
    removeVolumes = false
    if (DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        executable = "/usr/local/bin/docker-compose"
        dockerExecutable = "/usr/local/bin/docker"
    }
}

ktlint {
    version.set("1.4.1")
}

apply(from = "gradle/environment.gradle.kts")
val configureEnvironment = extra["configureEnvironment"] as (task: ProcessForkOptions) -> Unit

tasks.bootRun {
    val t = this
    doFirst {
        configureEnvironment(t)
    }
}

tasks.register("bootRunWithDocker", BootRun::class.java) {
    group = "application"
    description = "Starts docker containers and then runs this project as a Spring Boot application"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "com.ritense.valtimo.ApplicationKt"

    dependsOn("composeUpGzac")
    doFirst {
        val f = file(".env.properties")
        if (f.isFile()) {
            val props = Properties()
            f.inputStream().use { props.load(it) }
            props.forEach { key, value ->
                environment[key.toString()] = value.toString()
            }
        }
    }
}