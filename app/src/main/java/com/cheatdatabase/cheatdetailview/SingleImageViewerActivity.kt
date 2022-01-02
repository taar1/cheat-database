package com.cheatdatabase.cheatdetailview

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.cheatdatabase.databinding.WebviewWithToolbarBinding


class SingleImageViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = WebviewWithToolbarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uri: Uri = Uri.parse(intent.getStringExtra("image_full_path"))
        binding.webview.webViewClient = WebViewClient()
        binding.webview.loadUrl(uri.toString())

        binding.webview.settings.builtInZoomControls = true
        binding.webview.settings.loadWithOverviewMode = true
        binding.webview.settings.useWideViewPort = true

        setSupportActionBar(binding.includeToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        title = intent.getStringExtra("cheat_title")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}