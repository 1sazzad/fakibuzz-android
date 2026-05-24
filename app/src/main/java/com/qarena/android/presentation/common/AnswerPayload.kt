package com.qarena.android.presentation.common

import com.google.gson.JsonElement
import com.qarena.android.data.remote.dto.GenerateAnswerRequest

data class AnswerPayload(
    val questionId: Int? = null,
    val questionText: String = "",
    val subjectCode: String = "",
    val academicLevel: String? = null,
    val paperType: String? = null,
    val topic: String? = null,
    val marks: Int? = null,
    val formulaLatex: String? = null,
    val formulaDisplay: String? = null,
    val diagramRequired: Boolean? = null,
    val diagramType: String? = null,
    val diagramSvg: String? = null,
    val diagramUrl: String? = null,
    val diagramReference: String? = null,
    val diagramDescription: String? = null,
    val mathBlocks: JsonElement? = null,
    val answerType: String? = null,
    val prompt: String? = null,
    val section: String? = null,
    val instruction: String? = null,
    val stem: String? = null,
    val questionType: String? = null,
    val tableData: JsonElement? = null,
    val wordBox: JsonElement? = null
) {
    fun toGenerateAnswerRequest(): GenerateAnswerRequest {
        return GenerateAnswerRequest(
            questionId = questionId,
            questionText = questionText,
            subjectCode = subjectCode,
            academicLevel = academicLevel,
            paperType = paperType,
            marks = marks,
            topic = topic,
            formulaLatex = formulaLatex,
            formulaDisplay = formulaDisplay,
            diagramRequired = diagramRequired,
            diagramType = diagramType,
            diagramSvg = diagramSvg,
            diagramUrl = diagramUrl,
            diagramReference = diagramReference,
            diagramDescription = diagramDescription,
            mathBlocks = mathBlocks,
            answerType = answerType,
            prompt = prompt
        )
    }
}
