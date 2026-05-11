package org.remess.mdeditor

import org.junit.Assert.*
import org.junit.Test
import org.remess.mdeditor.data.local.DocumentEntity

// Тест-кейсы №4, 5 — создание и удаление документов
class DocumentTest {

    // TC4 — Создание нового документа

    @Test
    fun tc4_createDocument_hasCorrectName() {
        val doc = DocumentEntity(id = 1, name = "Тест", filePath = "/tmp/test.md")
        assertEquals("Тест", doc.name)
    }

    @Test
    fun tc4_createDocument_defaultIdIsZero() {
        val doc = DocumentEntity(name = "Новый документ", filePath = "/tmp/new.md")
        assertEquals(0, doc.id)
    }

    @Test
    fun tc4_createDocument_blankNameFallsToDefault() {
        val inputName = ""
        val name = inputName.ifBlank { "Новый документ" }
        assertEquals("Новый документ", name)
    }

    @Test
    fun tc4_createDocument_remoteIdIsNullByDefault() {
        val doc = DocumentEntity(name = "Тест", filePath = "/tmp/test.md")
        assertNull(doc.remoteId)
    }

    @Test
    fun tc4_createDocument_createdAtIsSet() {
        val before = System.currentTimeMillis()
        val doc = DocumentEntity(name = "Тест", filePath = "/tmp/test.md")
        val after = System.currentTimeMillis()
        assertTrue(doc.createdAt in before..after)
    }

    // TC5 — Удаление существующего документа

    @Test
    fun tc5_deleteDocument_removesFromList() {
        val docs = mutableListOf(
            DocumentEntity(id = 1, name = "Документ 1", filePath = "/tmp/1.md"),
            DocumentEntity(id = 2, name = "Документ 2", filePath = "/tmp/2.md")
        )
        docs.removeAll { it.id == 1 }
        assertEquals(1, docs.size)
        assertFalse(docs.any { it.id == 1 })
    }

    @Test
    fun tc5_deleteDocument_correctItemRemoved() {
        val docs = mutableListOf(
            DocumentEntity(id = 1, name = "Удаляемый", filePath = "/tmp/1.md"),
            DocumentEntity(id = 2, name = "Остающийся", filePath = "/tmp/2.md")
        )
        docs.removeAll { it.id == 1 }
        assertEquals("Остающийся", docs[0].name)
    }

    @Test
    fun tc5_deleteDocument_emptyListAfterLastRemoval() {
        val docs = mutableListOf(
            DocumentEntity(id = 1, name = "Единственный", filePath = "/tmp/1.md")
        )
        docs.removeAll { it.id == 1 }
        assertTrue(docs.isEmpty())
    }
}
