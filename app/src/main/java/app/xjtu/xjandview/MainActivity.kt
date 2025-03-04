package app.xjtu.xjandview

import android.annotation.TargetApi
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import java.lang.reflect.Method

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView: WebView = findViewById(R.id.webview)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                view?.loadUrl(url)
                return true
            }
        }

        setProxy(webView, "127.0.0.1", 4848)
        webView.loadUrl("https://myip.xjtu.app")
    }

    private fun setProxy(webView: WebView, host: String, port: Int) {
        try {
            val webViewClass = Class.forName("android.webkit.WebView")
            val method: Method = webViewClass.getDeclaredMethod("getWebViewCore")
            method.isAccessible = true
            val webViewCore = method.invoke(webView)
            val webViewCoreClass = Class.forName("android.webkit.WebViewCore")
            val mBrowserFrameField = webViewCoreClass.getDeclaredField("mBrowserFrame")
            mBrowserFrameField.isAccessible = true
            val mBrowserFrame = mBrowserFrameField.get(webViewCore)
            val browserFrameClass = Class.forName("android.webkit.BrowserFrame")
            val sJavaBridgeField = browserFrameClass.getDeclaredField("sJavaBridge")
            sJavaBridgeField.isAccessible = true
            val sJavaBridge = sJavaBridgeField.get(mBrowserFrame)
            val sJavaBridgeClass = Class.forName("android.net.ProxyProperties")
            val constructor = sJavaBridgeClass.getConstructor(String::class.java, Int::class.javaPrimitiveType, String::class.java)
            val proxyProperties = constructor.newInstance(host, port, null)
            val setProxyMethod = sJavaBridgeClass.getDeclaredMethod("setProxy", sJavaBridgeClass)
            setProxyMethod.isAccessible = true
            setProxyMethod.invoke(sJavaBridge, proxyProperties)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}