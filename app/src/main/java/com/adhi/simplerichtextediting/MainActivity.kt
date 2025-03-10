package com.adhi.simplerichtextediting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    var showFormattingToolbar by remember { mutableStateOf(false) }

    val view = LocalView.current
    val density = LocalDensity.current

    var keyboardHeight by remember { mutableStateOf(0.dp) }
    var isKeyboardVisible by remember { mutableStateOf(false) }

    DisposableEffect(view) {
        val listener = ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            isKeyboardVisible = imeVisible
            keyboardHeight = with(density) { imeHeight.toDp() }
            insets
        }
        onDispose {
            ViewCompat.setOnApplyWindowInsetsListener(view, null)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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

                    // Check if return was hit
                    if (addedLength == 1 && newValue.text[oldLength] == '\n') {
                        if (isFirstLine) {
                            isFirstLine = false
                            currentFormatting = setOf(FormattingType.BODY)
                        } else {
                            val lastRange = formattingRanges.lastOrNull { it.end == oldLength }
                            if (lastRange?.type in setOf(
                                    FormattingType.HEADER1,
                                    FormattingType.HEADER2,
                                    FormattingType.HEADER3
                                )
                            ) {
                                currentFormatting = setOf(FormattingType.BODY)
                            }
                        }
                        formattingRanges = formattingRanges + FormattingRange(
                            oldLength + 1,
                            newValue.text.length,
                            FormattingType.BODY
                        )
                    }
                } else {
                    // Text was removed or replaced
                    val removedLength = oldLength - newValue.text.length
                    formattingRanges =
                        updateFormattingRanges(formattingRanges, selection.start, -removedLength)
                }

                // Update current formatting based on selection
                if (selection.start != selection.end) {
                    currentFormatting = getCurrentFormatting(formattingRanges, selection)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(800.dp),
            textStyle = TextStyle(Color.White, fontWeight = FontWeight.Thin, fontSize = 25.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            keyboardActions = KeyboardActions(onDone = { showFormattingToolbar = false })
        )

        if (showFormattingToolbar) {
            FormattingToolbar(
                currentFormatting = currentFormatting,
                onFormatChange = { type ->
                    val result =
                        toggleFormatting(type, formattingRanges, selection, currentFormatting)
                    formattingRanges = result.first
                    currentFormatting = result.second
                },
                onClose = { showFormattingToolbar = false },
                modifier = Modifier
                    .align(if (isKeyboardVisible) Alignment.BottomEnd else Alignment.BottomEnd)
                    .padding(16.dp)
                    .offset(y = if (isKeyboardVisible) -keyboardHeight else 0.dp)

            )
        } else {
            IconButton(
                onClick = { showFormattingToolbar = true },
                modifier = Modifier
                    .align(if (isKeyboardVisible) Alignment.BottomEnd else Alignment.BottomEnd)
                    .padding(16.dp)
                    .offset(y = if (isKeyboardVisible) -keyboardHeight else 0.dp)
            ) {
                Icon(
                    Icons.Default.FormatSize,
                    contentDescription = "Open formatting toolbar"
                )
            }

        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun FormattingToolbar(
    currentFormatting: Set<FormattingType>,
    onFormatChange: (FormattingType) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.DarkGray,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Format", style = MaterialTheme.typography.titleMedium, color = Color.White)
                IconButton(onClick = onClose) {
                    Text(
                        "×",
                        color = Color.White,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FormatButton("Title", FormattingType.HEADER1, currentFormatting, onFormatChange)
                FormatButton("Heading", FormattingType.HEADER2, currentFormatting, onFormatChange)
                FormatButton(
                    "Subheading",
                    FormattingType.HEADER3,
                    currentFormatting,
                    onFormatChange
                )
                FormatButton("Body", FormattingType.BODY, currentFormatting, onFormatChange)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                FormatButton("B", FormattingType.BOLD, currentFormatting, onFormatChange)
                Spacer(modifier = Modifier.width(16.dp))
                FormatButton("I", FormattingType.ITALIC, currentFormatting, onFormatChange)
                Spacer(modifier = Modifier.width(16.dp))
                FormatButton("U", FormattingType.UNDERLINE, currentFormatting, onFormatChange)
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
    onFormatChange: (FormattingType) -> Unit
) {
    val isActive = currentFormatting.contains(formattingType)
    Button(
        onClick = { onFormatChange(formattingType) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) Color.Yellow else Color.LightGray
        ), modifier = Modifier.height(36.dp)
    ) {
        Text(text, color = if (isActive) Color.Black else Color.White)
    }
}


fun updateFormattingRanges(
    oldRanges: List<FormattingRange>,
    changeIndex: Int,
    lengthDiff: Int
): List<FormattingRange> {
    return oldRanges.flatMap { range ->
        when {
            range.end <= changeIndex -> listOf(range)  // Range is before the change, keep it as is
            range.start >= changeIndex -> listOf(
                range.copy(
                    start = (range.start + lengthDiff).coerceAtLeast(changeIndex),
                    end = (range.end + lengthDiff).coerceAtLeast(changeIndex)
                )
            )  // Range is after the change, shift it
            else -> {
                // Range overlaps with the change
                listOfNotNull(
                    range.copy(end = changeIndex),  // Keep the part before the change
                    if (range.end > changeIndex + lengthDiff.absoluteValue)
                        range.copy(
                            start = changeIndex + lengthDiff.coerceAtLeast(0),
                            end = range.end + lengthDiff
                        )
                    else null  // Keep the part after the change, if it exists
                )
            }
        }
    }.filter { it.start < it.end }  // Remove any invalid ranges
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
    val blockFormats = setOf(FormattingType.BODY, FormattingType.HEADER1, FormattingType.HEADER2, FormattingType.HEADER3)
    val inlineFormats = setOf(FormattingType.BOLD, FormattingType.ITALIC, FormattingType.UNDERLINE)

    val newFormatting = currentFormatting.toMutableSet()
    val newRanges = ranges.toMutableList()

    when (type) {
        FormattingType.BODY,
        FormattingType.HEADER1,
        FormattingType.HEADER2,
        FormattingType.HEADER3 -> {
            // Remove all block formats from current formatting
            newFormatting.removeAll(blockFormats)
            // Add the new block format
            newFormatting.add(type)

            if (selection.start != selection.end) {
                // Remove existing block formatting in the selection
                newRanges.removeAll { it.type in blockFormats && it.start < selection.end && it.end > selection.start }
                // Add new block formatting
                newRanges.add(FormattingRange(selection.start, selection.end, type))
            }
        }
        FormattingType.BOLD,
        FormattingType.ITALIC,
        FormattingType.UNDERLINE -> {
            if (newFormatting.contains(type)) {
                newFormatting.remove(type)
                if (selection.start != selection.end) {
                    // Remove the inline formatting from the selection
                    newRanges.removeAll { it.type == type && it.start < selection.end && it.end > selection.start }
                }
            } else {
                newFormatting.add(type)
                if (selection.start != selection.end) {
                    // Add the inline formatting to the selection
                    newRanges.add(FormattingRange(selection.start, selection.end, type))
                }
            }
        }
    }

    // Ensure there's always a block format
    if (newFormatting.intersect(blockFormats).isEmpty()) {
        newFormatting.add(FormattingType.BODY)
    }

    return Pair(newRanges, newFormatting)
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

