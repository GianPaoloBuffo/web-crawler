plugins {
    kotlin("jvm") version "2.1.21"
    application
}

group = "org.gp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.webcrawler.Main")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    // HTTP client for making requests
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-client-cio:2.3.12")
    implementation("io.ktor:ktor-client-logging:2.3.12")
    
    // HTML parsing
    implementation("org.jsoup:jsoup:1.17.2")
    
    // Coroutines for concurrency
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
}

// Task to run the crawler with arguments
tasks.register<JavaExec>("crawl") {
    group = "application"
    description = "Run the web crawler with specified URL"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.webcrawler.Main")
    
    // Allow passing arguments via -Pargs
    if (project.hasProperty("args")) {
        args = (project.property("args") as String).split(" ")
    }
}