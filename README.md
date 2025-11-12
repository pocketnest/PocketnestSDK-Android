# PocketnestSDK (Android)


[![](https://jitpack.io/v/pocketnest/PocketnestSDK-Android.svg)](https://jitpack.io/#pocketnest/PocketnestSDK-Android)

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
    implementation 'com.github.pocketnest:PocketnestSDK-Android:1.0.4'
}
```

---

## Usage

To integrate the PocketnestSDK, configure your **redirect URI**.

### Step 1. Configure redirect URI *REQUIRED*


In your `app/build.gradle`:

```groovy
android {
    defaultConfig {
        manifestPlaceholders = [
            pocketnestScheme: "myssoredirect",  // can be any string and it is required
        ]
    }
}
```

---

### Step 2. Launch the SDK

You can use the SDK in two modes depending on your integration needs:

To get `url` and `accessToken` you need to check `Pocketnest SSO Partner Procedures` documentation.

### Mode 1: Activity-based (standalone screen)

This launches the Pocketnest SSO in a dedicated Activity managed by the SDK.

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

### Mode 2: Fragment-based (embedded)

This embeds the Pocketnest SSO inside your own Fragment container.
Great for apps with a single-activity architecture or custom navigation stacks.

```kotlin
import org.pocketnest.sdk.PocketnestSDK

val fragment = PocketnestSDK.newWebViewFragment(
    url = "https://mywebsite.com/sso",  // provided by Pocketnest (prod or preprod)
    redirectUri = "myssoredirect",  // must match manifest placeholder pocketnestScheme from step 1
    accessToken = "myaccesstoken", // user to be logged in automatically (session)
    onSuccess = { 
        // Called when SDK webview is presented
    },
    onExit = {
        // Called when user exits/cancels
    }
)

// Attach it to your container
supportFragmentManager.beginTransaction()
    .replace(R.id.container, fragment, "Pocketnest")
    .addToBackStack(null)
    .commit()
```

👉 Use this when you want the SDK’s UI embedded in your own flow.

---

## Example Project

Check the sample `app` module in this repository for a working integration that demonstrates both Activity and Fragment modes.

---

## Notes

- Requires **minSdk 24** and **targetSdk 34** or higher.  
- SDK uses a WebView/Custom Tabs to handle Pocketnest SSO securely.  
- Make sure your `redirectUri` matches exactly (case-sensitive).
