plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("signing")
}

group = (findProperty("GROUP") as String?) ?: "org.pocketnest"
version = (findProperty("VERSION_NAME") as String?) ?: "1.0.0"

android {
    namespace = "org.pocketnest.sdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34
        consumerProguardFiles("consumer-rules.pro")
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}


// 💡 In Kotlin DSL you don't need afterEvaluate here.
// Configure a Maven publication that JitPack will pick up.
publishing {
    publications {
        register<MavenPublication>("release") {
            // JitPack expects this groupId format
            groupId = "com.github.pocketnest"
            // This is what your clients will use as artifactId
            artifactId = "PocketnestSDK-Android"
            // The version comes from the Git tag (e.g., v1.0.1) when JitPack builds

            // Publish the Android "release" AAR
            from(components["release"])
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.browser:browser:1.7.0")
    implementation("androidx.core:core-ktx:1.13.1")
}