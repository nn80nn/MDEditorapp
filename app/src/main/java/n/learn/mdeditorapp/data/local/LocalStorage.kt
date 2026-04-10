package n.learn.mdeditorapp.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

class LocalStorage(context: Context) {

    private val gson = Gson()
    private val storageDir = File(context.filesDir, "storage").also { it.mkdirs() }
    private val docsFile = File(storageDir, "documents.json")
    private val draftsFile = File(storageDir, "drafts.json")

    private val _documents = MutableStateFlow<List<DocumentEntity>>(emptyList())
    val documents: Flow<List<DocumentEntity>> = _documents.asStateFlow()

    init {
        _documents.value = loadDocuments()
    }

    // Documents

    private fun loadDocuments(): List<DocumentEntity> {
        if (!docsFile.exists()) return emptyList()
        return try {
            val type = object : TypeToken<List<DocumentEntity>>() {}.type
            gson.fromJson(docsFile.readText(), type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveDocumentsToFile(docs: List<DocumentEntity>) {
        docsFile.writeText(gson.toJson(docs))
        _documents.value = docs
    }

    suspend fun insertDocument(doc: DocumentEntity): DocumentEntity = withContext(Dispatchers.IO) {
        val current = loadDocuments().toMutableList()
        val newId = (current.maxOfOrNull { it.id } ?: 0) + 1
        val newDoc = doc.copy(id = newId)
        current.add(0, newDoc)
        saveDocumentsToFile(current)
        newDoc
    }

    suspend fun updateDocument(doc: DocumentEntity) = withContext(Dispatchers.IO) {
        val current = loadDocuments().toMutableList()
        val idx = current.indexOfFirst { it.id == doc.id }
        if (idx >= 0) {
            current[idx] = doc
            saveDocumentsToFile(current)
        }
    }

    suspend fun deleteDocument(doc: DocumentEntity) = withContext(Dispatchers.IO) {
        val current = loadDocuments().toMutableList()
        current.removeAll { it.id == doc.id }
        saveDocumentsToFile(current)
    }

    suspend fun getDocumentById(id: Int): DocumentEntity? = withContext(Dispatchers.IO) {
        loadDocuments().find { it.id == id }
    }

    // Drafts

    private fun loadDrafts(): MutableMap<Int, DraftEntity> {
        if (!draftsFile.exists()) return mutableMapOf()
        return try {
            val type = object : TypeToken<MutableMap<Int, DraftEntity>>() {}.type
            gson.fromJson(draftsFile.readText(), type) ?: mutableMapOf()
        } catch (e: Exception) {
            mutableMapOf()
        }
    }

    suspend fun saveDraft(draft: DraftEntity) = withContext(Dispatchers.IO) {
        val drafts = loadDrafts()
        drafts[draft.documentId] = draft
        draftsFile.writeText(gson.toJson(drafts))
    }

    suspend fun getDraft(documentId: Int): DraftEntity? = withContext(Dispatchers.IO) {
        loadDrafts()[documentId]
    }

    suspend fun deleteDraft(documentId: Int) = withContext(Dispatchers.IO) {
        val drafts = loadDrafts()
        drafts.remove(documentId)
        draftsFile.writeText(gson.toJson(drafts))
    }

    companion object {
        @Volatile
        private var instance: LocalStorage? = null

        fun getInstance(context: Context): LocalStorage {
            return instance ?: synchronized(this) {
                instance ?: LocalStorage(context.applicationContext).also { instance = it }
            }
        }
    }
}
