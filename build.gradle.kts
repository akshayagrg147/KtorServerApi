val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.7.10"
}

group = "com.example"
version = "0.0.1"
application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:2.1.2")
    implementation("io.ktor:ktor-server-netty-jvm:2.1.2")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:2.1.2")
    implementation ("io.ktor:ktor-server-core:2.1.0")
    implementation ("io.ktor:ktor-gson:1.6.8")
    testImplementation ("io.ktor:ktor-server-tests:2.1.0")
    implementation("io.ktor:ktor-server-content-negotiation:2.1.2")
    implementation("io.ktor:ktor-serialization-gson:2.1.2")
    implementation("org.litote.kmongo:kmongo-coroutine:4.7.1")
    implementation("io.ktor:ktor-server-auth:2.1.2")
    implementation("io.ktor:ktor-server-auth-jwt:2.1.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}