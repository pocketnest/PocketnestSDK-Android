# PocketnestSDK (Android)

![PocketnestSDK](https://img.shields.io/badge/PocketnestSDK-1.0.0-success)

Android SDK for Pocketnest.

## Installation

Add the JitPack repository to your build file:

- **For Gradle 7.0+ (using `dependencyResolutionManagement` in `settings.gradle`):**

```groovy
	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
``` 


Add the SDK to your **app module** by including the following inside the `dependencies` block of your `app/build.gradle` file:


```groovy
dependencies {
    implementation "com.github.pocketnest:PocketnestSDK-Android:1.0.0"
}
```

---

## Usage

To integrate the PocketnestSDK, configure your **redirect URI**.

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

### Step 2. Get SSO web view

In your Activity/Fragment:

```kotlin
import org.pocketnest.sdk.PocketnestSDK

PocketnestSDK.webView(
    activity = this, // or requireActivity() in Fragment
    url = "https://mywebsite.com/sso",     // provided by Pocketnest (prod or preprod)
    accessToken = "myaccesstoken",         // user to be logged in automatically (session)
    redirectUri = "myssoredirect", // must match manifest placeholder pocketnestScheme from step 1
    onSuccess = {
        // Handle SDK webview opened successfully
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
