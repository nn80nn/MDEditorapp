package org.remess.mdeditor.data.local

data class DraftEntity(
    val documentId: Int,
    val content: String,
    val savedAt: Long = System.currentTimeMillis()
)
