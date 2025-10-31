plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

group = (findProperty("GROUP") as String?) ?: "org.pocketnest"
version = (findProperty("VERSION_NAME") as String?) ?: "1.0.0"

android {
    namespace = "org.pocketnest.sdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
//            withJavadocJar() optional
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

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.browser:browser:1.7.0")
    implementation("androidx.core:core-ktx:1.13.1")
}

// 🔑 Defer binding the component until after Android has created it
afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = "com.github.pocketnest"         // JitPack convention
                artifactId = "PocketnestSDK-Android"      // coordinate you want
                // version is taken from the Git tag on JitPack

                from(components["release"])
            }
        }
    }
}

