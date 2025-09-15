package com.example.sdkexample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.pocketnest.sdk.WebViewActivity
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    private lateinit var logTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logTextView = findViewById(R.id.logTextView)

        findViewById<Button>(R.id.launchSdkButton).setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            startActivity(intent)
        }

        // Handle deep link if app was cold-started
        intent?.data?.let { handleDeepLink(it) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { handleDeepLink(it) }
    }

    private fun handleDeepLink(uri: Uri) {
        appendLog("Received deep link: $uri")

        val params = mutableMapOf<String, String>()
        uri.queryParameterNames.forEach { name ->
            params[name] = uri.getQueryParameter(name) ?: ""
        }

        val payload = JSONObject().apply {
            put("status", "success")
            put("callbackURL", uri.toString())
            put("params", JSONObject(params as Map<*, *>))
        }

        appendLog("Parsed payload: $payload")
    }

    private fun appendLog(message: String) {
        runOnUiThread {
            logTextView.append("$message\n")
        }
    }
}
