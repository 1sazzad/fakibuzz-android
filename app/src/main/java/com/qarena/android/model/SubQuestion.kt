package com.qarena.android.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class SubQuestion(
    @SerializedName("label")
    val label: String? = null,

    @SerializedName(value = "question_text", alternate = ["text", "question"])
    val questionText: String? = null,

    @SerializedName("options")
    val options: List<String>? = null,

    @SerializedName("correct_answer")
    val correctAnswer: String? = null,

    @SerializedName(value = "marks", alternate = ["question_marks", "total_marks"])
    val marks: Double? = null,

    @SerializedName("answer_hint")
    val answerHint: String? = null,

    @SerializedName("formula_latex")
    val formulaLatex: String? = null,

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
    val mathBlocks: JsonElement? = null
)
