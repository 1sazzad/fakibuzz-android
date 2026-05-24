package com.qarena.android.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class Suggestion(
    @SerializedName(value = "question_id", alternate = ["id"])
    val questionId: Int? = null,

    @SerializedName(value = "question_text", alternate = ["text", "title", "prompt", "suggestion"])
    val questionText: String = "",

    @SerializedName("stem")
    val stem: String? = null,

    @SerializedName(value = "topic", alternate = ["final_topic", "suggested_topic"])
    val topic: String? = null,

    @SerializedName(value = "marks", alternate = ["total_marks", "expected_marks"])
    val marks: Int? = null,

    @SerializedName("year")
    val year: Int? = null,

    @SerializedName("exam_year")
    val examYear: Int? = null,

    @SerializedName(
        value = "prediction_score",
        alternate = ["importance", "probability_score", "importance_score", "score", "confidence", "probability", "frequency"]
    )
    val predictionScore: Double? = null,

    @SerializedName("paper_type")
    val paperType: String? = null,

    @SerializedName("section")
    val section: String? = null,

    @SerializedName("question_type")
    val questionType: String? = null,

    @SerializedName("correct_answer")
    val correctAnswer: String? = null,

    @SerializedName("instruction")
    val instruction: String? = null,

    @SerializedName("table_data")
    val tableData: JsonElement? = null,

    @SerializedName("word_box")
    val wordBox: JsonElement? = null,

    @SerializedName("sub_questions")
    val subQuestions: List<SubQuestion>? = null,

    @SerializedName("options")
    val options: List<String>? = null,

    @SerializedName("diagram_required")
    val diagramRequired: Boolean? = null,

    @SerializedName("diagram_type")
    val diagramType: String? = null,

    @SerializedName("diagram_svg")
    val diagramSvg: String? = null,

    @SerializedName("diagram_url")
    val diagramUrl: String? = null,

    @SerializedName("diagram_reference")
    val diagramReference: String? = null,

    @SerializedName("diagram_description")
    val diagramDescription: String? = null,

    @SerializedName("formula_latex")
    val formulaLatex: String? = null,

    @SerializedName("formula_display")
    val formulaDisplay: String? = null,

    @SerializedName("math_blocks")
    val mathBlocks: JsonElement? = null,

    @SerializedName("reason")
    val reason: String? = null,

    @SerializedName("suggestion_no")
    val suggestionNo: String? = null
)

fun Suggestion.displayQuestionText(): String {
    return questionText.trim().takeIf { it.isNotBlank() }
        ?: stem?.trim()?.takeIf { it.isNotBlank() }
        ?: reason?.trim()?.takeIf { it.isNotBlank() }
        ?: suggestionNo?.trim()?.takeIf { it.isNotBlank() }
        ?: "No question text"
}

fun Suggestion.displayTopic(): String? {
    return topic?.trim()?.takeIf { it.isNotBlank() }
}

fun Suggestion.displayMarks(): String? {
    return marks?.toString()
}

fun Suggestion.displayScore(): String? {
    val score = predictionScore ?: return null
    return if (score.isNaN() || score.isInfinite()) null else String.format("%.2f", score)
}

fun Suggestion.displayPaperType(): String? {
    return paperType?.trim()?.takeIf { it.isNotBlank() }
}

fun com.qarena.android.data.remote.dto.QuestionResponse.toSuggestion(): Suggestion {
    return Suggestion(
        questionId = id,
        questionText = questionText?.trim()?.takeIf { it.isNotBlank() }
            ?: stem?.trim()?.takeIf { it.isNotBlank() }
            ?: "",
        stem = stem,
        topic = topic,
        marks = marks,
        year = year ?: examYear,
        examYear = examYear ?: year,
        paperType = paperType,
        section = section,
        questionType = questionType,
        instruction = instruction,
        tableData = tableData,
        wordBox = wordBox,
        subQuestions = subQuestions,
        options = options,
        correctAnswer = correctAnswer,
        diagramRequired = diagramRequired,
        diagramType = diagramType,
        diagramSvg = diagramSvg,
        diagramUrl = diagramUrl,
        diagramReference = diagramReference,
        diagramDescription = diagramDescription,
        formulaLatex = formulaLatex,
        formulaDisplay = formulaDisplay,
        mathBlocks = mathBlocks
    )
}
