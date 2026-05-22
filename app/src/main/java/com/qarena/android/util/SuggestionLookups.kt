package com.qarena.android.util

import com.qarena.android.data.remote.dto.PredictionDto
import com.qarena.android.data.remote.dto.PredictionsResponse
import com.qarena.android.data.remote.dto.SuggestionDto
import com.qarena.android.data.remote.dto.SuggestionsResponse
import com.qarena.android.model.Suggestion
import com.qarena.android.util.PaperTypeLookups.normalizePaperType

object SuggestionLookups {

    const val MAX_SUGGESTION_QUERY_LENGTH = 1000
    const val MIN_TOP_K = 1
    const val MAX_TOP_K = 50

    fun normalizeSuggestions(response: SuggestionsResponse?): List<Suggestion> {
        if (response == null) {
            return emptyList()
        }

        val payload = response.items ?: response.suggestions ?: emptyList()
        return payload.map { it.toSuggestion() }
    }

    fun normalizePredictions(response: PredictionsResponse?): List<Suggestion> {
        if (response == null) {
            return emptyList()
        }

        val payload = response.items ?: response.predictions ?: emptyList()
        return payload.map { it.toSuggestion() }
    }

    fun normalizeQuery(rawQuery: String): String {
        return rawQuery.trim().take(MAX_SUGGESTION_QUERY_LENGTH)
    }

    fun clampQueryLength(rawQuery: String): String {
        return rawQuery.take(MAX_SUGGESTION_QUERY_LENGTH)
    }

    fun clampTopK(topK: Int): Int {
        return topK.coerceIn(MIN_TOP_K, MAX_TOP_K)
    }

    fun normalizePredictionScore(score: Double?): Double? {
        if (score == null || score.isNaN() || score.isInfinite()) {
            return null
        }

        return score.coerceIn(0.0, 1.0)
    }

    fun formatPredictionScore(score: Double?): String? {
        val normalized = normalizePredictionScore(score) ?: return null
        return String.format("%.2f", normalized)
    }

    fun displaySafeText(text: String?, fallback: String? = "No question text"): String {
        return text?.trim().takeUnless { it.isNullOrBlank() }
            ?: fallback?.trim().takeUnless { it.isNullOrBlank() }
            ?: "No question text"
    }

    private fun SuggestionDto.toSuggestion(): Suggestion {
        return Suggestion(
            questionId = question_id,
            questionText = displaySafeText(question_text, stem),
            stem = stem?.trim()?.takeIf { it.isNotBlank() },
            topic = topic?.trim()?.takeIf { it.isNotBlank() },
            marks = marks,
            year = year ?: exam_year,
            examYear = exam_year ?: year,
            predictionScore = normalizePredictionScore(prediction_score),
            paperType = normalizePaperType(paper_type),
            section = section?.trim()?.takeIf { it.isNotBlank() },
            questionType = question_type?.trim()?.takeIf { it.isNotBlank() },
            instruction = instruction?.trim()?.takeIf { it.isNotBlank() },
            tableData = table_data?.takeIf { !it.isJsonNull },
            wordBox = word_box?.takeIf { !it.isJsonNull },
            subQuestions = sub_questions,
            options = options,
            diagramRequired = diagram_required,
            diagramReference = diagram_reference?.trim()?.takeIf { it.isNotBlank() },
            diagramDescription = diagram_description?.trim()?.takeIf { it.isNotBlank() },
            mathBlocks = math_blocks?.takeIf { !it.isJsonNull }
        )
    }

    private fun PredictionDto.toSuggestion(): Suggestion {
        return Suggestion(
            questionId = question_id,
            questionText = displaySafeText(question_text),
            stem = null,
            topic = topic?.trim()?.takeIf { it.isNotBlank() },
            marks = marks,
            year = year,
            examYear = year,
            predictionScore = normalizePredictionScore(prediction_score),
            paperType = null,
            subQuestions = null,
            options = null
        )
    }
}
