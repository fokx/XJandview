package app.xjtu.xjandview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.xjtu.xjandview.ui.theme.XJandviewTheme
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import java.io.InputStream
import java.net.HttpURLConnection

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    companion object {
        private const val TARGET_URL = "https://myip.xjtu.app"
        private const val PROXY_ADDRESS = "127.0.0.1"
        private const val PROXY_PORT = 2080
//        private const val PROXY_PORT = 4801
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        configureWebView()
    }

    private fun configureWebView() {
        // Enable JavaScript
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true

        // Set custom WebViewClient to handle proxy
        webView.webViewClient = ProxyWebViewClient()

        // Load the target URL
        webView.loadUrl(TARGET_URL)
    }

    private inner class ProxyWebViewClient : WebViewClient() {
        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            return try {
                val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(PROXY_ADDRESS, PROXY_PORT))
                val url = URL(request.url.toString())
                val connection = url.openConnection(proxy) as HttpURLConnection

                connection.requestMethod = request.method
                request.requestHeaders.forEach { (key, value) ->
                    connection.setRequestProperty(key, value)
                }

                connection.connect()
                val inputStream = connection.inputStream

                WebResourceResponse(
                    connection.contentType ?: "text/html",
                    connection.contentEncoding ?: "utf-8",
                    inputStream
                )
            } catch (e: Exception) {
                e.printStackTrace()
                super.shouldInterceptRequest(view, request)
            }
        }
    }
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
