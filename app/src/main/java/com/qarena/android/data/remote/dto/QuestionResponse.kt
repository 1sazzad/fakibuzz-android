package com.qarena.android.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.qarena.android.model.SubQuestion

data class QuestionResponse(
    val id: Int? = null,

    @SerializedName("question_no")
    val questionNo: String? = null,

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

    @SerializedName("stem")
    val stem: String? = null,

    @SerializedName("question_text")
    val questionText: String? = null,

    val marks: Int? = null,
    val topic: String? = null,

    @SerializedName("options")
    val options: List<String>? = null,

    @SerializedName("correct_answer")
    val correctAnswer: String? = null,

    @SerializedName("sub_questions")
    val subQuestions: List<SubQuestion>? = null,

    @SerializedName("formula_latex")
    val formulaLatex: String? = null,

    @SerializedName("formula_display")
    val formulaDisplay: String? = null,

    @SerializedName("diagram_required")
    val diagramRequired: Boolean? = null,

    @SerializedName("diagram_reference")
    val diagramReference: String? = null,

    @SerializedName("diagram_description")
    val diagramDescription: String? = null,

    @SerializedName("math_blocks")
    val mathBlocks: JsonElement? = null,

    @SerializedName("exam_year")
    val examYear: Int? = null,

    @SerializedName("subject_code")
    val subjectCode: String? = null
)
