package n.learn.mdeditorapp.data.local

data class DraftEntity(
    val documentId: Int,
    val content: String,
    val savedAt: Long = System.currentTimeMillis()
)
