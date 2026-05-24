package com.qarena.android.util

import com.qarena.android.data.remote.dto.PredictionDto
import com.qarena.android.data.remote.dto.PredictionsResponse
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

        return response.suggestions ?: response.data ?: response.results ?: emptyList()
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
            options = null,
            diagramRequired = diagram_required,
            diagramType = diagram_type?.trim()?.takeIf { it.isNotBlank() },
            diagramSvg = diagram_svg?.trim()?.takeIf { it.isNotBlank() },
            diagramUrl = diagram_url?.trim()?.takeIf { it.isNotBlank() },
            diagramReference = diagram_reference?.trim()?.takeIf { it.isNotBlank() },
            diagramDescription = diagram_description?.trim()?.takeIf { it.isNotBlank() },
            formulaLatex = formula_latex?.trim()?.takeIf { it.isNotBlank() },
            formulaDisplay = formula_display?.trim()?.takeIf { it.isNotBlank() },
            mathBlocks = null
        )
    }
}
