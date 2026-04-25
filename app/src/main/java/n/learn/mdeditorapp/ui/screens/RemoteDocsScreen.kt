package n.learn.mdeditorapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import n.learn.mdeditorapp.data.local.DocumentEntity
import n.learn.mdeditorapp.data.local.LocalStorage
import n.learn.mdeditorapp.data.remote.RemoteDocument
import n.learn.mdeditorapp.data.remote.RetrofitClient
import n.learn.mdeditorapp.util.SessionManager
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteDocsScreen(
    onOpenDocument: (Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }
    val api = remember { RetrofitClient.api }
    val storage = remember { LocalStorage.getInstance(context) }

    var docs by remember { mutableStateOf<List<RemoteDocument>>(emptyList()) }
    var localDocs by remember { mutableStateOf<List<DocumentEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var downloading by remember { mutableStateOf<Int?>(null) }
    var docToDelete by remember { mutableStateOf<RemoteDocument?>(null) }
    var deleting by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        val token = session.getBearerToken()
        if (token == null) {
            error = "войдите в аккаунт"
            isLoading = false
            return@LaunchedEffect
        }
        try {
            val resp = api.getDocuments(token)
            if (resp.isSuccessful) {
                docs = resp.body() ?: emptyList()
            } else {
                error = "ошибка загрузки списка"
            }
        } catch (e: Exception) {
            error = "нет соединения с сервером"
        }
        isLoading = false
    }

    // загружаем локальные документы для отображения статуса синхронизации
    LaunchedEffect(Unit) {
        storage.documents.collect { localDocs = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Документы на сервере") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            isLoading = true
                            val token = session.getBearerToken() ?: return@launch
                            try {
                                val resp = api.getDocuments(token)
                                if (resp.isSuccessful) docs = resp.body() ?: emptyList()
                            } catch (_: Exception) {}
                            isLoading = false
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "обновить")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> Text(
                    error!!,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
                docs.isEmpty() -> Text(
                    "Нет документов на сервере",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> LazyColumn {
                    items(docs) { doc ->
                        val isSynced = localDocs.any { it.remoteId == doc.id }
                        ListItem(
                            headlineContent = {
                                Text(doc.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            supportingContent = {
                                Text(
                                    buildString {
                                        append("${doc.fileSize / 1024} КБ • ${doc.updatedAt.take(10)}")
                                        if (isSynced) append(" • скачан")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSynced) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingContent = {
                                Icon(
                                    if (isSynced) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = if (isSynced) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (downloading == doc.id) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        IconButton(onClick = {
                                            scope.launch {
                                                downloading = doc.id
                                                try {
                                                    val existing = storage.getDocumentByRemoteId(doc.id)
                                                        ?: storage.getDocumentByName(doc.name)
                                                    if (existing != null) {
                                                        onOpenDocument(existing.id)
                                                        downloading = null
                                                        return@launch
                                                    }
                                                    val token = session.getBearerToken() ?: return@launch
                                                    val resp = api.downloadDocument(token, doc.id)
                                                    if (resp.isSuccessful) {
                                                        val dir = File(context.filesDir, "documents")
                                                        dir.mkdirs()
                                                        val file = File(dir, doc.name)
                                                        resp.body()?.byteStream()?.use { input ->
                                                            file.outputStream().use { output -> input.copyTo(output) }
                                                        }
                                                        val entity = storage.insertDocument(
                                                            DocumentEntity(
                                                                name = doc.name,
                                                                filePath = file.absolutePath,
                                                                remoteId = doc.id
                                                            )
                                                        )
                                                        onOpenDocument(entity.id)
                                                    } else {
                                                        error = "не удалось скачать документ"
                                                    }
                                                } catch (e: Exception) {
                                                    error = "ошибка: ${e.message}"
                                                }
                                                downloading = null
                                            }
                                        }) {
                                            Icon(Icons.Default.Download, contentDescription = "скачать")
                                        }
                                    }

                                    if (deleting == doc.id) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        IconButton(onClick = { docToDelete = doc }) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = "удалить с сервера",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    // диалог подтверждения удаления с сервера
    docToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { docToDelete = null },
            title = { Text("Удалить с сервера?") },
            text = { Text("«${doc.name}» будет удалён с сервера. Локальная копия останется.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = doc
                        docToDelete = null
                        scope.launch {
                            deleting = target.id
                            try {
                                val token = session.getBearerToken() ?: return@launch
                                val resp = api.deleteDocument(token, target.id)
                                if (resp.isSuccessful) {
                                    // сбрасываем remoteId у локального документа
                                    val local = storage.getDocumentByRemoteId(target.id)
                                    if (local != null) {
                                        storage.updateDocument(local.copy(remoteId = null))
                                    }
                                    docs = docs.filter { it.id != target.id }
                                } else {
                                    error = "ошибка удаления с сервера"
                                }
                            } catch (e: Exception) {
                                error = "нет соединения"
                            }
                            deleting = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { docToDelete = null }) { Text("Отмена") }
            }
        )
    }
}
