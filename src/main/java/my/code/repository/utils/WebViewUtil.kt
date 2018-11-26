@file:JvmName("WebViewUtil")

package my.code.repository.utils

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.service.quicksettings.Tile
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.annotations.Until
import java.io.File

/**
 * @author djh on  2018/10/7 18:27
 * @E-Mail 1544579459@qq.com
 */
class WebViewManger(private val webView: WebView) {
    
    /**
     * WebViewClient method.
     */
    private var shouldOverrideUrlLoading:
            ((view: WebView?, request: WebResourceRequest?) -> Unit)? = null
    
    private var onPageStarted:
            ((view: WebView?, url: String?, favicon: Bitmap?) -> Unit)? = null
    
    private var onPageFinished:
            ((view: WebView?, url: String?) -> Unit)? = null
    
    
    /**
     * WebChromeClient method.
     */
    private var onReceivedTitle:
            ((view: WebView?, title: String?) -> Unit)? = null
    
    private var onProgressChanged:
            ((view: WebView?, newProgress: Int) -> Unit)? = null
    
    /**
     * Setting the webView.
     */
    @JvmOverloads
    @SuppressLint("SetJavaScriptEnabled")
    fun settingWebView(customSettings: ((webSetting: WebSettings) -> Unit)? = null): WebViewManger {
        val webSettings = webView.settings
        
        with(webSettings) {
            
            // Adapt to the screen.
            useWideViewPort = true
            loadWithOverviewMode = true
            
            // Setting the zoom.
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = true
            
            // Cache setting.
            this.cacheMode = cacheMode
            val cacheDir = File(MyApplication.getContext().cacheDir, "web_view_cache")
            cacheDir.mkdir()
            setAppCacheEnabled(true)
            setAppCachePath(cacheDir.path)
            
            // Allow http and https mixed page.
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            
            // Other setting.
            allowFileAccess = true
            javaScriptEnabled = true
            loadsImagesAutomatically = true
            defaultTextEncodingName = "utf-8"
            javaScriptCanOpenWindowsAutomatically = true

//            domStorageEnabled = true
            
            
            // If client set the custom setting, last set it,
            // this will cover the default setting.
            customSettings?.invoke(webSettings)
        }
        return this
    }
    
    /**
     * Set the WebViewClient call back.
     */
    fun shouldOverrideUrlLoading(action: ((webView: WebView?, request: WebResourceRequest?) -> Unit)?)
            : WebViewManger {
        shouldOverrideUrlLoading = action
        return this
    }
    
    fun onPageStarted(action: ((webView: WebView?, url: String?, favicon: Bitmap?) -> Unit)?)
            : WebViewManger {
        onPageStarted = action
        return this
    }
    
    fun onPageFinished(action: ((webView: WebView?, url: String?) -> Unit)?)
            : WebViewManger {
        onPageFinished = action
        return this
    }
    
    
    /**
     * Set the WebChromeClient call back.
     */
    fun onReceivedTitle(action: ((webView: WebView?, title: String?) -> Unit)?)
            : WebViewManger {
        onReceivedTitle = action
        return this
    }
    
    fun onProgressChanged(action: ((webView: WebView?, newProgress: Int) -> Unit)?)
            : WebViewManger {
        onProgressChanged = action
        return this
    }
    
    
    /**
     * Install all call back, this must be call at last!
     */
    fun install(): WebViewManger {
        
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                shouldOverrideUrlLoading?.invoke(view, request)
                return false
            }
            
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                onPageStarted?.invoke(view, url, favicon)
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                onPageFinished?.invoke(view, url)
            }
        }
        
        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                onReceivedTitle?.invoke(view, title)
            }
            
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                onProgressChanged?.invoke(view, newProgress)
            }
        }
        return this
    }
    
    fun go(url: String) {
        webView.loadUrl(url)
    }
}

/**
 * Use this extend method to start stream call.
 */
fun WebView.prepare() = WebViewManger(this)

/**
 * Call this at activity destroy.
 */
fun WebView.destroyWebView() {
    clearHistory()
    loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
    (this.parent as ViewGroup).removeView(this)
    destroy()
}

fun WebView.onBackKeyDown(keyCode: Int): Boolean {
    if (keyCode == KeyEvent.KEYCODE_BACK && canGoBack()) {
        goBack()
        return true
    }
    return false
}