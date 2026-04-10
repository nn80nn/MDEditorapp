package n.learn.mdeditorapp.data.local

data class DocumentEntity(
    val id: Int = 0,
    val name: String,
    val filePath: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
