package n.learn.mdeditorapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import n.learn.mdeditorapp.data.local.DocumentEntity
import n.learn.mdeditorapp.data.remote.RemoteDocument
import n.learn.mdeditorapp.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenDocument: (Int) -> Unit,
    onOpenRemoteDocs: () -> Unit,
    onLogout: () -> Unit,
    vm: HomeViewModel = viewModel()
) {
    var showNewDocDialog by remember { mutableStateOf(false) }
    var newDocName by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    val localDocs by vm.localDocuments.collectAsStateWithLifecycle(initialValue = emptyList())
    val remoteDocs by vm.remoteDocuments.collectAsStateWithLifecycle()
    val isLoadingRemote by vm.isLoadingRemote.collectAsStateWithLifecycle()
    val downloading by vm.downloading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    // подтверждение удаления локального документа
    var docToDelete by remember { mutableStateOf<DocumentEntity?>(null) }
    // подтверждение удаления с сервера
    var remoteToDelete by remember { mutableStateOf<RemoteDocument?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { vm.openLocalFile(it) { doc -> onOpenDocument(doc.id) } }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) vm.loadRemoteDocuments()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MD Editor") },
                actions = {
                    if (selectedTab == 1) {
                        IconButton(onClick = { vm.loadRemoteDocuments() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "обновить")
                        }
                    }
                    IconButton(onClick = { filePicker.launch("*/*") }) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "открыть файл")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "выйти")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { showNewDocDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "создать")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Локальные") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("На сервере") }
                )
            }

            error?.let {
                Snackbar(
                    modifier = Modifier.padding(8.dp),
                    action = { TextButton(onClick = { vm.clearError() }) { Text("OK") } }
                ) { Text(it) }
            }

            when (selectedTab) {
                0 -> LocalDocsList(
                    docs = localDocs,
                    onOpen = onOpenDocument,
                    onDelete = { docToDelete = it }
                )
                1 -> RemoteDocsList(
                    docs = remoteDocs,
                    localDocs = localDocs,
                    isLoading = isLoadingRemote,
                    downloading = downloading,
                    onDownload = { doc -> vm.downloadDocument(doc, onOpened = onOpenDocument) },
                    onDelete = { remoteToDelete = it }
                )
            }
        }
    }

    // диалог создания документа
    if (showNewDocDialog) {
        AlertDialog(
            onDismissRequest = { showNewDocDialog = false },
            title = { Text("Новый документ") },
            text = {
                OutlinedTextField(
                    value = newDocName,
                    onValueChange = { newDocName = it },
                    label = { Text("Название") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.createNewDocument(newDocName) { doc -> onOpenDocument(doc.id) }
                    showNewDocDialog = false
                    newDocName = ""
                }) { Text("Создать") }
            },
            dismissButton = {
                TextButton(onClick = { showNewDocDialog = false }) { Text("Отмена") }
            }
        )
    }

    // диалог подтверждения удаления локального документа
    docToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { docToDelete = null },
            title = { Text("Удалить документ?") },
            text = { Text("«${doc.name}» будет удалён с устройства.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteDocument(doc)
                        docToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { docToDelete = null }) { Text("Отмена") }
            }
        )
    }

    // диалог подтверждения удаления с сервера
    remoteToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { remoteToDelete = null },
            title = { Text("Удалить с сервера?") },
            text = { Text("«${doc.name}» будет удалён с сервера. Локальная копия останется.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteFromServer(doc.id)
                        remoteToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { remoteToDelete = null }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun LocalDocsList(
    docs: List<DocumentEntity>,
    onOpen: (Int) -> Unit,
    onDelete: (DocumentEntity) -> Unit
) {
    if (docs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Нет документов. Нажмите + чтобы создать.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn {
        items(docs) { doc ->
            ListItem(
                headlineContent = {
                    Text(doc.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                supportingContent = {
                    Text(
                        java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date(doc.updatedAt)),
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                leadingContent = {
                    if (doc.remoteId != null) {
                        Icon(
                            Icons.Default.Cloud,
                            contentDescription = "сохранён на сервере",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                trailingContent = {
                    IconButton(onClick = { onDelete(doc) }) {
                        Icon(Icons.Default.Delete, contentDescription = "удалить")
                    }
                },
                modifier = Modifier.clickable { onOpen(doc.id) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun RemoteDocsList(
    docs: List<RemoteDocument>,
    localDocs: List<DocumentEntity>,
    isLoading: Boolean,
    downloading: Int?,
    onDownload: (RemoteDocument) -> Unit,
    onDelete: (RemoteDocument) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                                    IconButton(onClick = { onDownload(doc) }) {
                                        Icon(Icons.Default.Download, contentDescription = "скачать")
                                    }
                                }
                                IconButton(onClick = { onDelete(doc) }) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "удалить с сервера",
                                        tint = MaterialTheme.colorScheme.error
                                    )
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
