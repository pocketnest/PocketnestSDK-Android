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
    implementation 'com.github.pocketnest:PocketnestSDK-Android:2.0.1'
}
```

---

## Usage

To integrate the PocketnestSDK, configure your **redirect URI**.

### Step 1. Configure redirect URI _REQUIRED_

In your `app/build.gradle`:

```groovy
android {
    defaultConfig {
        manifestPlaceholders = [
            pocketnestScheme: "pocketnestredirecturi",  //Required: (If you require unique URL scheme, contact us.) This is required to handle the redirect back from from plaid app to app linking like Chase.
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
    accessToken = "myaccesstoken",         // Optional: user to be logged in automatically (session)
    redirectUri = "pocketnestredirecturi", // Optional: This is only required if you want to use your own unique URL scheme and should match those you set in the manifestPlaceholders, otherwise it will be automatically set to default value
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
    accessToken = "myaccesstoken", //Optional:  user to be logged in automatically (session)
    redirectUri = "pocketnestredirecturi", // Optional: This is only required if you want to use your own unique URL scheme and should match those you set in the manifestPlaceholders, otherwise it will be automatically set to default value
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
