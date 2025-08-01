@file:Suppress("UNCHECKED_CAST")

import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun
import java.util.Properties

plugins {
    war
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
}

val valtimoVersion: String by project
val postgresqlDriverVersion: String by project
val nettyResolverDnsNativeMacOsVersion: String by project
val mockitoKotlinVersion: String by project
val camundaBpmAssertVersion: String by project

dependencies {
    implementation(platform("com.ritense.valtimo:valtimo-dependency-versions:$valtimoVersion"))

    implementation("com.ritense.valtimo:valtimo-gzac-dependencies")

    implementation("com.ritense.valtimo:local-mail")

    implementation("org.postgresql:postgresql:$postgresqlDriverVersion")

    if (System.getProperty("os.arch") == "aarch64") {
        runtimeOnly("io.netty:netty-resolver-dns-native-macos:$nettyResolverDnsNativeMacOsVersion:osx-aarch_64")
    }

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
    useComposeFiles.add("${buildDir.absolutePath}/docker/extract/gzac-docker-compose-v-12/docker-compose.yaml")
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