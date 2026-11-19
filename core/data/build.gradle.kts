plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.mauricioliveira.awesomenotes.core.data"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())
}

dependencies {
    api(projects.core.domain)
    implementation(projects.core.network)
    implementation(projects.core.storage)
    implementation(projects.core.database)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(kotlin("test"))
}
