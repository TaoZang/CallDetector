package art.zangtao.numberdetector

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.telecom.Call
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.TextView
import androidx.cardview.widget.CardView
import org.jsoup.Jsoup

data class CallInfo(
    val type: Int,
    val number: String? = null,
    val title: String? = null,
    val location: String? = null
)

object CallType {
    const val Unknown = 0
    const val Normal = 1
    const val Common = 2
    const val Reported = 3
    const val ReportedAsScam = 4
}

class MyApplication: Application() {
    private var presentingView: View? = null
    private val wm get() = getSystemService(Context.WINDOW_SERVICE) as? WindowManager

    private val jsHook = """
        const root = document.getElementsByTagName('html')[0];
        new MutationObserver(function() {
            window.HtmlHandler.handleHtml('<html>' + root.innerHTML + '<html>');
        }).observe(root, {subtree: true, childList: true});
    """.trimIndent()

    inner class JsBridge {
        @JavascriptInterface
        fun handleHtml(html: String) {
            val root = Jsoup.parse(html).getElementsByClass("info-container").firstOrNull()
            if (root != null) {
                val commonElement = root.getElementsByClass("comp-custom").firstOrNull()
                val normalElement = root.getElementsByClass("comp-location").firstOrNull()
                val reportElement = root.getElementsByClass("comp-report").firstOrNull()
                when {
                    commonElement != null -> {
                        val title = root.getElementsByClass("title").firstOrNull()?.text()
                        showFloatingView(CallInfo(CallType.Common, title = title))
                    }
                    normalElement != null -> {
                        val title = root.getElementsByClass("location-name").firstOrNull()?.text() ?: ""
                        val location = root.getElementsByClass("location").firstOrNull()?.text()
                        showFloatingView(CallInfo(CallType.Normal, title = title, location = location?.replace(title, "")?.trim()))
                    }
                    reportElement != null -> {
                        val title = root.getElementsByClass("report-name").firstOrNull()?.text()
                        val location = root.getElementsByClass("location").firstOrNull()?.text()
                        val type = if (root.getElementsByClass("reverse-report-type").isNotEmpty()) CallType.ReportedAsScam else CallType.Reported
                        showFloatingView(CallInfo(type, title = title, location = location))
                    }
                }
            }
        }
    }

    fun handleIncoming(number: String) {
        WebView(this).apply {
            settings.apply {
                javaScriptEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
            }
            addJavascriptInterface(JsBridge(), "HtmlHandler")
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    view?.loadUrl(request?.url.toString())
                    return super.shouldOverrideUrlLoading(view, request)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    evaluateJavascript(jsHook, null)
                }
            }
        }.loadUrl("https://haoma.baidu.com/phoneSearch?search=$number")
    }

    fun handleIdle() {
        presentingView?.let {
            wm?.removeView(it)
            presentingView = null
        }
    }

    private fun showFloatingView(callInfo: CallInfo) {
        val view = LayoutInflater.from(this).inflate(R.layout.popup_window, null).apply {
            val cardView = findViewById<CardView>(R.id.card_view)
            val titleText = findViewById<TextView>(R.id.title)
            val locationText = findViewById<TextView>(R.id.location)

            titleText.text = callInfo.title
            locationText.text = callInfo.location
            when (callInfo.type) {
                CallType.Normal, CallType.Common -> {
                    cardView.setCardBackgroundColor(0xFF66BB6A.toInt())
                }
                CallType.Reported -> {
                    cardView.setCardBackgroundColor(0xFFFCB614.toInt())
                }
                CallType.ReportedAsScam -> {
                    cardView.setCardBackgroundColor(0xFFEF5350.toInt())
                }
            }
        }
        val lp = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            gravity = Gravity.CENTER
            format = PixelFormat.RGBA_8888
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }

        presentingView?.let { wm?.removeView(it) }
        wm?.addView(view, lp)
        presentingView = view
    }
}