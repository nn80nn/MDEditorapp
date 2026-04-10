package n.learn.mdeditorapp.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import n.learn.mdeditorapp.data.local.DocumentEntity
import n.learn.mdeditorapp.data.local.LocalStorage
import n.learn.mdeditorapp.data.remote.RemoteDocument
import n.learn.mdeditorapp.data.remote.RetrofitClient
import n.learn.mdeditorapp.util.SessionManager
import java.io.File

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = LocalStorage.getInstance(application)
    private val session = SessionManager(application)
    private val api = RetrofitClient.api

    val localDocuments = storage.documents

    private val _remoteDocuments = MutableStateFlow<List<RemoteDocument>>(emptyList())
    val remoteDocuments: StateFlow<List<RemoteDocument>> = _remoteDocuments

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadRemoteDocuments() {
        val token = session.getBearerToken() ?: run {
            _error.value = "войдите в аккаунт для просмотра удалённых документов"
            return
        }
        viewModelScope.launch {
            try {
                val resp = api.getDocuments(token)
                if (resp.isSuccessful) {
                    _remoteDocuments.value = resp.body() ?: emptyList()
                } else {
                    _error.value = "не удалось загрузить список документов"
                }
            } catch (e: Exception) {
                _error.value = "нет соединения с сервером"
            }
        }
    }

    fun createNewDocument(name: String, onCreated: (DocumentEntity) -> Unit) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val dir = File(ctx.filesDir, "documents")
            dir.mkdirs()
            val file = File(dir, "${System.currentTimeMillis()}.md")
            file.createNewFile()

            val doc = storage.insertDocument(
                DocumentEntity(
                    name = name.ifBlank { "Новый документ" },
                    filePath = file.absolutePath
                )
            )
            onCreated(doc)
        }
    }

    fun openLocalFile(uri: Uri, onOpened: (DocumentEntity) -> Unit) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val dir = File(ctx.filesDir, "documents")
            dir.mkdirs()
            val file = File(dir, "${System.currentTimeMillis()}_imported.md")

            ctx.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }

            val name = uri.lastPathSegment?.substringAfterLast("/") ?: "документ.md"
            val doc = storage.insertDocument(DocumentEntity(name = name, filePath = file.absolutePath))
            onOpened(doc)
        }
    }

    fun deleteDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            File(doc.filePath).delete()
            storage.deleteDocument(doc)
        }
    }

    fun clearError() { _error.value = null }
}
