plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
    id("com.gradleup.shadow") version "8.3.5"
}

group = "com.vialreport.backend"
version = "1.0.0"

application {
    mainClass.set("com.vialreport.backend.ApplicationKt")
}

val ktor_version = "2.3.7"
val exposed_version = "0.45.0"

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("io.ktor:ktor-server-cors:$ktor_version")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("ch.qos.logback:logback-classic:1.4.11")
}