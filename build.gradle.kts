val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.0.21"
    id("io.ktor.plugin") version "3.0.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
}

group = "com.sakethh"
version = "0.1.0"

application {
    mainClass.set("com.sakethh.linkora.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

ktor {
    fatJar {
        archiveFileName.set("linkoraSyncServer.jar")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-websockets")
    implementation("io.ktor:ktor-network")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-websockets-jvm:3.0.1")
    implementation("io.ktor:ktor-server-cors")
    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    implementation("org.jetbrains.exposed:exposed-core:1.0.0-beta-4")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.0.0-beta-4")

    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("com.h2database:h2:2.2.224")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.3.1")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.oracle.database.jdbc:ojdbc8:12.2.0.1")
    implementation("org.postgresql:postgresql:42.7.5")
    implementation("com.microsoft.sqlserver:mssql-jdbc:9.4.1.jre8")
    implementation("io.github.sakethpathike:kapsule:0.1.2")
    implementation("org.jetbrains:markdown:0.7.3")

    implementation("io.ktor:ktor-network-tls-certificates-jvm")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}