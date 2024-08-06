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
import kotlin.math.absoluteValue


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
                    formattingRanges = updateFormattingRanges(formattingRanges, oldLength, addedLength) + newRanges

                    // Check if return was hit
                    if (addedLength == 1 && newValue.text[oldLength] == '\n') {
                        if (isFirstLine) {
                            isFirstLine = false
                            currentFormatting = setOf(FormattingType.BODY)
                        } else {
                            val lastRange = formattingRanges.lastOrNull { it.end == oldLength }
                            if (lastRange?.type in setOf(FormattingType.HEADER1, FormattingType.HEADER2, FormattingType.HEADER3)) {
                                currentFormatting = setOf(FormattingType.BODY)
                            }
                        }
                        formattingRanges = formattingRanges + FormattingRange(oldLength + 1, newValue.text.length, FormattingType.BODY)
                    }
                } else {
                    // Text was removed or replaced
                    val removedLength = oldLength - newValue.text.length
                    formattingRanges = updateFormattingRanges(formattingRanges, selection.start, -removedLength)
                }

                // Update current formatting based on selection
                if (selection.start != selection.end) {
                    currentFormatting = getCurrentFormatting(formattingRanges, selection)
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
            FormatButton("Body", FormattingType.BODY, currentFormatting) {
                if (selection.start != selection.end) {
                    formattingRanges = applyFormatting(FormattingType.BODY, formattingRanges, selection)
                }
                currentFormatting = setOf(FormattingType.BODY)
            }
            FormatButton("B", FormattingType.BOLD, currentFormatting) {
                val result = toggleFormatting(FormattingType.BOLD, formattingRanges, selection, currentFormatting)
                formattingRanges = result.first
                currentFormatting = result.second
            }
            FormatButton("I", FormattingType.ITALIC, currentFormatting) {
                val result = toggleFormatting(FormattingType.ITALIC, formattingRanges, selection, currentFormatting)
                formattingRanges = result.first
                currentFormatting = result.second
            }
            FormatButton("U", FormattingType.UNDERLINE, currentFormatting) {
                val result = toggleFormatting(FormattingType.UNDERLINE, formattingRanges, selection, currentFormatting)
                formattingRanges = result.first
                currentFormatting = result.second
            }
            FormatButton("H1", FormattingType.HEADER1, currentFormatting) {
                if (selection.start != selection.end) {
                    formattingRanges = applyFormatting(FormattingType.HEADER1, formattingRanges, selection)
                }
                currentFormatting = setOf(FormattingType.HEADER1)
            }
            FormatButton("H2", FormattingType.HEADER2, currentFormatting) {
                if (selection.start != selection.end) {
                    formattingRanges = applyFormatting(FormattingType.HEADER2, formattingRanges, selection)
                }
                currentFormatting = setOf(FormattingType.HEADER2)
            }
            FormatButton("H3", FormattingType.HEADER3, currentFormatting) {
                if (selection.start != selection.end) {
                    formattingRanges = applyFormatting(FormattingType.HEADER3, formattingRanges, selection)
                }
                currentFormatting = setOf(FormattingType.HEADER3)
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
    onFormatChange: () -> Unit
) {
    val isActive = currentFormatting.contains(formattingType)

    Button(
        onClick = onFormatChange,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) Color.Yellow else Color.White
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
    return oldRanges.flatMap { range ->
        when {
            range.end <= changeIndex -> listOf(range)
            range.start >= changeIndex -> listOf(
                range.copy(
                    start = (range.start + lengthDiff).coerceAtLeast(changeIndex),
                    end = (range.end + lengthDiff).coerceAtLeast(changeIndex)
                )
            )
            else -> {
                val newRanges = mutableListOf<FormattingRange>()
                if (range.start < changeIndex) {
                    newRanges.add(range.copy(end = changeIndex))
                }
                if (range.end > changeIndex + lengthDiff.absoluteValue) {
                    newRanges.add(
                        range.copy(
                            start = changeIndex,
                            end = range.end + lengthDiff
                        )
                    )
                }
                newRanges
            }
        }
    }.filter { it.start < it.end }
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


fun toggleFormatting(
    type: FormattingType,
    ranges: List<FormattingRange>,
    selection: TextRange,
    currentFormatting: Set<FormattingType>
): Pair<List<FormattingRange>, Set<FormattingType>> {
    if (selection.start != selection.end) {
        // Apply to selection
        val newRanges = applyFormatting(type, ranges, selection)
        return Pair(newRanges, getCurrentFormatting(newRanges, selection))
    } else {
        // Toggle for future typing
        val newFormatting = currentFormatting.toMutableSet()
        if (newFormatting.contains(type)) {
            newFormatting.remove(type)
        } else {
            newFormatting.add(type)
        }
        return Pair(ranges, newFormatting)
    }
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

fun getCurrentFormatting(ranges: List<FormattingRange>, selection: TextRange): Set<FormattingType> {
    return ranges.filter { it.start <= selection.start && it.end > selection.start }
        .map { it.type }
        .toSet()
        .ifEmpty { setOf(FormattingType.BODY) }
}

