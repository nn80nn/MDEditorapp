package n.learn.mdeditorapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun FormulaDialog(
    onInsert: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var formula by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Вставить формулу (LaTeX)") },
        text = {
            Column {
                Text(
                    "Введите LaTeX-выражение:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = formula,
                    onValueChange = { formula = it },
                    placeholder = { Text("\\frac{1}{2} x^2 + y^2", fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (formula.isNotBlank()) onInsert(formula) },
                enabled = formula.isNotBlank()
            ) { Text("Вставить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
