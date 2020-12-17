package com.example.android.newsapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient

class DetailActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        webView = findViewById<WebView>(R.id.web_view)
        val url = intent.getStringExtra(EXTRA_URL)
        webView.webViewClient = WebViewClient()
        url?.let {
            webView.loadUrl(url!!)
        }

    }

    override fun onBackPressed() {

        if(webView.canGoBack()){
            webView.goBack()
        }else{
            super.onBackPressed()
        }
    }
}