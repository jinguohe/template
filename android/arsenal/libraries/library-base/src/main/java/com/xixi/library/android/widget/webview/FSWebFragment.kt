package com.xixi.library.android.widget.webview

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import com.xixi.library.R
import com.xixi.library.android.base.CXActivity
import com.xixi.library.android.base.CXBaseFragment
import com.xixi.library.android.widget.loading.CXFrameLoadingLayout
import kotlinx.android.synthetic.main.fs_fragment_webview.*

open class CXWebFragment : CXBaseFragment(), CXBaseFragment.OnBackPressedListener {
    companion object {
        protected var TAG = "HTML5"
        protected val KEY_URL = "key_url"
        protected val KEY_DATA = "key_data"
        protected val KEY_TITLE = "key_title"
        protected val KEY_HIDE_TITLE = "key_hide_title"

        //可外部静态配置
        var defaultTitleBarBgColor: Int = 0
        var defaultTitleBarBgRes: Int = R.drawable.fs_b_e2_white_normal_shape
        var defaultTitleBarTextColor = Color.parseColor("#333333")
        var defaultTitleBarTextSize = 16

        fun goToCompleteHttpUrl(activity: Activity, url: String) {
            goTo(activity, CXWebViewUtil.getCompleteHttpUrl(url), false)
        }

        fun goToCompleteHttpUrl(activity: Activity, url: String, title: String) {
            goTo(activity, CXWebViewUtil.getCompleteHttpUrl(url), title, null)
        }

        fun goToCompleteHttpUrl(activity: Activity, url: String, title: String, failureUrl: String) {
            goTo(activity, CXWebViewUtil.getCompleteHttpUrl(url), false, title, 0, 0, 0, 0, failureUrl)
        }

        fun goToCompleteHttpUrl(activity: Activity, url: String, isHideTitle: Boolean) {
            goTo(activity, CXWebViewUtil.getCompleteHttpUrl(url), isHideTitle, null)
        }

        fun goToCompleteHttpUrl(activity: Activity, url: String, isHideTitle: Boolean, title: String,
                                titleBarBgColor: Int,
                                titleBarBgRes: Int,
                                titleBarTextColor: Int,
                                titleBarTextSize: Int,
                                failureUrl: String
        ) {
            goTo(activity, CXWebViewUtil.getCompleteHttpUrl(url), isHideTitle, title, titleBarBgColor, titleBarBgRes, titleBarTextColor, titleBarTextSize, failureUrl)
        }

        fun goTo(activity: Activity, url: String, title: String, failureUrl: String? = null) {
            goTo(activity, url, false, title, 0, 0, 0, 0, failureUrl)
        }

        fun goTo(activity: Activity, url: String, isHideTitle: Boolean = false, failureUrl: String? = null) {
            goTo(activity, url, isHideTitle, null, 0, 0, 0, 0, failureUrl)
        }

        fun goTo(activity: Activity, url: String, isHideTitle: Boolean, title: String?,
                 titleBarBgColor: Int,
                 titleBarBgRes: Int,
                 titleBarTextColor: Int,
                 titleBarTextSize: Int,
                 failureUrl: String?
        ) {
            if (activity.isFinishing) {
                Log.e(TAG, "上下文无效")
                return
            }
            val bundle = Bundle()
            bundle.putString(KEY_URL, url)
            bundle.putBoolean(KEY_HIDE_TITLE, isHideTitle)
            bundle.putString(KEY_TITLE, title)
            bundle.putString("failureUrl", failureUrl)
            bundle.putInt("titleBarBgColor", titleBarBgColor)
            bundle.putInt("titleBarBgRes", titleBarBgRes)
            bundle.putInt("titleBarTextColor", titleBarTextColor)
            bundle.putInt("titleBarTextSize", titleBarTextSize)
            CXActivity.start(activity, CXWebFragment::class.java, bundle)
        }

        fun goToWithData(activity: Activity, htmlStr: String, isHideTitle: Boolean, title: String,
                         titleBarBgColor: Int,
                         titleBarBgRes: Int,
                         titleBarTextColor: Int,
                         titleBarTextSize: Int,
                         failureUrl: String
        ) {
            if (activity.isFinishing) {
                Log.e(TAG, "上下文无效")
                return
            }
            val bundle = Bundle()
            bundle.putString(KEY_DATA, htmlStr)
            bundle.putBoolean(KEY_HIDE_TITLE, isHideTitle)
            bundle.putString(KEY_TITLE, title)
            bundle.putString("failureUrl", failureUrl)
            bundle.putInt("titleBarBgColor", titleBarBgColor)
            bundle.putInt("titleBarBgRes", titleBarBgRes)
            bundle.putInt("titleBarTextColor", titleBarTextColor)
            bundle.putInt("titleBarTextSize", titleBarTextSize)
            CXActivity.start(activity, CXWebFragment::class.java, bundle)
        }
    }

    protected val SCHEME = "native"
    protected val HOST = "home"

