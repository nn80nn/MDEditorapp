package n.learn.mdeditorapp.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import n.learn.mdeditorapp.util.MarkdownHtmlConverter

@Composable
fun MarkdownPreviewView(markdown: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
            }
        },
        update = { webView ->
            val html = MarkdownHtmlConverter.toHtml(markdown)
            webView.loadDataWithBaseURL(
                "https://cdn.jsdelivr.net",
                html,
                "text/html",
                "UTF-8",
                null
            )
        }
    )
}
