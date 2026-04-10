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
    val error by vm.error.collectAsStateWithLifecycle()

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
            FloatingActionButton(onClick = { showNewDocDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "создать")
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
                0 -> LocalDocsList(docs = localDocs, onOpen = onOpenDocument, onDelete = { vm.deleteDocument(it) })
                1 -> RemoteTabContent(onOpenRemoteDocs = onOpenRemoteDocs)
            }
        }
    }

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
private fun RemoteTabContent(onOpenRemoteDocs: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Документы на сервере", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onOpenRemoteDocs) { Text("Открыть") }
        }
    }
}
