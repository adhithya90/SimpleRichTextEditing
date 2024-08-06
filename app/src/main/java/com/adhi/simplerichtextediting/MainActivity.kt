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


    Column(modifier = Modifier.padding(16.dp)) {
        BasicTextField(
            value = TextFieldValue(
                annotatedString = buildAnnotatedString {
                    append(text)
                    formattingRanges.forEach { formattingRange ->
                        val style = when (formattingRange.type) {
                            FormattingType.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
                            FormattingType.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
                            FormattingType.UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
                            FormattingType.HEADER1 -> SpanStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            FormattingType.HEADER2 -> SpanStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            FormattingType.HEADER3 -> SpanStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        addStyle(
                            style = style,
                            start = formattingRange.start,
                            end = formattingRange.end
                        )
                    }
                }, selection = selection
            ),
            onValueChange = { newValue ->
                text = newValue.text
                selection = newValue.selection
                formattingRanges = updateFormattingRanges(formattingRanges, text, newValue.text)
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
            FormatButton("B", FormattingType.BOLD, formattingRanges, selection) {
                formattingRanges = it
            }
            FormatButton("I", FormattingType.ITALIC, formattingRanges, selection) {
                formattingRanges = it
            }
            FormatButton("U", FormattingType.UNDERLINE, formattingRanges, selection) {
                formattingRanges = it
            }
            FormatButton("H1", FormattingType.HEADER1, formattingRanges, selection) {
                formattingRanges = it
            }
            FormatButton("H2", FormattingType.HEADER2, formattingRanges, selection) {
                formattingRanges = it
            }
            FormatButton("H3", FormattingType.HEADER3, formattingRanges, selection) {
                formattingRanges = it
            }
        }

    }
}

@Composable
fun FormatButton(
    text: String,
    formattingType: FormattingType,
    currentRange: List<FormattingRange>,
    selection: TextRange,
    onFormatChange: (List<FormattingRange>) -> Unit
) {
    val isActive = currentRange.any { range ->
        range.type == formattingType &&
                range.start <= selection.end &&
                range.end >= selection.start
    }

    Button(
        onClick = {
            val newRanges = currentRange.toMutableList()
            if (isActive) {
                newRanges.removeAll { range ->
                    range.type == formattingType &&
                            range.start <= selection.start &&
                            range.end >= selection.end
                }
            } else {
                newRanges.add(FormattingRange(selection.start, selection.end, formattingType))
            }
            onFormatChange(newRanges)
        }, colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) Color.Blue else Color.Gray,
            contentColor = Color.White
        )
    ) {
        Text(text = text)
    }

}

fun updateFormattingRanges(
    oldRanges: List<FormattingRange>,
    oldText: String,
    newText: String
): List<FormattingRange> {
    val diffIndex = oldText.zip(newText).indexOfFirst { it.first != it.second }.let {
        if (it == -1) minOf(oldText.length, newText.length) else it
    }
    val lengthDiff = newText.length - oldText.length

    return oldRanges.map { range ->
        when {
            range.end <= diffIndex -> range
            range.start >= diffIndex -> range.copy(
                start = (range.start + lengthDiff).coerceAtLeast(0),
                end = (range.end + lengthDiff).coerceAtLeast(0)
            )

            else -> range.copy(
                end = (range.end + lengthDiff).coerceAtLeast(range.start)
            )
        }
    }.filter { it.start < it.end }
}


data class FormattingRange(val start: Int, val end: Int, val type: FormattingType)

enum class FormattingType {
    BOLD, ITALIC, UNDERLINE, HEADER1, HEADER2, HEADER3
}