    protected lateinit var mUrl: String
    protected lateinit var mHtmlData: String
    protected lateinit var mTitle: String
    protected var mFailureUrl: String? = null
    protected var mIsHideTitle = true

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutInflater.from(activity).inflate(R.layout.fs_fragment_webview, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //set color size start======================================================================
        var titleBarBgColor = 0
        var titleBarBgRes = 0
        var titleBarTextColor = 0
        var titleBarTextSize = 0
        val bundle = arguments
        if (bundle != null) {
            mUrl = bundle.getString(KEY_URL)
            mHtmlData = bundle.getString(KEY_DATA, "")
            mTitle = bundle.getString(KEY_TITLE)
            mFailureUrl = bundle.getString("failureUrl", null)
            mIsHideTitle = bundle.getBoolean(KEY_HIDE_TITLE, true)
            titleBarBgColor = bundle.getInt("titleBarBgColor", defaultTitleBarBgColor)
            titleBarBgRes = bundle.getInt("titleBarBgRes", defaultTitleBarBgRes)
            titleBarTextColor = bundle.getInt("titleBarTextColor", defaultTitleBarTextColor)
            titleBarTextSize = bundle.getInt("titleBarTextSize", defaultTitleBarTextSize)
        }
        if (titleBarBgColor <= 0) titleBarBgColor = defaultTitleBarBgColor
        if (titleBarBgRes <= 0) titleBarBgRes = defaultTitleBarBgRes
        if (titleBarTextColor <= 0) titleBarTextColor = defaultTitleBarTextColor
        if (titleBarTextSize <= 0) titleBarTextSize = defaultTitleBarTextSize
        titleBar.titleText.text = mTitle
        titleBar.visibility = if (mIsHideTitle) View.GONE else View.VISIBLE
        titleBar.titleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, titleBarTextSize.toFloat())
        titleBar.titleText.setTextColor(titleBarTextColor)
        titleBar.setBackgroundResource(titleBarBgRes)
        if (titleBarBgColor > 0)
            titleBar.setBackgroundColor(titleBarBgColor)
        //set color size end  ======================================================================

        initWebView(web_view)
        addJavascriptInterface(web_view)
        loadUrl(web_view, mUrl, mFailureUrl)
    }

    protected fun initWebView(webView: WebView) {
        CXWebViewUtil.initWebView(webView)

        webView.setWebViewClient(object : WebViewClient() {

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                view.stopLoading()
                if (TextUtils.isEmpty(mFailureUrl)) {
                    webView.goBack()
                    loading_view.showView(CXFrameLoadingLayout.ViewType.NETWORK_EXCEPTION, if (error == null) loading_view.getDefaultText(CXFrameLoadingLayout.ViewType.NETWORK_EXCEPTION) else "error", false, true)
                } else {
                    view.loadUrl(mFailureUrl)
                }
            }

            @Suppress("DEPRECATION", "OverridingDeprecatedMember")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                Log.d(TAG, "shouldOverrideUrlLoading:" + url)
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
                super.onPageStarted(view, url, favicon)
                Log.d(TAG, "onPageStarted:" + url)
                loading_view.showView(CXFrameLoadingLayout.ViewType.LOADING)
            }

            override fun onPageFinished(view: WebView, url: String) {
                Log.d(TAG, "onPageFinished:" + url)
                loading_view.hideAll()
            }
        })

        webView.setWebChromeClient(object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                Log.d(TAG, "onProgressChanged:" + newProgress)
                loading_view.updateText(CXFrameLoadingLayout.ViewType.LOADING, loading_view.getDefaultText(CXFrameLoadingLayout.ViewType.NODATA) + " " + newProgress + "%", false, true)
            }

            @Suppress("OverridingDeprecatedMember")
            override fun onConsoleMessage(message: String, lineNumber: Int, sourceID: String) {
                Log.w(TAG, "#$lineNumber:$sourceID")
                Log.d(TAG, message)
            }

            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
                Log.d(TAG, "onReceivedTitle:" + title)
                if (!mIsHideTitle && TextUtils.isEmpty(mTitle)) {
                    titleBar.titleText.text = mTitle
                }
            }

            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                Log.d(TAG, "onJsAlert:" + message)
                return super.onJsAlert(view, url, message, result)
            }
        })
    }

    @Suppress("unused_parameter")
    protected fun addJavascriptInterface(webview: WebView) {
    }

    protected fun loadUrl(webView: WebView, url: String, failureUrl: String?) {
        if (!URLUtil.isValidUrl(url)) {
            if (URLUtil.isValidUrl(failureUrl))
                webView.loadUrl(failureUrl)
            else
                loading_view.showView(CXFrameLoadingLayout.ViewType.NETWORK_EXCEPTION, "加载失败,Url不正确:\n" + url, false, true)
        } else {
            loading_view.setOnRefreshClickListener(View.OnClickListener { webView.loadUrl(url) })
            if (!TextUtils.isEmpty(mHtmlData)) {
                webView.loadDataWithBaseURL(null, mHtmlData, "text/html", "utf-8", null)
            } else
                webView.loadUrl(url)
        }
    }

    override fun onBackPressed(): Boolean {
        if (web_view.canGoBack()) {
            web_view.goBack()
            return true
        }
        return false
    }
}