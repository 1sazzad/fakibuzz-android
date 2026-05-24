package com.qarena.android.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class SearchRequest(
    val query: String,

    @SerializedName("subject_code")
    val subjectCode: String? = null,

    @SerializedName("academic_level")
    val academicLevel: String? = null,

    @SerializedName("paper_type")
    val paperType: String? = null,

    @SerializedName("university_id")
    val universityId: Int? = null,

    @SerializedName("department_id")
    val departmentId: Int? = null,

    @SerializedName("top_k")
    val topK: Int? = null
)

data class SearchResponse(
    val results: List<SearchResultResponse>? = null,
    val questions: List<SearchResultResponse>? = null,
    val message: String? = null,
    val status: String? = null
)

data class SearchResultResponse(
    @SerializedName("question_id")
    val questionId: Int? = null,

    @SerializedName("question_text")
    val questionText: String? = null,

    @SerializedName("subject_code")
    val subjectCode: String? = null,

    @SerializedName("subject_name")
    val subjectName: String? = null,

    @SerializedName("academic_level")
    val academicLevel: String? = null,

    @SerializedName("paper_type")
    val paperType: String? = null,

    @SerializedName("answer_type")
    val answerType: String? = null,

    @SerializedName("department_id")
    val departmentId: Int? = null,

    @SerializedName("university_id")
    val universityId: Int? = null,

    val topic: String? = null,
    val marks: Int? = null,
    val score: Double? = null,

    @SerializedName("similarity_score")
    val similarityScore: Double? = null,

    val year: Int? = null,

    @SerializedName("exam_year")
    val examYear: Int? = null,

    @SerializedName("diagram_required")
    val diagramRequired: Boolean? = null,

    @SerializedName("diagram_type")
    val diagramType: String? = null,

    @SerializedName("diagram_svg")
    val diagramSvg: String? = null,

    @SerializedName("diagram_url")
    val diagramUrl: String? = null,

    @SerializedName("diagram_description")
    val diagramDescription: String? = null,

    @SerializedName("diagram_reference")
    val diagramReference: String? = null,

    @SerializedName("formula_latex")
    val formulaLatex: String? = null,

    @SerializedName("formula_display")
    val formulaDisplay: String? = null,

    @SerializedName("math_blocks")
    val mathBlocks: JsonElement? = null
)
