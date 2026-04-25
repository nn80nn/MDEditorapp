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

    private val _isLoadingRemote = MutableStateFlow(false)
    val isLoadingRemote: StateFlow<Boolean> = _isLoadingRemote

    private val _downloading = MutableStateFlow<Int?>(null)
    val downloading: StateFlow<Int?> = _downloading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus

    fun loadRemoteDocuments() {
        val token = session.getBearerToken() ?: run {
            _error.value = "войдите в аккаунт для просмотра удалённых документов"
            return
        }
        viewModelScope.launch {
            _isLoadingRemote.value = true
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
            _isLoadingRemote.value = false
        }
    }

    fun downloadDocument(doc: RemoteDocument, onOpened: (Int) -> Unit) {
        val token = session.getBearerToken() ?: run {
            _error.value = "нужно войти в аккаунт"
            return
        }
        viewModelScope.launch {
            _downloading.value = doc.id
            try {
                val existing = storage.getDocumentByRemoteId(doc.id)
                    ?: storage.getDocumentByName(doc.name)
                if (existing != null) {
                    onOpened(existing.id)
                    _downloading.value = null
                    return@launch
                }
                val resp = api.downloadDocument(token, doc.id)
                if (resp.isSuccessful) {
                    val ctx = getApplication<Application>()
                    val dir = File(ctx.filesDir, "documents")
                    dir.mkdirs()
                    val file = File(dir, doc.name)
                    resp.body()?.byteStream()?.use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    }
                    val entity = storage.insertDocument(
                        DocumentEntity(name = doc.name, filePath = file.absolutePath, remoteId = doc.id)
                    )
                    onOpened(entity.id)
                } else {
                    _error.value = "не удалось скачать документ"
                }
            } catch (e: Exception) {
                _error.value = "ошибка загрузки: ${e.message}"
            }
            _downloading.value = null
        }
    }

    fun deleteFromServer(remoteId: Int, onDone: () -> Unit = {}) {
        val token = session.getBearerToken() ?: run {
            _error.value = "нужно войти в аккаунт"
            return
        }
        viewModelScope.launch {
            try {
                val resp = api.deleteDocument(token, remoteId)
                if (resp.isSuccessful) {
                    val local = storage.getDocumentByRemoteId(remoteId)
                    if (local != null) {
                        storage.updateDocument(local.copy(remoteId = null))
                    }
                    _remoteDocuments.value = _remoteDocuments.value.filter { it.id != remoteId }
                    onDone()
                } else {
                    _error.value = "ошибка удаления с сервера"
                }
            } catch (e: Exception) {
                _error.value = "нет соединения"
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
    fun clearSync() { _syncStatus.value = null }
}
