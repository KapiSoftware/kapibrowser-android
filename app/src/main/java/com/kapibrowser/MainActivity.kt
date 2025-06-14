package com.kapibrowser

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.webkit.DownloadListener
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout

class MainActivity : Activity() {

    private lateinit var urlEditText: EditText
    private lateinit var goButton: Button
    private lateinit var backButton: Button
    private lateinit var forwardButton: Button
    private lateinit var newTabButton: Button
    private lateinit var tabsContainer: LinearLayout
    private lateinit var webViewContainer: FrameLayout

    private val webViews = mutableListOf<WebView>()
    private val tabButtons = mutableListOf<Button>()
    private var currentTabIndex = 0

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        urlEditText = findViewById(R.id.urlEditText)
        goButton = findViewById(R.id.goButton)
        backButton = findViewById(R.id.backButton)
        forwardButton = findViewById(R.id.forwardButton)
        newTabButton = findViewById(R.id.newTabButton)
        tabsContainer = findViewById(R.id.tabsContainer)
        webViewContainer = findViewById(R.id.webViewContainer)

        addNewTab()

        goButton.setOnClickListener {
            val url = fixUrl(urlEditText.text.toString())
            currentWebView().loadUrl(url)
        }

        backButton.setOnClickListener {
            val webView = currentWebView()
            if (webView.canGoBack()) webView.goBack()
        }

        forwardButton.setOnClickListener {
            val webView = currentWebView()
            if (webView.canGoForward()) webView.goForward()
        }

        newTabButton.setOnClickListener {
            addNewTab()
        }
    }

    private fun currentWebView(): WebView = webViews[currentTabIndex]

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(): WebView {
        val webView = WebView(this)
        webView.webViewClient = WebViewClient()
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true

        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimeType)
            request.addRequestHeader("User-Agent", userAgent)
            request.setDescription("Downloading file...")
            request.setTitle(url.substringAfterLast('/'))
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.substringAfterLast('/'))

            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
        }

        return webView
    }

    private fun addNewTab() {
        val newWebView = createWebView()
        webViews.add(newWebView)

        // Add WebView to container (but only show current)
        webViewContainer.addView(newWebView)
        updateWebViewVisibility()

        // Create tab button
        val tabButton = Button(this).apply {
            text = "Tab ${webViews.size}"
            setOnClickListener {
                val index = tabButtons.indexOf(this)
                if (index != -1) switchToTab(index)
            }
            // Optional: style your buttons here, e.g. margin/padding
        }
        tabButtons.add(tabButton)
        tabsContainer.addView(tabButton)

        switchToTab(webViews.size - 1)
        urlEditText.setText("")
    }

    private fun switchToTab(index: Int) {
        if (index !in webViews.indices) return
        currentTabIndex = index
        updateWebViewVisibility()
        urlEditText.setText(currentWebView().url ?: "")
        updateTabButtons()
    }

    private fun updateWebViewVisibility() {
        for (i in webViews.indices) {
            webViews[i].visibility = if (i == currentTabIndex) View.VISIBLE else View.GONE
        }
    }

    private fun updateTabButtons() {
        for (i in tabButtons.indices) {
            val btn = tabButtons[i]
            btn.isEnabled = i != currentTabIndex
            // Optional: change button background/color here to highlight current tab
        }
    }

    override fun onBackPressed() {
        val webView = currentWebView()
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun fixUrl(url: String): String {
        var fixedUrl = url.trim()
        if (!fixedUrl.startsWith("http://") && !fixedUrl.startsWith("https://")) {
            fixedUrl = "https://$fixedUrl"
        }
        return fixedUrl
    }
}
