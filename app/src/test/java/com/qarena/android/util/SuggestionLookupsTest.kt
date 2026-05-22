package com.qarena.android.util

import com.qarena.android.data.remote.dto.PredictionDto
import com.qarena.android.data.remote.dto.PredictionsResponse
import com.qarena.android.data.remote.dto.SuggestionDto
import com.qarena.android.data.remote.dto.SuggestionsResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SuggestionLookupsTest {

    @Test
    fun `items preferred over suggestions`() {
        val item = SuggestionDto(
            question_id = 11,
            question_text = "Item question",
            topic = "Item topic",
            marks = 10,
            year = 2024,
            prediction_score = 0.91
        )
        val legacy = SuggestionDto(
            question_id = 12,
            question_text = "Legacy question",
            topic = "Legacy topic",
            marks = 8,
            year = 2023,
            prediction_score = 0.51
        )

        val result = SuggestionLookups.normalizeSuggestions(
            SuggestionsResponse(
                success = true,
                items = listOf(item),
                suggestions = listOf(legacy)
            )
        )

        assertEquals(1, result.size)
        assertEquals(11, result[0].questionId)
        assertEquals("Item question", result[0].questionText)
    }

    @Test
    fun `suggestions fallback used when items missing`() {
        val legacy = SuggestionDto(
            question_id = 21,
            question_text = "Legacy only",
            topic = "Fallback",
            marks = 7,
            year = 2022,
            prediction_score = 0.45
        )

        val result = SuggestionLookups.normalizeSuggestions(
            SuggestionsResponse(
                success = true,
                items = null,
                suggestions = listOf(legacy)
            )
        )

        assertEquals(1, result.size)
        assertEquals(21, result[0].questionId)
    }

    @Test
    fun `prediction items preferred over predictions`() {
        val item = PredictionDto(
            question_id = 31,
            question_text = "Pred item",
            topic = "Topic A",
            marks = 12,
            year = 2025,
            prediction_score = 0.77
        )
        val legacy = PredictionDto(
            question_id = 32,
            question_text = "Pred legacy",
            topic = "Topic B",
            marks = 8,
            year = 2024,
            prediction_score = 0.55
        )

        val result = SuggestionLookups.normalizePredictions(
            PredictionsResponse(
                success = true,
                items = listOf(item),
                predictions = listOf(legacy)
            )
        )

        assertEquals(1, result.size)
        assertEquals(31, result[0].questionId)
        assertEquals("Pred item", result[0].questionText)
    }

    @Test
    fun `predictions fallback used when items missing`() {
        val legacy = PredictionDto(
            question_id = 41,
            question_text = "Legacy prediction",
            topic = null,
            marks = null,
            year = null,
            prediction_score = 0.61
        )

        val result = SuggestionLookups.normalizePredictions(
            PredictionsResponse(
                success = true,
                items = null,
                predictions = listOf(legacy)
            )
        )

        assertEquals(1, result.size)
        assertEquals(41, result[0].questionId)
    }

    @Test
    fun `query normalization trims and clamps to max length`() {
        val query = " ${"a".repeat(1200)} "

        val normalized = SuggestionLookups.normalizeQuery(query)

        assertEquals(1000, normalized.length)
        assertEquals("a", normalized.first().toString())
    }

    @Test
    fun `top k is clamped into allowed range`() {
        assertEquals(1, SuggestionLookups.clampTopK(0))
        assertEquals(50, SuggestionLookups.clampTopK(999))
        assertEquals(10, SuggestionLookups.clampTopK(10))
    }

    @Test
    fun `prediction score normalization handles invalid and out of bounds values`() {
        assertEquals(1.0, SuggestionLookups.normalizePredictionScore(1.2))
        assertEquals(0.0, SuggestionLookups.normalizePredictionScore(-0.1))
        assertNull(SuggestionLookups.normalizePredictionScore(Double.NaN))
    }
}
