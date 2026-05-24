package com.qarena.android.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.qarena.android.model.SubQuestion

data class SubQuestionDto(
    @SerializedName("question_no")
    val questionNo: String? = null,

    @SerializedName("question_text")
    val questionText: String? = null,

    @SerializedName("marks")
    val marks: Int? = null,

    @SerializedName("topic")
    val topic: String? = null,

    @SerializedName("diagram_required")
    val diagramRequired: Boolean? = null,

    @SerializedName("diagram_svg")
    val diagramSvg: String? = null
)

data class QuestionDto(
    @SerializedName("question_id")
    val questionId: Int? = null,

    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("question_text")
    val questionText: String? = null,

    @SerializedName("stem")
    val stem: String? = null,

    @SerializedName("topic")
    val topic: String? = null,

    @SerializedName("marks")
    val marks: Int? = null,

    @SerializedName("year")
    val year: Int? = null,

    @SerializedName("exam_year")
    val examYear: Int? = null,

    @SerializedName("paper_type")
    val paperType: String? = null,

    @SerializedName("section")
    val section: String? = null,

    @SerializedName("question_type")
    val questionType: String? = null,

    @SerializedName("instruction")
    val instruction: String? = null,

    @SerializedName("table_data")
    val tableData: JsonElement? = null,

    @SerializedName("word_box")
    val wordBox: JsonElement? = null,

    @SerializedName("sub_questions")
    val subQuestions: List<SubQuestionDto>? = null,

    @SerializedName("options")
    val options: List<String>? = null,

    @SerializedName("correct_answer")
    val correctAnswer: String? = null,

    @SerializedName("diagram_required")
    val diagramRequired: Boolean? = null,

    @SerializedName("diagram_type")
    val diagramType: String? = null,

    @SerializedName("diagram_svg")
    val diagramSvg: String? = null
)

fun SubQuestionDto.toSubQuestion(): SubQuestion {
    return SubQuestion(
        label = questionNo,
        questionText = questionText,
        marks = marks?.toDouble(),
        diagramRequired = diagramRequired,
        diagramSvg = diagramSvg
    )
}

fun QuestionDto.toQuestionResponse(): QuestionResponse {
    return QuestionResponse(
        id = questionId ?: id,
        questionNo = null,
        paperType = paperType,
        section = section,
        questionType = questionType,
        instruction = instruction,
        tableData = tableData,
        wordBox = wordBox,
        stem = stem,
        questionText = questionText,
        year = year,
        examYear = examYear,
        marks = marks,
        topic = topic,
        options = options,
        correctAnswer = correctAnswer,
        subQuestions = subQuestions?.map { it.toSubQuestion() },
        diagramRequired = diagramRequired,
        diagramType = diagramType,
        diagramSvg = diagramSvg
    )
}