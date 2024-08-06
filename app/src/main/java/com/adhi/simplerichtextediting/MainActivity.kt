package com.adhi.simplerichtextediting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adhi.simplerichtextediting.ui.theme.SimpleRichTextEditingTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimpleRichTextEditingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        RichTextEditor()
                    }
                }
            }
        }
    }
}

@Composable
fun RichTextEditor() {
    var text by remember { mutableStateOf("") }
    var selection by remember { mutableStateOf(TextRange(0, 0)) }
    var formattingRanges by remember { mutableStateOf(listOf<FormattingRange>()) }
    var currentFormatting by remember { mutableStateOf(setOf(FormattingType.BODY)) }


    Column(modifier = Modifier.padding(16.dp)) {
        BasicTextField(
            value = TextFieldValue(
                annotatedString = buildAnnotatedString {
                    append(text)
                    formattingRanges.forEach { formattingRange ->
                        addStyle(
                            getStyleForFormatting(formattingRange.type),
                            formattingRange.start,
                            formattingRange.end
                        )
                    }
                }, selection = selection
            ),
            onValueChange = { newValue ->
                val oldLength = text.length
                text = newValue.text
                selection = newValue.selection

                if (newValue.text.length > oldLength) {
                    val addedLength = newValue.text.length - oldLength
                    val newRange = FormattingRange(oldLength, newValue.text.length, currentFormatting.firstOrNull() ?: FormattingType.BODY)
                    formattingRanges = updateFormattingRanges(formattingRanges, oldLength, addedLength) + newRange
                } else {
                    formattingRanges = updateFormattingRanges(formattingRanges, oldLength, newValue.text.length - oldLength)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            textStyle = TextStyle(Color.White, fontWeight = FontWeight.Thin, fontSize = 25.sp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FormatButton("Body", FormattingType.BODY, currentFormatting, selection) {
                currentFormatting = setOf(FormattingType.BODY)
                applyFormatting(FormattingType.BODY, formattingRanges, selection) { formattingRanges = it }
            }
            FormatButton("B", FormattingType.BOLD, currentFormatting, selection) {
                toggleFormatting(FormattingType.BOLD, currentFormatting) { currentFormatting = it }
                applyFormatting(FormattingType.BOLD, formattingRanges, selection) { formattingRanges = it }
            }
            FormatButton("I", FormattingType.ITALIC, currentFormatting, selection) {
                toggleFormatting(FormattingType.ITALIC, currentFormatting) { currentFormatting = it }
                applyFormatting(FormattingType.ITALIC, formattingRanges, selection) { formattingRanges = it }
            }
            FormatButton("U", FormattingType.UNDERLINE, currentFormatting, selection) {
                toggleFormatting(FormattingType.UNDERLINE, currentFormatting) { currentFormatting = it }
                applyFormatting(FormattingType.UNDERLINE, formattingRanges, selection) { formattingRanges = it }
            }
            FormatButton("H1", FormattingType.HEADER1, currentFormatting, selection) {
                currentFormatting = setOf(FormattingType.HEADER1)
                applyFormatting(FormattingType.HEADER1, formattingRanges, selection) { formattingRanges = it }
            }
            FormatButton("H2", FormattingType.HEADER2, currentFormatting, selection) {
                currentFormatting = setOf(FormattingType.HEADER2)
                applyFormatting(FormattingType.HEADER2, formattingRanges, selection) { formattingRanges = it }
            }
            FormatButton("H3", FormattingType.HEADER3, currentFormatting, selection) {
                currentFormatting = setOf(FormattingType.HEADER3)
                applyFormatting(FormattingType.HEADER3, formattingRanges, selection) { formattingRanges = it }
            }

        }

    }
}

fun getStyleForFormatting(type: FormattingType): SpanStyle {
    return when (type) {
        FormattingType.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
        FormattingType.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
        FormattingType.UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
        FormattingType.BODY -> SpanStyle()
        FormattingType.HEADER1 -> SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
        FormattingType.HEADER2 -> SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
        FormattingType.HEADER3 -> SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FormatButton(
    text: String,
    formattingType: FormattingType,
    currentFormatting: Set<FormattingType>,
    selection: TextRange,
    onFormatChange: () -> Unit
) {
    val isActive = currentFormatting.contains(formattingType)

    Button(
        onClick = onFormatChange,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) Color.LightGray else Color.White
        )
    ) {
        Text(text)
    }
}


fun updateFormattingRanges(
    oldRanges: List<FormattingRange>,
    changeIndex: Int,
    lengthDiff: Int
): List<FormattingRange> {
    return oldRanges.map { range ->
        when {
            range.end <= changeIndex -> range
            range.start >= changeIndex -> range.copy(
                start = (range.start + lengthDiff).coerceAtLeast(0),
                end = (range.end + lengthDiff).coerceAtLeast(0)
            )
            else -> range.copy(
                end = (range.end + lengthDiff).coerceAtLeast(range.start)
            )
        }
    }.filter { it.start < it.end }
}

fun applyFormatting(
    type: FormattingType,
    currentRanges: List<FormattingRange>,
    selection: TextRange,
    onRangesChange: (List<FormattingRange>) -> Unit
) {
    val newRanges = currentRanges.toMutableList()
    val overlappingRanges = newRanges.filter { it.type == type && it.start <= selection.end && it.end >= selection.start }

    if (overlappingRanges.isEmpty()) {
        // Add new formatting
        newRanges.add(FormattingRange(selection.start, selection.end, type))
    } else {
        // Remove formatting
        newRanges.removeAll(overlappingRanges)
        // Add formatting to non-overlapping parts if any
        if (selection.start < overlappingRanges.first().start) {
            newRanges.add(FormattingRange(selection.start, overlappingRanges.first().start, type))
        }
        if (selection.end > overlappingRanges.last().end) {
            newRanges.add(FormattingRange(overlappingRanges.last().end, selection.end, type))
        }
    }
    onRangesChange(newRanges)
}

fun toggleFormatting(
    type: FormattingType,
    currentFormatting: Set<FormattingType>,
    onFormatChange: (Set<FormattingType>) -> Unit
) {
    val newFormatting = currentFormatting.toMutableSet()
    if (newFormatting.contains(type)) {
        newFormatting.remove(type)
        if (newFormatting.isEmpty()) {
            newFormatting.add(FormattingType.BODY)
        }
    } else {
        newFormatting.remove(FormattingType.BODY)
        newFormatting.add(type)
    }
    onFormatChange(newFormatting)
}


data class FormattingRange(val start: Int, val end: Int, val type: FormattingType)

enum class FormattingType {
    BODY, BOLD, ITALIC, UNDERLINE, HEADER1, HEADER2, HEADER3
}

