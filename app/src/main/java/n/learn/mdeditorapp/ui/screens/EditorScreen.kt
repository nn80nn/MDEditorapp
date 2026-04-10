package n.learn.mdeditorapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import n.learn.mdeditorapp.ui.components.FormulaDialog
import n.learn.mdeditorapp.ui.components.MarkdownPreviewView
import n.learn.mdeditorapp.viewmodel.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    docId: Int,
    onOpenChartBuilder: () -> Unit,
    onBack: () -> Unit,
    vm: EditorViewModel = viewModel()
) {
    val context = LocalContext.current
    val content by vm.content.collectAsStateWithLifecycle()
    val docName by vm.documentName.collectAsStateWithLifecycle()
    val showPreview by vm.showPreview.collectAsStateWithLifecycle()
    val uploadStatus by vm.uploadStatus.collectAsStateWithLifecycle()
    val hasDraft by vm.hasDraft.collectAsStateWithLifecycle()

    var showFormulaDialog by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = it.lastPathSegment ?: "image"
            vm.insertText("\n![изображение]($fileName)\n")
        }
    }

    LaunchedEffect(docId) {
        vm.loadDocument(docId)
    }

    if (hasDraft) {
        AlertDialog(
            onDismissRequest = { vm.discardDraft() },
            title = { Text("Черновик найден") },
            text = { Text("Восстановить последний несохранённый черновик?") },
            confirmButton = {
                TextButton(onClick = { vm.restoreFromDraft() }) { Text("Восстановить") }
            },
            dismissButton = {
                TextButton(onClick = { vm.discardDraft() }) { Text("Отмена") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(docName, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { vm.saveNow(); onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "назад")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.togglePreview() }) {
                        Icon(
                            if (showPreview) Icons.Default.EditNote else Icons.Default.Visibility,
                            contentDescription = if (showPreview) "редактировать" else "предпросмотр"
                        )
                    }
                    IconButton(onClick = { vm.uploadToServer() }) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "загрузить на сервер")
                    }
                }
            )
        },
        bottomBar = {
            if (!showPreview) {
                EditorBottomBar(
                    onInsertFormula = { showFormulaDialog = true },
                    onInsertImage = { imagePicker.launch("image/*") },
                    onBuildChart = onOpenChartBuilder
                )
            }
        },
        snackbarHost = {
            uploadStatus?.let {
                Snackbar(
                    modifier = Modifier.padding(8.dp),
                    action = { TextButton(onClick = { vm.clearUploadStatus() }) { Text("OK") } }
                ) { Text(it) }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (showPreview) {
                MarkdownPreviewView(markdown = content)
            } else {
                BasicTextField(
                    value = content,
                    onValueChange = { vm.updateContent(it) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }

    if (showFormulaDialog) {
        FormulaDialog(
            onInsert = { formula ->
                vm.insertText("$$${formula}$$")
                showFormulaDialog = false
            },
            onDismiss = { showFormulaDialog = false }
        )
    }
}

@Composable
private fun EditorBottomBar(
    onInsertFormula: () -> Unit,
    onInsertImage: () -> Unit,
    onBuildChart: () -> Unit
) {
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onInsertFormula) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Functions, contentDescription = "формула")
                    Text("Формула", style = MaterialTheme.typography.labelSmall)
                }
            }
            IconButton(onClick = onInsertImage) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Image, contentDescription = "изображение")
                    Text("Фото", style = MaterialTheme.typography.labelSmall)
                }
            }
            IconButton(onClick = onBuildChart) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.BarChart, contentDescription = "график")
                    Text("График", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
