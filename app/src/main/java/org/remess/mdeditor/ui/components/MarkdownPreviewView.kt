package org.remess.mdeditor.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.remess.mdeditor.util.MarkdownHtmlConverter

@Composable
fun MarkdownPreviewView(markdown: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                @Suppress("DEPRECATION")
                settings.allowFileAccess = true
                @Suppress("DEPRECATION")
                settings.allowUniversalAccessFromFileURLs = true
            }
        },
        update = { webView ->
            val html = MarkdownHtmlConverter.toHtml(markdown)
            // base URL = filesDir, чтобы relative пути images/xxx.jpg работали как file://
            val baseUrl = "file://${context.filesDir}/"
            webView.loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", null)
        }
    )
}
