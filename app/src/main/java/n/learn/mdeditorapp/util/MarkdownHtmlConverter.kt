package n.learn.mdeditorapp.util

object MarkdownHtmlConverter {

    fun toHtml(markdown: String): String {
        val escaped = markdown
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <script src="https://cdn.jsdelivr.net/npm/markdown-it@14/dist/markdown-it.min.js"></script>
                <script>
                    window.MathJax = {
                        tex: { inlineMath: [['${'$'}', '${'$'}'], ['\\(', '\\)']], displayMath: [['$$', '$$'], ['\\[', '\\]']] },
                        svg: { fontCache: 'global' }
                    };
                </script>
                <script src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-svg.js"></script>
                <style>
                    body { font-family: sans-serif; padding: 16px; line-height: 1.6; background: #1e1e1e; color: #ddd; }
                    code { background: #333; padding: 2px 4px; border-radius: 4px; }
                    pre { background: #333; padding: 12px; border-radius: 6px; overflow-x: auto; }
                    blockquote { border-left: 4px solid #555; margin: 0; padding-left: 16px; color: #aaa; }
                    img { max-width: 100%; }
                    h1, h2, h3 { color: #fff; }
                    a { color: #4fc3f7; }
                </style>
            </head>
            <body>
                <div id="content"></div>
                <script>
                    var md = window.markdownit({ html: false, linkify: true, typographer: true });
                    var raw = atob("${java.util.Base64.getEncoder().encodeToString(markdown.toByteArray())}");
                    document.getElementById('content').innerHTML = md.render(raw);
                    if (window.MathJax) MathJax.typeset();
                </script>
            </body>
            </html>
        """.trimIndent()
    }
}
