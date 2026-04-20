package n.learn.mdeditorapp.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import n.learn.mdeditorapp.ui.components.FormulaDialog
import n.learn.mdeditorapp.ui.components.MarkdownPreviewView
import n.learn.mdeditorapp.viewmodel.EditorViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    docId: Int,
    navEntry: NavBackStackEntry? = null,
    onOpenChartBuilder: () -> Unit,
    onBack: () -> Unit,
    vm: EditorViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val content by vm.content.collectAsStateWithLifecycle()
    val docName by vm.documentName.collectAsStateWithLifecycle()
    val showPreview by vm.showPreview.collectAsStateWithLifecycle()
    val uploadStatus by vm.uploadStatus.collectAsStateWithLifecycle()
    val hasDraft by vm.hasDraft.collectAsStateWithLifecycle()

    var showFormulaDialog by remember { mutableStateOf(false) }

    // читаем вставку графика из savedStateHandle
    val chartPath by navEntry?.savedStateHandle
        ?.getStateFlow<String?>("chart_image_path", null)
        ?.collectAsState()
        ?: remember { mutableStateOf(null) }

    LaunchedEffect(chartPath) {
        chartPath?.let { path ->
            vm.insertText("\n![график]($path)\n")
            navEntry?.savedStateHandle?.remove<String>("chart_image_path")
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri ->
            scope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(imageUri)
                    val original = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    if (original != null) {
                        val maxSize = 1024
                        val scaled = if (original.width > maxSize || original.height > maxSize) {
                            val ratio = maxSize.toFloat() / maxOf(original.width, original.height)
                            Bitmap.createScaledBitmap(
                                original,
                                (original.width * ratio).toInt(),
                                (original.height * ratio).toInt(),
                                true
                            )
                        } else original

                        val imgDir = File(context.filesDir, "images").also { it.mkdirs() }
                        val imgFile = File(imgDir, "img_${System.currentTimeMillis()}.jpg")
                        FileOutputStream(imgFile).use { out ->
                            scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
                        }

                        withContext(Dispatchers.Main) {
                            vm.insertText("\n![изображение](images/${imgFile.name})\n")
                        }
                    }
                } catch (e: Exception) {
                    // игнорируем
                }
            }
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
        modifier = Modifier.imePadding(),
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
        snackbarHost = {
            uploadStatus?.let {
                Snackbar(
                    modifier = Modifier.padding(8.dp),
                    action = { TextButton(onClick = { vm.clearUploadStatus() }) { Text("OK") } }
                ) { Text(it) }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (!showPreview) {
                EditorToolbar(
                    onInsertFormula = { showFormulaDialog = true },
                    onInsertImage = { imagePicker.launch("image/*") },
                    onBuildChart = onOpenChartBuilder
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (showPreview) {
                    MarkdownPreviewView(markdown = content, modifier = Modifier.fillMaxSize())
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

            if (!showPreview) {
                SymbolRow(onInsert = { vm.insertText(it) })
            }
        }
    }

    if (showFormulaDialog) {
        FormulaDialog(
            onInsert = { formula ->
                vm.insertText("\n$$${formula}$$\n")
                showFormulaDialog = false
            },
            onDismiss = { showFormulaDialog = false }
        )
    }
}

@Composable
private fun EditorToolbar(
    onInsertFormula: () -> Unit,
    onInsertImage: () -> Unit,
    onBuildChart: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onInsertFormula,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Functions, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Формула", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(
                onClick = onInsertImage,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Фото", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(
                onClick = onBuildChart,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("График", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun SymbolRow(onInsert: (String) -> Unit) {
    val symbols = listOf(
        "#" to "# ",
        "##" to "## ",
        "###" to "### ",
        "**b**" to "****",
        "*i*" to "**",
        "`" to "``",
        "```" to "\n```\n\n```\n",
        ">" to "\n> ",
        "- " to "\n- ",
        "---" to "\n\n---\n\n",
        "[ ]" to "[текст](url)",
        "$$" to "\n$$\n\n$$\n",
        "^" to "^{}",
        "_" to "_{}",
        "\\" to "\\"
    )

    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            symbols.forEach { (label, insert) ->
                TextButton(
                    onClick = { onInsert(insert) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        label,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
