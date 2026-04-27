package org.remess.mdeditor.data.remote

data class RemoteDocument(
    val id: Int,
    val name: String,
    val fileSize: Long,
    val createdAt: String,
    val updatedAt: String
)
