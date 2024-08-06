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
import androidx.compose.ui.text.AnnotatedString
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
    var formattingRanges by remember {
        mutableStateOf(
            listOf<FormattingRange>(
                FormattingRange(
                    0,
                    0,
                    FormattingType.HEADER1
                )
            )
        )
    }
    var currentFormatting by remember { mutableStateOf(setOf(FormattingType.HEADER1)) }
    var isFirstLine by remember { mutableStateOf(true) }

    Column(modifier = Modifier.padding(16.dp)) {
        BasicTextField(
            value = TextFieldValue(
                annotatedString = buildSafeAnnotatedString(text, formattingRanges),
                selection = selection
            ),
            onValueChange = { newValue ->
                val oldLength = text.length
                text = newValue.text
                selection = newValue.selection

                if (newValue.text.length > oldLength) {
                    // Text was added
                    val addedLength = newValue.text.length - oldLength
                    val newRanges = currentFormatting.map { type ->
                        FormattingRange(oldLength, newValue.text.length, type)
                    }
                    formattingRanges =
                        updateFormattingRanges(formattingRanges, oldLength, addedLength) + newRanges

                    // Check if return was hit after a header
                    if (addedLength == 1 && newValue.text[oldLength] == '\n') {
                        val headerType = formattingRanges.lastOrNull {
                            it.end == oldLength && it.type in setOf(
                                FormattingType.HEADER1,
                                FormattingType.HEADER2,
                                FormattingType.HEADER3
                            )
                        }?.type
                        if (headerType != null) {
                            currentFormatting = setOf(FormattingType.BODY)
                            formattingRanges = formattingRanges + FormattingRange(
                                oldLength + 1,
                                newValue.text.length,
                                FormattingType.BODY
                            )
                        }
                    }
                } else {
                    // Text was removed or replaced
                    formattingRanges = updateFormattingRanges(
                        formattingRanges,
                        oldLength,
                        newValue.text.length - oldLength
                    )
                }
                if (isFirstLine && text.isNotEmpty()) {
                    formattingRanges = formattingRanges.map {
                        if (it.type == FormattingType.HEADER1 && it.start == 0) it.copy(end = text.indexOf('\n').let { if (it == -1) text.length else it })
                        else it
                    }
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
                if (!isFirstLine) {
                    currentFormatting = currentFormatting.filter { it in setOf(FormattingType.BOLD, FormattingType.ITALIC, FormattingType.UNDERLINE) }.toMutableSet().apply { add(FormattingType.BODY) }
                    formattingRanges = applyFormatting(FormattingType.BODY, formattingRanges, selection)
                }
            }
            FormatButton("B", FormattingType.BOLD, currentFormatting, selection) {
                toggleInlineFormatting(FormattingType.BOLD, currentFormatting) { currentFormatting = it }
                formattingRanges = applyFormatting(FormattingType.BOLD, formattingRanges, selection)
            }
            FormatButton("I", FormattingType.ITALIC, currentFormatting, selection) {
                toggleInlineFormatting(FormattingType.ITALIC, currentFormatting) { currentFormatting = it }
                formattingRanges = applyFormatting(FormattingType.ITALIC, formattingRanges, selection)
            }
            FormatButton("U", FormattingType.UNDERLINE, currentFormatting, selection) {
                toggleInlineFormatting(FormattingType.UNDERLINE, currentFormatting) { currentFormatting = it }
                formattingRanges = applyFormatting(FormattingType.UNDERLINE, formattingRanges, selection)
            }
            FormatButton("H1", FormattingType.HEADER1, currentFormatting, selection) {
                if (!isFirstLine) {
                    currentFormatting = setOf(FormattingType.HEADER1)
                    formattingRanges = applyFormatting(FormattingType.HEADER1, formattingRanges, selection)
                }
            }
            FormatButton("H2", FormattingType.HEADER2, currentFormatting, selection) {
                if (!isFirstLine) {
                    currentFormatting = setOf(FormattingType.HEADER2)
                    formattingRanges = applyFormatting(FormattingType.HEADER2, formattingRanges, selection)
                }
            }
            FormatButton("H3", FormattingType.HEADER3, currentFormatting, selection) {
                if (!isFirstLine) {
                    currentFormatting = setOf(FormattingType.HEADER3)
                    formattingRanges = applyFormatting(FormattingType.HEADER3, formattingRanges, selection)
                }
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
    return oldRanges.mapNotNull { range ->
        when {
            range.end <= changeIndex -> range
            range.start >= changeIndex -> range.copy(
                start = (range.start + lengthDiff).coerceAtLeast(0),
                end = (range.end + lengthDiff).coerceAtLeast(0)
            )

            else -> range.copy(
                end = (range.end + lengthDiff).coerceAtLeast(range.start)
            )
        }.takeIf { it.start < it.end }
    }
}

fun applyFormatting(
    type: FormattingType,
    currentRanges: List<FormattingRange>,
    selection: TextRange
): List<FormattingRange> {
    val newRanges = currentRanges.toMutableList()

    when (type) {
        FormattingType.BODY -> {
            // Remove only header formatting
            newRanges.removeAll {
                it.type in setOf(
                    FormattingType.HEADER1,
                    FormattingType.HEADER2,
                    FormattingType.HEADER3
                ) && it.start < selection.end && it.end > selection.start
            }
        }

        in setOf(FormattingType.HEADER1, FormattingType.HEADER2, FormattingType.HEADER3) -> {
            // Remove existing header formatting and add new header
            newRanges.removeAll {
                it.type in setOf(
                    FormattingType.HEADER1,
                    FormattingType.HEADER2,
                    FormattingType.HEADER3,
                    FormattingType.BODY
                ) && it.start < selection.end && it.end > selection.start
            }
            newRanges.add(FormattingRange(selection.start, selection.end, type))
        }

        else -> {
            // Toggle inline formatting
            val existingRange =
                newRanges.find { it.type == type && it.start <= selection.start && it.end >= selection.end }
            if (existingRange != null) {
                newRanges.remove(existingRange)
                if (existingRange.start < selection.start) {
                    newRanges.add(FormattingRange(existingRange.start, selection.start, type))
                }
                if (existingRange.end > selection.end) {
                    newRanges.add(FormattingRange(selection.end, existingRange.end, type))
                }
            } else {
                newRanges.add(FormattingRange(selection.start, selection.end, type))
            }
        }
    }

    return newRanges
}


fun toggleInlineFormatting(
    type: FormattingType,
    currentFormatting: Set<FormattingType>,
    onFormatChange: (Set<FormattingType>) -> Unit
) {
    val newFormatting = currentFormatting.toMutableSet()
    if (newFormatting.contains(type)) {
        newFormatting.remove(type)
    } else {
        newFormatting.add(type)
    }
    if (newFormatting.none {
            it in setOf(
                FormattingType.BODY,
                FormattingType.HEADER1,
                FormattingType.HEADER2,
                FormattingType.HEADER3
            )
        }) {
        newFormatting.add(FormattingType.BODY)
    }
    onFormatChange(newFormatting)
}


data class FormattingRange(val start: Int, val end: Int, val type: FormattingType)

enum class FormattingType {
    BODY, BOLD, ITALIC, UNDERLINE, HEADER1, HEADER2, HEADER3
}

fun buildSafeAnnotatedString(
    text: String,
    formattingRanges: List<FormattingRange>
): AnnotatedString {
    return buildAnnotatedString {
        append(text)
        formattingRanges.forEach { range ->
            try {
                if (range.start < text.length && range.end <= text.length && range.start < range.end) {
                    addStyle(getStyleForFormatting(range.type), range.start, range.end)
                }
            } catch (e: Exception) {
                // Log the exception or handle it as needed
                println("Error applying formatting: ${e.message}")
            }
        }
    }
}

