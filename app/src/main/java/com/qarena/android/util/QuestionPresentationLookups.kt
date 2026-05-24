package com.qarena.android.util

import com.google.gson.JsonElement
import com.qarena.android.model.SubQuestion

object QuestionPresentationLookups {

    private val cqLabels = listOf("ক.", "খ.", "গ.", "ঘ.")

    fun displayQuestionText(primaryText: String?, stem: String? = null): String {
        return primaryText?.trim().takeUnless { it.isNullOrBlank() }
            ?: stem?.trim().takeUnless { it.isNullOrBlank() }
            ?: "No question text"
    }

    fun buildAnswerPrompt(
        questionText: String?,
        section: String? = null,
        questionType: String? = null,
        instruction: String? = null,
        stem: String? = null,
        tableData: JsonElement? = null,
        wordBox: JsonElement? = null,
        options: List<String>? = null,
        subQuestions: List<SubQuestion>? = null,
        formulaLatex: String? = null,
        formulaDisplay: String? = null,
        diagramDescription: String? = null,
        diagramReference: String? = null,
        diagramType: String? = null,
        diagramSvg: String? = null,
        diagramRequired: Boolean? = null,
        mathBlocks: JsonElement? = null
    ): String {
        val lines = mutableListOf<String>()

        section?.trim()?.takeIf { it.isNotBlank() }?.let { lines.add("Section: $it") }
        questionType?.trim()?.takeIf { it.isNotBlank() }?.let { lines.add("Question type: $it") }
        instruction?.trim()?.takeIf { it.isNotBlank() }?.let { lines.add("Instruction: $it") }

        stem?.trim()?.takeIf { it.isNotBlank() }?.let { lines.add(it) }

        questionText?.trim()?.takeIf { it.isNotBlank() }?.let { text ->
            if (lines.isEmpty() || lines.last() != text) {
                lines.add(text)
            }
        }

        wordBox?.takeIf { !it.isJsonNull }?.let { lines.add("Word box: ${formatJsonContent(it)}") }

        options.orEmpty().forEachIndexed { index, option ->
            option.trim().takeIf { it.isNotBlank() }?.let { text ->
                lines.add("${index + 1}. $text")
            }
        }

        subQuestions.orEmpty().forEachIndexed { index, subQuestion ->
            val label = subQuestion.label?.trim()?.takeIf { it.isNotBlank() } ?: cqLabels.getOrNull(index) ?: "${index + 1}."
            val text = displayQuestionText(subQuestion.questionText)
            lines.add("$label $text")

            subQuestion.answerHint?.trim()?.takeIf { it.isNotBlank() }?.let { hint ->
                lines.add("Hint: $hint")
            }

            subQuestion.formulaLatex?.trim()?.takeIf { it.isNotBlank() }?.let { formula ->
                lines.add("Formula: $formula")
            }

            subQuestion.options.orEmpty().forEachIndexed { optionIndex, option ->
                option.trim().takeIf { it.isNotBlank() }?.let { optionText ->
                    lines.add("${optionIndex + 1}. $optionText")
                }
            }

            subQuestion.diagramDescription?.trim()?.takeIf { it.isNotBlank() }?.let { description ->
                lines.add("Diagram: $description")
            }

            subQuestion.diagramReference?.trim()?.takeIf { it.isNotBlank() }?.let { reference ->
                lines.add("Diagram reference: $reference")
            }

            val subQuestionHasSvg = subQuestion.diagramType?.trim()?.equals("svg", ignoreCase = true) == true &&
                !subQuestion.diagramSvg.isNullOrBlank()

            if (subQuestionHasSvg && subQuestion.diagramDescription.isNullOrBlank()) {
                lines.add("Diagram: SVG provided")
            }

            subQuestion.mathBlocks?.takeIf { !it.isJsonNull }?.let { lines.add("Math blocks: ${formatJsonContent(it)}") }

            if (subQuestion.diagramRequired == true) {
                lines.add("Diagram required")
            }
        }

        tableData?.takeIf { !it.isJsonNull }?.let { lines.add("Table data: ${formatJsonContent(it)}") }

        formulaDisplay?.trim()?.takeIf { it.isNotBlank() }?.let { lines.add("Formula: $it") }
        formulaLatex?.trim()?.takeIf { it.isNotBlank() }?.let { lines.add("Formula (LaTeX): $it") }

        diagramDescription?.trim()?.takeIf { it.isNotBlank() }?.let { lines.add("Diagram: $it") }
        diagramReference?.trim()?.takeIf { it.isNotBlank() }?.let { lines.add("Diagram reference: $it") }

        val hasSvgDiagram = diagramType?.trim()?.equals("svg", ignoreCase = true) == true && !diagramSvg.isNullOrBlank()

        if (hasSvgDiagram && diagramDescription.isNullOrBlank()) {
            lines.add("Diagram: SVG provided")
        }

        if (diagramRequired == true) {
            lines.add("Diagram required")
        }

        mathBlocks?.takeIf { !it.isJsonNull }?.let { lines.add("Math blocks: ${formatJsonContent(it)}") }

        return lines.joinToString("\n").trim()
    }

    fun subQuestionLabel(index: Int, label: String? = null): String {
        return label?.trim()?.takeIf { it.isNotBlank() } ?: cqLabels.getOrNull(index) ?: "${index + 1}."
    }

    fun formatMarks(marks: Int?): String? {
        return marks?.toString()
    }

    fun formatMarks(marks: Double?): String? {
        if (marks == null) {
            return null
        }

        val rounded = marks.toInt()
        return if (marks == rounded.toDouble()) rounded.toString() else marks.toString()
    }

    fun formatJsonContent(value: JsonElement?): String? {
        if (value == null || value.isJsonNull) {
            return null
        }

        return when {
            value.isJsonPrimitive -> value.asJsonPrimitive.toString().trim('"')
            value.isJsonArray -> value.asJsonArray.joinToString(", ") { element ->
                formatJsonContent(element) ?: element.toString()
            }
            else -> value.toString()
        }.takeIf { it.isNotBlank() }
    }
}
