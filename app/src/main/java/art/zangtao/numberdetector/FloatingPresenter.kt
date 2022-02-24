package art.zangtao.numberdetector

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class FloatingPresenter(private val context: Context) {
    private var view: View? = null
    private val wm get() = (context.applicationContext?.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)

    fun present(number: String) {
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
        wm?.addView(LayoutInflater.from(context).inflate(R.layout.popup_dialog, null).apply {
            findViewById<WebView>(R.id.webview).apply {
                settings.apply {
                    javaScriptEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        view?.loadUrl(request?.url.toString())
                        return super.shouldOverrideUrlLoading(view, request)
                    }
                }
                loadUrl("https://haoma.baidu.com/phoneSearch?search=$number")
            }
            view = this
        }, lp)
    }

    fun dismiss() {
        view?.let { wm?.removeView(it) }
        view = null
    }

    companion object {
        @Volatile
        private var INSTANCE: FloatingPresenter? = null

        fun getInstance(context: Context): FloatingPresenter =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: FloatingPresenter(context).also { INSTANCE = it }
            }
    }
}