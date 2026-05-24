package com.qarena.android.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.qarena.android.model.Suggestion
import com.qarena.android.model.SubQuestion

data class SubjectOverviewResponse(
    val subject: SubjectResponse? = null,

    @SerializedName("subject_code")
    val subjectCode: String? = null,

    @SerializedName("subject_name")
    val subjectName: String? = null,

    @SerializedName("question_count")
    val questionCount: Int? = null,

    @SerializedName("total_questions")
    val totalQuestions: Int? = null,

    val topics: List<String>? = null,
    val years: List<Int>? = null,
    val summary: String? = null
)

data class SuggestionDto(
    @SerializedName("paper_type")
    val paper_type: String? = null,

    @SerializedName("question_id")
    val question_id: Int? = null,

    @SerializedName("section")
    val section: String? = null,

    @SerializedName("question_type")
    val question_type: String? = null,

    @SerializedName("instruction")
    val instruction: String? = null,

    @SerializedName("table_data")
    val table_data: JsonElement? = null,

    @SerializedName("word_box")
    val word_box: JsonElement? = null,

    @SerializedName("stem")
    val stem: String? = null,

    @SerializedName("question_text")
    val question_text: String? = null,

    @SerializedName(value = "topic", alternate = ["final_topic", "suggested_topic"])
    val topic: String? = null,

    @SerializedName(value = "marks", alternate = ["total_marks", "expected_marks"])
    val marks: Int? = null,

    @SerializedName(value = "year", alternate = ["exam_year"])
    val year: Int? = null,

    @SerializedName("exam_year")
    val exam_year: Int? = null,

    @SerializedName("sub_questions")
    val sub_questions: List<SubQuestion>? = null,

    @SerializedName("options")
    val options: List<String>? = null,

    @SerializedName("correct_answer")
    val correct_answer: String? = null,

    @SerializedName("reason")
    val reason: String? = null,

    @SerializedName("suggestion_no")
    val suggestion_no: String? = null,

    @SerializedName("diagram_required")
    val diagram_required: Boolean? = null,

    @SerializedName("diagram_type")
    val diagram_type: String? = null,

    @SerializedName("diagram_svg")
    val diagram_svg: String? = null,

    @SerializedName("diagram_url")
    val diagram_url: String? = null,

    @SerializedName("diagram_reference")
    val diagram_reference: String? = null,

    @SerializedName("diagram_description")
    val diagram_description: String? = null,

    @SerializedName("formula_latex")
    val formula_latex: String? = null,

    @SerializedName("formula_display")
    val formula_display: String? = null,

    @SerializedName("math_blocks")
    val math_blocks: JsonElement? = null,

    @SerializedName("prediction_score")
    val prediction_score: Double? = null
)

data class SuggestionsResponse(
    @SerializedName(value = "success", alternate = ["ok"])
    val success: Boolean? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName(value = "suggestions", alternate = ["items"])
    val suggestions: List<Suggestion>? = null,

    @SerializedName("data")
    val data: List<Suggestion>? = null,

    @SerializedName("results")
    val results: List<Suggestion>? = null,

    @SerializedName("warning")
    val warning: String? = null,

    @SerializedName("fallback_warning")
    val fallbackWarning: String? = null,

    @SerializedName("retrieval_source")
    val retrievalSource: String? = null,

    @SerializedName("fallback_used")
    val fallbackUsed: Boolean? = null,

    @SerializedName("diagnostics")
    val diagnostics: JsonElement? = null
)

data class SubjectAnalysisResponse(
    @SerializedName("subject_code")
    val subjectCode: String? = null,

    @SerializedName("subject_name")
    val subjectName: String? = null,

    @SerializedName("total_questions")
    val totalQuestions: Int? = null,

    val topics: JsonElement? = null,
    val marks: JsonElement? = null,
    val years: JsonElement? = null,
    val samples: JsonElement? = null,
    val summary: String? = null,
    val message: String? = null,
    val status: String? = null
)

data class PredictionDto(
    @SerializedName("question_text")
    val question_text: String? = null,

    @SerializedName("question_id")
    val question_id: Int? = null,

    @SerializedName(value = "topic", alternate = ["final_topic", "suggested_topic"])
    val topic: String? = null,

    @SerializedName(value = "marks", alternate = ["total_marks", "expected_marks"])
    val marks: Int? = null,

    @SerializedName(value = "year", alternate = ["exam_year"])
    val year: Int? = null,

    @SerializedName("prediction_score")
    val prediction_score: Double? = null

    ,@SerializedName("diagram_required")
    val diagram_required: Boolean? = null,

    @SerializedName("diagram_type")
    val diagram_type: String? = null,

    @SerializedName("diagram_svg")
    val diagram_svg: String? = null,

    @SerializedName("diagram_url")
    val diagram_url: String? = null,

    @SerializedName("diagram_description")
    val diagram_description: String? = null,

    @SerializedName("diagram_reference")
    val diagram_reference: String? = null,

    @SerializedName("formula_latex")
    val formula_latex: String? = null,

    @SerializedName("formula_display")
    val formula_display: String? = null
)

data class PredictionsResponse(
    val success: Boolean? = null,
    val items: List<PredictionDto>? = null,
    val predictions: List<PredictionDto>? = null
)
