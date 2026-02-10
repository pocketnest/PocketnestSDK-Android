package com.example.sdkexample

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import org.pocketnest.sdk.PocketnestSDK
import androidx.appcompat.app.AppCompatActivity


private const val BASE_URL = "https://pocketnest.app"

class MainActivity : AppCompatActivity() {

    private lateinit var logTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logTextView = findViewById(R.id.logTextView)

        findViewById<Button>(R.id.launchSdkButton).setOnClickListener {
            PocketnestSDK.webView(
                activity = this,
                url = BASE_URL,
                onSuccess = {
                    appendLog("✅ Success")
                },
                onExit = {
                    appendLog("🚪 User exited SDK")
                }
            )
        }


        findViewById<Button>(R.id.launchSdkFragmentButton).setOnClickListener {
            val frag = PocketnestSDK.newWebViewFragment(
                url = BASE_URL,
                onSuccess = { appendLog("✅ Success (Fragment)") },
                onExit = { appendLog("🚪 Exit (Fragment)") }
            )
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, frag, "Pocketnest")
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
//        (supportFragmentManager.findFragmentByTag("Pocketnest") as? org.pocketnest.sdk.PocketnestWebViewFragment)
//            ?.handleDeepLinkFromHost(intent.data ?: return)
    }

    private fun appendLog(message: String) {
        runOnUiThread { logTextView.append("$message\n") }
    }
}