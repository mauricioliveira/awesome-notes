plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
}

group = "com.mauricioliveira.awesomenotes"

application {
    mainClass = "com.mauricioliveira.awesomenotes.server.ApplicationKt"
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())
}

ktor {
    fatJar {
        archiveFileName = "server.jar"
    }
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.swagger)
    implementation(libs.logback.classic)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.postgresql)
    implementation(libs.hikari)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(kotlin("test"))
}

tasks.processResources {
    from("../api") {
        into("openapi")
    }
}