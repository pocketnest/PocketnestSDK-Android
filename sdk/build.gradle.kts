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

afterEvaluate {
    val pub = publishing.publications.create("release", MavenPublication::class.java) {
        from(components["release"])
        groupId = project.group.toString()
        artifactId = (findProperty("POM_ARTIFACT_ID") as String?) ?: "pocketnest-sdk"
        version = project.version.toString()

        pom {
            name.set(findProperty("POM_NAME") as String? ?: "Pocketnest SDK (Android)")
            description.set(findProperty("POM_DESCRIPTION") as String? ?: "Android SDK for Pocketnest")
            url.set(findProperty("POM_URL") as String? ?: "https://github.com/pocketnest")

            licenses {
                license {
                    name.set(findProperty("POM_LICENSE_NAME") as String? ?: "The MIT License")
                    url.set(findProperty("POM_LICENSE_URL") as String? ?: "https://opensource.org/licenses/MIT")
                    distribution.set(findProperty("POM_LICENSE_DIST") as String? ?: "repo")
                }
            }
            scm {
                url.set(findProperty("POM_SCM_URL") as String? ?: "https://github.com/pocketnest/pocketnest-android")
                connection.set(findProperty("POM_SCM_CONNECTION") as String? ?: "scm:git:git://github.com/pocketnest/pocketnest-android.git")
                developerConnection.set(findProperty("POM_SCM_DEV_CONNECTION") as String? ?: "scm:git:ssh://git@github.com/pocketnest/pocketnest-android.git")
            }
            developers {
                developer {
                    id.set(findProperty("POM_DEVELOPER_ID") as String? ?: "pocketnest")
                    name.set(findProperty("POM_DEVELOPER_NAME") as String? ?: "Pocketnest")
                }
            }
        }
    }

    publishing.repositories.maven {
        name = "CentralPortal"
        url = uri(
            if (version.toString().endsWith("SNAPSHOT"))
                "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            else
                "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
        )
        credentials {
            username = findProperty("centralUsername") as String? ?: System.getenv("CENTRAL_USERNAME")
            password = findProperty("centralPassword") as String? ?: System.getenv("CENTRAL_PASSWORD")
        }
    }

    signing {
        useInMemoryPgpKeys(
            findProperty("signingInMemoryKey") as String?,
            findProperty("signingInMemoryKeyPassword") as String?
        )
        sign(pub)
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.browser:browser:1.7.0")
    implementation("androidx.core:core-ktx:1.13.1")
}