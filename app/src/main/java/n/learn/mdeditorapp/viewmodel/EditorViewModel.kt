package n.learn.mdeditorapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import n.learn.mdeditorapp.data.local.DocumentEntity
import n.learn.mdeditorapp.data.local.DraftEntity
import n.learn.mdeditorapp.data.local.LocalStorage
import n.learn.mdeditorapp.data.remote.RetrofitClient
import n.learn.mdeditorapp.util.SessionManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = LocalStorage.getInstance(application)
    private val session = SessionManager(application)
    private val api = RetrofitClient.api

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content

    private val _documentName = MutableStateFlow("")
    val documentName: StateFlow<String> = _documentName

    private val _showPreview = MutableStateFlow(false)
    val showPreview: StateFlow<Boolean> = _showPreview

    private val _uploadStatus = MutableStateFlow<String?>(null)
    val uploadStatus: StateFlow<String?> = _uploadStatus

    private val _hasDraft = MutableStateFlow(false)
    val hasDraft: StateFlow<Boolean> = _hasDraft

    private var currentDoc: DocumentEntity? = null
    private var autosaveJob: Job? = null

    fun loadDocument(docId: Int) {
        viewModelScope.launch {
            val doc = storage.getDocumentById(docId) ?: return@launch
            currentDoc = doc
            _documentName.value = doc.name

            val draft = storage.getDraft(docId)
            if (draft != null) {
                _hasDraft.value = true
                _content.value = draft.content
            } else {
                val file = File(doc.filePath)
                _content.value = if (file.exists()) file.readText() else ""
            }
            startAutosave()
        }
    }

    fun restoreFromDraft() {
        _hasDraft.value = false
    }

    fun discardDraft() {
        viewModelScope.launch {
            currentDoc?.let { storage.deleteDraft(it.id) }
            val doc = currentDoc ?: return@launch
            val file = File(doc.filePath)
            _content.value = if (file.exists()) file.readText() else ""
            _hasDraft.value = false
        }
    }

    fun updateContent(text: String) {
        _content.value = text
    }

    fun togglePreview() {
        _showPreview.value = !_showPreview.value
    }

    fun insertText(text: String) {
        _content.value = _content.value + text
    }

    fun saveNow() {
        viewModelScope.launch {
            val doc = currentDoc ?: return@launch
            File(doc.filePath).writeText(_content.value)
            storage.deleteDraft(doc.id)
            storage.updateDocument(doc.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun uploadToServer() {
        val doc = currentDoc ?: return
        val token = session.getBearerToken() ?: run {
            _uploadStatus.value = "нужно войти в аккаунт"
            return
        }
        viewModelScope.launch {
            saveNow()
            try {
                val file = File(doc.filePath)
                val namePart = doc.name.toRequestBody("text/plain".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData(
                    "file", file.name,
                    file.asRequestBody("text/markdown".toMediaTypeOrNull())
                )
                val resp = api.uploadDocument(token, namePart, filePart)
                _uploadStatus.value = if (resp.isSuccessful) "загружено на сервер" else "ошибка загрузки"
            } catch (e: Exception) {
                _uploadStatus.value = "нет соединения"
            }
        }
    }

    fun clearUploadStatus() { _uploadStatus.value = null }

    private fun startAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            while (isActive) {
                delay(10_000)
                val doc = currentDoc ?: continue
                storage.saveDraft(DraftEntity(documentId = doc.id, content = _content.value))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autosaveJob?.cancel()
        saveNow()
    }
}
