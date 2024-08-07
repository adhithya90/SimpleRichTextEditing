package com.adhi.simplerichtextediting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeCompilerApi

@Composable
fun RichPreviousEditor() {
    //        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceEvenly
//        ) {
//            FormatButton("Body", FormattingType.BODY, currentFormatting) {
//                if (selection.start != selection.end) {
//                    formattingRanges =
//                        applyFormatting(FormattingType.BODY, formattingRanges, selection)
//                }
//                currentFormatting = setOf(FormattingType.BODY)
//            }
//            FormatButton("B", FormattingType.BOLD, currentFormatting) {
//                val result = toggleFormatting(
//                    FormattingType.BOLD,
//                    formattingRanges,
//                    selection,
//                    currentFormatting
//                )
//                formattingRanges = result.first
//                currentFormatting = result.second
//            }
//            FormatButton("I", FormattingType.ITALIC, currentFormatting) {
//                val result = toggleFormatting(
//                    FormattingType.ITALIC,
//                    formattingRanges,
//                    selection,
//                    currentFormatting
//                )
//                formattingRanges = result.first
//                currentFormatting = result.second
//            }
//            FormatButton("U", FormattingType.UNDERLINE, currentFormatting) {
//                val result = toggleFormatting(
//                    FormattingType.UNDERLINE,
//                    formattingRanges,
//                    selection,
//                    currentFormatting
//                )
//                formattingRanges = result.first
//                currentFormatting = result.second
//            }
//            FormatButton("H1", FormattingType.HEADER1, currentFormatting) {
//                if (selection.start != selection.end) {
//                    formattingRanges =
//                        applyFormatting(FormattingType.HEADER1, formattingRanges, selection)
//                }
//                currentFormatting = setOf(FormattingType.HEADER1)
//            }
//            FormatButton("H2", FormattingType.HEADER2, currentFormatting) {
//                if (selection.start != selection.end) {
//                    formattingRanges =
//                        applyFormatting(FormattingType.HEADER2, formattingRanges, selection)
//                }
//                currentFormatting = setOf(FormattingType.HEADER2)
//            }
//            FormatButton("H3", FormattingType.HEADER3, currentFormatting) {
//                if (selection.start != selection.end) {
//                    formattingRanges =
//                        applyFormatting(FormattingType.HEADER3, formattingRanges, selection)
//                }
//                currentFormatting = setOf(FormattingType.HEADER3)
//            }
//        }
}