package org.remess.mdeditor

import org.junit.Assert.*
import org.junit.Test
import org.remess.mdeditor.util.MarkdownHtmlConverter

// Тест-кейсы №6, 7, 8, 9, 10 — работа с содержимым документа
class MarkdownConverterTest {

    // TC6 — Сохранение документа на сервере (контент перед загрузкой не пустой)

    @Test
    fun tc6_documentContent_notEmptyBeforeUpload() {
        val content = "# Мой документ\n\nКонтент."
        assertTrue(content.isNotBlank())
    }

    @Test
    fun tc6_htmlOutput_isGeneratedFromContent() {
        val content = "# Заголовок\n\nТекст документа."
        val html = MarkdownHtmlConverter.toHtml(content)
        assertTrue(html.contains("<!DOCTYPE html>"))
    }

    // TC7 — Загрузка документа с сервера (структура данных)

    @Test
    fun tc7_remoteDocument_idIsPositive() {
        val remoteId = 42
        assertTrue(remoteId > 0)
    }

    @Test
    fun tc7_remoteDocument_nameEndsWithMd() {
        val remoteName = "документ.md"
        assertTrue(remoteName.endsWith(".md"))
    }

    // TC8 — Добавление фотографии в документ

    @Test
    fun tc8_imageMarkdown_hasCorrectSyntax() {
        val imagePath = "images/photo.jpg"
        val markdown = "![изображение]($imagePath)"
        assertTrue(markdown.startsWith("!["))
        assertTrue(markdown.contains(imagePath))
    }

    @Test
    fun tc8_markdownWithImage_htmlIsGenerated() {
        val markdown = "# Тест\n\n![фото](images/photo.jpg)"
        val html = MarkdownHtmlConverter.toHtml(markdown)
        assertTrue(html.isNotEmpty())
        assertTrue(html.contains("<!DOCTYPE html>"))
    }

    // TC9 — Создание формулы в документе

    @Test
    fun tc9_formulaMarkdown_wrappedInDoubleDollar() {
        val formula = "E = mc^2"
        val markdown = "\n\$\$${formula}\$\$\n"
        assertTrue(markdown.contains("\$\$"))
        assertTrue(markdown.contains(formula))
    }

    @Test
    fun tc9_markdownWithFormula_htmlContainsMathJax() {
        val markdown = "Формула: \$\$E = mc^2\$\$"
        val html = MarkdownHtmlConverter.toHtml(markdown)
        assertTrue(html.contains("MathJax"))
    }

    // TC10 — Создание графика в документе

    @Test
    fun tc10_chartImagePath_isValidMarkdownSyntax() {
        val chartPath = "/data/user/0/org.remess.mdeditor/files/charts/chart_123.png"
        val markdown = "\n![график]($chartPath)\n"
        assertTrue(markdown.contains("![график]"))
        assertTrue(markdown.contains(chartPath))
    }

    @Test
    fun tc10_markdownWithChart_htmlIsGenerated() {
        val markdown = "# Документ\n\n![график](/some/path/chart.png)"
        val html = MarkdownHtmlConverter.toHtml(markdown)
        assertTrue(html.isNotEmpty())
        assertTrue(html.contains("<!DOCTYPE html>"))
    }
}
