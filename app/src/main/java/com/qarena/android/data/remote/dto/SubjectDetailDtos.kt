package com.qarena.android.data.remote.dto

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.qarena.android.model.Suggestion
import com.qarena.android.model.SubQuestion
import java.lang.reflect.Type

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

@JsonAdapter(PredictionListResponseDeserializer::class)
data class PredictionListResponse(
    @SerializedName("items")
    val items: List<PredictionDto> = emptyList(),

    @SerializedName("suggestions")
    val suggestions: List<PredictionDto> = emptyList(),

    @SerializedName("predictions")
    val predictions: List<PredictionDto> = emptyList(),

    @SerializedName("data")
    val data: List<PredictionDto> = emptyList(),

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("pending_review_count")
    val pendingReviewCount: Int? = null,

    @SerializedName("total")
    val total: Int? = null
) {
    fun predictionList(): List<PredictionDto> {
        return when {
            items.isNotEmpty() -> items
            suggestions.isNotEmpty() -> suggestions
            predictions.isNotEmpty() -> predictions
            data.isNotEmpty() -> data
            else -> emptyList()
        }
    }
}

data class TopicAnalysisDto(
    @SerializedName("topic")
    val topic: String? = null,

    @SerializedName("question_count")
    val questionCount: Int? = null,

    @SerializedName("count")
    val count: Int? = null,

    @SerializedName("total_marks")
    val totalMarks: Int? = null,

    @SerializedName("average_marks")
    val averageMarks: Double? = null,

    @SerializedName("years")
    val years: List<Int>? = null,

    @SerializedName("paper_type")
    val paperType: String? = null,

    @SerializedName("prediction_score")
    val predictionScore: Double? = null
)

@JsonAdapter(TopicAnalysisResponseDeserializer::class)
data class TopicAnalysisResponse(
    @SerializedName("items")
    val items: List<TopicAnalysisDto> = emptyList(),

    @SerializedName("topics")
    val topics: List<TopicAnalysisDto> = emptyList(),

    @SerializedName("analysis")
    val analysis: List<TopicAnalysisDto> = emptyList(),

    @SerializedName("data")
    val data: List<TopicAnalysisDto> = emptyList(),

    @SerializedName("message")
    val message: String? = null
) {
    fun topicList(): List<TopicAnalysisDto> {
        return when {
            items.isNotEmpty() -> items
            topics.isNotEmpty() -> topics
            analysis.isNotEmpty() -> analysis
            data.isNotEmpty() -> data
            else -> emptyList()
        }
    }
}

data class PredictionDto(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("question_text")
    val question_text: String? = null,

    @SerializedName("question_id")
    val question_id: Int? = null,

    @SerializedName("subject_code")
    val subjectCode: String? = null,

    @SerializedName("question_no")
    val questionNo: String? = null,

    @SerializedName(value = "topic", alternate = ["final_topic", "suggested_topic"])
    val topic: String? = null,

    @SerializedName(value = "marks", alternate = ["total_marks", "expected_marks"])
    val marks: Int? = null,

    @SerializedName(value = "year", alternate = ["exam_year"])
    val year: Int? = null,

    @SerializedName("prediction_score")
    val prediction_score: Double? = null,

    @SerializedName("score")
    val score: Double? = null,

    @SerializedName("confidence")
    val confidence: Double? = null,

    @SerializedName("reason")
    val reason: String? = null,

    @SerializedName("related_questions")
    val relatedQuestions: List<QuestionDto>? = null,

    @SerializedName("paper_type")
    val paperType: String? = null,

    @SerializedName("diagram_required")
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

private class PredictionListResponseDeserializer : JsonDeserializer<PredictionListResponse> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PredictionListResponse {
        return when {
            json.isJsonArray -> PredictionListResponse(items = json.asJsonArray.toPredictionDtoList(context))
            json.isJsonObject -> json.asJsonObject.toPredictionListResponse(context)
            else -> throw JsonParseException("Unsupported prediction response shape")
        }
    }
}

private fun JsonObject.toPredictionListResponse(context: JsonDeserializationContext): PredictionListResponse {
    return PredictionListResponse(
        items = getPredictionList("items", context),
        suggestions = getPredictionList("suggestions", context),
        predictions = getPredictionList("predictions", context),
        data = getPredictionList("data", context),
        message = getStringOrNull("message"),
        pendingReviewCount = get("pending_review_count")?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }?.asInt,
        total = get("total")?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }?.asInt
    )
}

private fun JsonObject.getPredictionList(name: String, context: JsonDeserializationContext): List<PredictionDto> {
    val element = get(name) ?: return emptyList()
    return when {
        element.isJsonArray -> element.asJsonArray.toPredictionDtoList(context)
        element.isJsonObject -> listOf(context.deserialize(element, PredictionDto::class.java))
        else -> emptyList()
    }
}

private fun JsonArray.toPredictionDtoList(context: JsonDeserializationContext): List<PredictionDto> {
    return mapNotNull { item ->
        runCatching<PredictionDto> { context.deserialize(item, PredictionDto::class.java) }.getOrNull()
    }
}

private class TopicAnalysisResponseDeserializer : JsonDeserializer<TopicAnalysisResponse> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TopicAnalysisResponse {
        return when {
            json.isJsonArray -> TopicAnalysisResponse(items = json.asJsonArray.toTopicAnalysisDtoList(context))
            json.isJsonObject -> json.asJsonObject.toTopicAnalysisResponse(context)
            else -> throw JsonParseException("Unsupported topic analysis response shape")
        }
    }
}

private fun JsonObject.toTopicAnalysisResponse(context: JsonDeserializationContext): TopicAnalysisResponse {
    return TopicAnalysisResponse(
        items = getTopicAnalysisList("items", context),
        topics = getTopicAnalysisList("topics", context),
        analysis = getTopicAnalysisList("analysis", context),
        data = getTopicAnalysisList("data", context),
        message = getStringOrNull("message")
    )
}

private fun JsonObject.getTopicAnalysisList(name: String, context: JsonDeserializationContext): List<TopicAnalysisDto> {
    val element = get(name) ?: return emptyList()
    return when {
        element.isJsonArray -> element.asJsonArray.toTopicAnalysisDtoList(context)
        element.isJsonObject -> listOf(context.deserialize(element, TopicAnalysisDto::class.java))
        else -> emptyList()
    }
}

private fun JsonArray.toTopicAnalysisDtoList(context: JsonDeserializationContext): List<TopicAnalysisDto> {
    return mapNotNull { item ->
        runCatching<TopicAnalysisDto> { context.deserialize(item, TopicAnalysisDto::class.java) }.getOrNull()
    }
}

private fun JsonObject.getStringOrNull(name: String): String? {
    val element = get(name) ?: return null
    return if (element.isJsonPrimitive && element.asJsonPrimitive.isString) element.asString else null
}
