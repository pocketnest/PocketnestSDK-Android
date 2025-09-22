# PocketnestSDK (Android)

Android SDK for Pocketnest.

## Installation

Once published, you can add **PocketnestSDK** to your project via **Maven Central**.

### Gradle (Kotlin DSL)

In your root `settings.gradle.kts` make sure you have:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
```

Then add the SDK to your **app module**:

```kotlin
dependencies {
    implementation("org.pocketnest:pocketnest-sdk:1.0.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation "org.pocketnest:pocketnest-sdk:1.0.0"
}
```

---

## Usage

To integrate the PocketnestSDK, configure your **redirect URI** and call the SDK’s launcher.

### Step 1. Configure redirect URI


In your `app/build.gradle`:

```groovy
android {
    defaultConfig {
        manifestPlaceholders = [
            pocketnestScheme: "myssoredirect",  // can be any string
            pocketnestHost: "hosted-link-complete"
        ]
    }
}
```

---

### Step 2. Launch the SDK

In your Activity/Fragment:

```kotlin
import org.pocketnest.sdk.PocketnestSDK

PocketnestSDK.webView(
    activity = this, // or requireActivity() in Fragment
    url = "https://mywebsite.com/sso",     // provided by Pocketnest (prod or preprod)
    redirectUri = "myssoredirect", // must match manifest placeholders from step 1
    onSuccess = { _ ->
        // Handle success: SDK finished with result map
    },
    onExit = {
        // Handle user exit/cancel
    }
)
```

Function webView returns view component that can be used to show Pocketnest SSO screen.

---

## Example Project

Check the sample `app` module in this repository for a working integration.

---

## Notes

- Requires **minSdk 24** and **targetSdk 34** or higher.  
- SDK uses a WebView/Custom Tabs to handle Pocketnest SSO securely.  
- Make sure your `redirectUri` matches exactly (case-sensitive).
