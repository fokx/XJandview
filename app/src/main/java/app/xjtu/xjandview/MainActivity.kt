package app.xjtu.xjandview

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import android.view.WindowInsets
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.WindowInsetsCompat
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    companion object {
        private const val TARGET_URL = "https://bgp.he.net"
        private const val PROXY_ADDRESS = "127.0.0.1"
        private const val PROXY_PORT = 2080
//        private const val PROXY_PORT = 4801
    }
    fun setProxyForWebView(context: Context, proxyAddress: String, proxyPort: Int) {
        val proxyHost = proxyAddress
        val proxyHostPort = proxyPort

        // Setting proxy via system properties
        System.setProperty("http.proxyHost", proxyHost)
        System.setProperty("http.proxyPort", proxyHostPort.toString())
        System.setProperty("https.proxyHost", proxyHost)
        System.setProperty("https.proxyPort", proxyHostPort.toString())
    }
    fun setProxySelector(proxyAddress: String, proxyPort: Int) {
        val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(proxyAddress, proxyPort))
        ProxySelector.setDefault(object : ProxySelector() {
            override fun select(uri: URI?): List<Proxy> {
                // Apply proxy only for HTTP/HTTPS schemes
                return if (uri?.scheme == "http" || uri?.scheme == "https") {
                    listOf(proxy)
                } else {
                    listOf(Proxy.NO_PROXY)
                }
            }

            override fun connectFailed(uri: URI?, socketAddress: SocketAddress?, ioe: IOException?) {
                // Log the failure if needed
                ioe?.printStackTrace()
            }
        })
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Make sure the app layout respects edge-to-edge displays
        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // For Android R and above
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                // Adjust padding to compensate for system bars (status + navigation bars)
                view.setPadding(
                    insets.left,
                    insets.top,
                    insets.right,
                    insets.bottom
                )
            } else {
                // For Android Q and below (using deprecated fields as fallback)
                val insets = WindowInsetsCompat.toWindowInsetsCompat(windowInsets).systemGestureInsets
                view.setPadding(
                    insets.left,
                    insets.top,
                    insets.right,
                    insets.bottom
                )
            }

            windowInsets
        }

        setContentView(R.layout.activity_main)

        setProxySelector(PROXY_ADDRESS, PROXY_PORT)
//        setProxyForWebView(this, PROXY_ADDRESS, PROXY_PORT)
        openCustomTabWithFallback(TARGET_URL)

//        webView = findViewById(R.id.webview)
//        configureWebView()
//        webView.loadUrl(TARGET_URL)
    }
    private fun openCustomTabWithFallback(url: String) {
        // Create a CustomTabsIntent Builder to configure settings
        val customTabsIntentBuilder = CustomTabsIntent.Builder()
        try {
            // Optional: Set toolbar color, animations, etc.
//            customTabsIntentBuilder.setToolbarColor(resources.getColor(android.R.color.holo_blue_bright, null))
            customTabsIntentBuilder.setShowTitle(true)
            customTabsIntentBuilder.setExitAnimations(this, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            // Build the CustomTabsIntent object
            val customTabsIntent = customTabsIntentBuilder.build()

            // Launch the URL
            customTabsIntent.launchUrl(this, Uri.parse(url))
        } catch (e: Exception) {
            // Fallback: Open in default external browser if Custom Tabs is unavailable
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)

        }


    }


    private fun configureWebView() {
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
//        webView.webViewClient = WebViewClient()
//        webView.webViewClient = ProxyWebViewClient()
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
//        override fun onPageFinished(view: WebView?, url: String?) {
//            super.onPageFinished(view, url)
//            view?.evaluateJavascript("document.body.innerHTML;") { content ->
//                // Debug: You can log the content to check if JS-rendered content is present
//                println("Rendered Content: $content")
//            }
//        }

    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
