package n.learn.mdeditorapp.ui.components

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import n.learn.mdeditorapp.util.MarkdownHtmlConverter
import java.io.File

@Composable
fun MarkdownPreviewView(markdown: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val filesDir = context.filesDir.absolutePath

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = object : WebViewClient() {
                    // разрешаем доступ к локальным файлам через file:// в img src
                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest
                    ): WebResourceResponse? {
                        val uri = request.url
                        if (uri.scheme == "file") {
                            val path = uri.path ?: return null
                            val file = File(path)
                            if (file.exists()) {
                                val mime = when {
                                    path.endsWith(".jpg") || path.endsWith(".jpeg") -> "image/jpeg"
                                    path.endsWith(".png") -> "image/png"
                                    else -> "application/octet-stream"
                                }
                                return WebResourceResponse(mime, "UTF-8", file.inputStream())
                            }
                        }
                        return null
                    }
                }
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                @Suppress("DEPRECATION")
                settings.allowFileAccess = true
                @Suppress("DEPRECATION")
                settings.allowUniversalAccessFromFileURLs = true
            }
        },
        update = { webView ->
            val html = MarkdownHtmlConverter.toHtml(markdown, filesDir)
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
