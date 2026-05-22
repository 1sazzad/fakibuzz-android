package com.qarena.android.model

import com.google.gson.JsonElement

data class Suggestion(
    val questionId: Int? = null,
    val questionText: String = "",
    val stem: String? = null,
    val topic: String? = null,
    val marks: Int? = null,
    val year: Int? = null,
    val examYear: Int? = null,
    val predictionScore: Double? = null,
    val paperType: String? = null,
    val section: String? = null,
    val questionType: String? = null,
    val instruction: String? = null,
    val tableData: JsonElement? = null,
    val wordBox: JsonElement? = null,
    val subQuestions: List<SubQuestion>? = null,
    val options: List<String>? = null,
    val diagramRequired: Boolean? = null,
    val diagramReference: String? = null,
    val diagramDescription: String? = null,
    val mathBlocks: JsonElement? = null
)
