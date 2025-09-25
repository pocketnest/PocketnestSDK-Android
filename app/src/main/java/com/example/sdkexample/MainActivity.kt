package com.example.sdkexample

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import org.pocketnest.sdk.PocketnestSDK

private const val REDIRECT_SCHEME = "pocketnesthostedlink"
private const val BASE_URL = "https://pocketnest-preprod.netlify.app"

class MainActivity : ComponentActivity() {

    private lateinit var logTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logTextView = findViewById(R.id.logTextView)

        findViewById<Button>(R.id.launchSdkButton).setOnClickListener {
            PocketnestSDK.webView(
                activity = this,
                url = BASE_URL,
                accessToken = "",
                redirectUri = REDIRECT_SCHEME,
                onSuccess = {
                    appendLog("✅ Success")
                },
                onExit = {
                    appendLog("🚪 User exited SDK")
                }
            )
        }
    }

    private fun appendLog(message: String) {
        runOnUiThread { logTextView.append("$message\n") }
    }
}