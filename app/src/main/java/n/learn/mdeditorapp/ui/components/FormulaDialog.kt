package n.learn.mdeditorapp.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

private data class LatexTemplate(val label: String, val code: String)

private val templates = listOf(
    LatexTemplate("a/b", "\\frac{a}{b}"),
    LatexTemplate("xⁿ", "x^{n}"),
    LatexTemplate("xₙ", "x_{n}"),
    LatexTemplate("√x", "\\sqrt{x}"),
    LatexTemplate("ⁿ√x", "\\sqrt[n]{x}"),
    LatexTemplate("∫", "\\int_{a}^{b} f(x)\\,dx"),
    LatexTemplate("Σ", "\\sum_{i=1}^{n} x_i"),
    LatexTemplate("∏", "\\prod_{i=1}^{n}"),
    LatexTemplate("lim", "\\lim_{x \\to \\infty}"),
    LatexTemplate("α", "\\alpha"),
    LatexTemplate("β", "\\beta"),
    LatexTemplate("γ", "\\gamma"),
    LatexTemplate("π", "\\pi"),
    LatexTemplate("σ", "\\sigma"),
    LatexTemplate("θ", "\\theta"),
    LatexTemplate("λ", "\\lambda"),
    LatexTemplate("∞", "\\infty"),
    LatexTemplate("∈", "\\in"),
    LatexTemplate("⊂", "\\subset"),
    LatexTemplate("∪", "\\cup"),
    LatexTemplate("∩", "\\cap"),
    LatexTemplate("матрица", "\\begin{pmatrix} a & b \\\\ c & d \\end{pmatrix}"),
    LatexTemplate("≤", "\\leq"),
    LatexTemplate("≥", "\\geq"),
    LatexTemplate("≠", "\\neq"),
    LatexTemplate("±", "\\pm")
)

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
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Шаблоны:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    templates.forEach { tmpl ->
                        SuggestionChip(
                            onClick = { formula = tmpl.code },
                            label = { Text(tmpl.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Выражение:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = formula,
                    onValueChange = { formula = it },
                    placeholder = {
                        Text("\\frac{1}{2} x^2 + y^2", fontFamily = FontFamily.Monospace)
                    },
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
