package com.qarena.android.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class GenerateAnswerRequest(
    @SerializedName("question_id")
    val questionId: Int? = null,

    @SerializedName("question_text")
    val questionText: String? = null,

    @SerializedName("subject_code")
    val subjectCode: String? = null,

    @SerializedName("academic_level")
    val academicLevel: String? = null,

    @SerializedName("paper_type")
    val paperType: String? = null,

    val marks: Int? = null,
    val topic: String? = null,

    @SerializedName("answer_type")
    val answerType: String? = null,

    @SerializedName("university_id")
    val universityId: Int? = null,

    @SerializedName("department_id")
    val departmentId: Int? = null,

    @SerializedName("formula_latex")
    val formulaLatex: String? = null,

    @SerializedName("formula_display")
    val formulaDisplay: String? = null,

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

    @SerializedName("math_blocks")
    val mathBlocks: JsonElement? = null,

    val prompt: String? = null
)

data class GenerateAnswerResponse(
    val answer: String? = null,

    @SerializedName("job_id")
    val jobId: String? = null,

    val status: String? = null,
    val message: String? = null
)

data class JobResponse(
    @SerializedName("job_id")
    val jobId: String? = null,

    val status: String? = null,
    val message: String? = null,
    val result: JsonElement? = null,
    val answer: String? = null,
    val error: String? = null,
    val progress: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)
