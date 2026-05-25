package com.qarena.android.data.repository

import com.qarena.android.core.network.RetrofitClient
import com.qarena.android.core.session.SessionManager
import com.qarena.android.data.remote.ApiErrorParser
import com.qarena.android.data.remote.ApiException
import com.qarena.android.data.remote.api.SubjectApi
import com.qarena.android.data.remote.dto.SubjectAnalysisResponse
import com.qarena.android.data.remote.dto.SubjectOverviewResponse
import com.qarena.android.data.remote.dto.SubjectListResponse
import com.qarena.android.model.Subject
import com.qarena.android.model.Suggestion
import com.qarena.android.util.PaperTypeLookups
import com.qarena.android.util.SubjectLookups
import com.qarena.android.util.SuggestionLookups
import android.util.Log
import java.io.IOException
import retrofit2.HttpException
import retrofit2.Response

class SubjectRepository(
    private val subjectApi: SubjectApi = RetrofitClient.retrofit.create(SubjectApi::class.java),
    private val accessTokenProvider: () -> String? = { SessionManager.accessToken }
) {

    suspend fun getSubjects(): Result<List<Subject>> {
        val token = accessTokenProvider()

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        return try {
            val scope = resolveSubjectScope()
            logScopeState("SubjectList", scope)
            val response = subjectApi.getSubjects(
                authorization = "Bearer $token",
                academicLevel = scope.academicLevel,
                universityId = scope.universityId,
                departmentId = scope.departmentId,
                curriculum = scope.curriculum,
                streamGroup = scope.streamGroup
            )
            val subjects = SubjectLookups.normalizeSubjects(response)
            logSubjectResponseCount("SubjectList", subjects.size)
            Result.success(subjects)
        } catch (exception: Exception) {
            Result.failure(Exception(mapThrowable(exception, "Failed to load subject data")))
        }
    }

    suspend fun searchSubjects(query: String): Result<List<Subject>> {
        val token = accessTokenProvider()
        val trimmedQuery = query.trim()

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        if (trimmedQuery.isBlank()) {
            return Result.failure(Exception("Search query is required"))
        }

        return try {
            val scope = resolveSubjectScope()
            val response = subjectApi.searchSubjects(
                authorization = "Bearer $token",
                query = trimmedQuery,
                academicLevel = scope.academicLevel,
                universityId = scope.universityId,
                departmentId = scope.departmentId,
                curriculum = scope.curriculum,
                streamGroup = scope.streamGroup
            )
            val subjects = SubjectLookups.normalizeSubjects(response)
            logSubjectResponseCount("SubjectSearch", subjects.size)
            Result.success(subjects)
        } catch (exception: Exception) {
            Result.failure(Exception(mapThrowable(exception, "Failed to load subject data")))
        }
    }

    suspend fun getSubjectOverview(
        subjectCode: String,
        paperType: String? = null,
        debugScreenName: String? = null,
        subject: Subject? = null
    ): Result<SubjectOverviewResponse> {
        val token = accessTokenProvider()
        val trimmedSubjectCode = subjectCode.trim()
        val normalizedPaperType = PaperTypeLookups.normalizePaperType(paperType)

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        if (trimmedSubjectCode.isBlank()) {
            return Result.failure(Exception("Subject code is required"))
        }

        return try {
            val scope = resolveSubjectScope()
            logSubjectRequest(
                screenName = debugScreenName ?: "SubjectOverview",
                endpoint = "/subjects/$trimmedSubjectCode/overview",
                subject = subject,
                subjectCode = trimmedSubjectCode,
                paperType = normalizedPaperType,
                academicLevel = scope.academicLevel,
                extraParams = mapOf(
                    "university_id" to scope.universityId,
                    "department_id" to scope.departmentId,
                    "curriculum" to scope.curriculum,
                    "stream_group" to scope.streamGroup
                )
            )
            val response = subjectApi.getSubjectOverview(
                authorization = "Bearer $token",
                subjectCode = trimmedSubjectCode,
                academicLevel = scope.academicLevel,
                paperType = normalizedPaperType,
                curriculum = scope.curriculum,
                streamGroup = scope.streamGroup
            )
            logSubjectResponse(debugScreenName ?: "SubjectOverview", 200, null)
            Result.success(response)
        } catch (exception: Exception) {
            logSubjectError(debugScreenName ?: "SubjectOverview", exception)
            Result.failure(Exception(mapThrowable(exception, "Failed to load subject overview")))
        }
    }

    suspend fun getSubjectAnalysis(
        subjectCode: String,
        paperType: String? = null,
        debugScreenName: String? = null,
        subject: Subject? = null
    ): Result<SubjectAnalysisResponse> {
        val token = accessTokenProvider()
        val trimmedSubjectCode = subjectCode.trim()
        val normalizedPaperType = PaperTypeLookups.normalizePaperType(paperType)

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        if (trimmedSubjectCode.isBlank()) {
            return Result.failure(Exception("Subject code is required"))
        }

        return try {
            val scope = resolveSubjectScope()
            logSubjectRequest(
                screenName = debugScreenName ?: "SubjectAnalysis",
                endpoint = "/subjects/$trimmedSubjectCode/analysis",
                subject = subject,
                subjectCode = trimmedSubjectCode,
                paperType = normalizedPaperType,
                academicLevel = scope.academicLevel,
                extraParams = mapOf(
                    "university_id" to scope.universityId,
                    "department_id" to scope.departmentId,
                    "curriculum" to scope.curriculum,
                    "stream_group" to scope.streamGroup
                )
            )
            val response = subjectApi.getSubjectAnalysis(
                authorization = "Bearer $token",
                subjectCode = trimmedSubjectCode,
                academicLevel = scope.academicLevel,
                paperType = normalizedPaperType,
                curriculum = scope.curriculum,
                streamGroup = scope.streamGroup
            )
            logSubjectResponse(debugScreenName ?: "SubjectAnalysis", 200, null)
            Result.success(response)
        } catch (exception: Exception) {
            logSubjectError(debugScreenName ?: "SubjectAnalysis", exception)
            Result.failure(Exception(mapThrowable(exception, "Failed to load subject analysis")))
        }
    }

    suspend fun getSubjectPredictions(
        subjectCode: String,
        paperType: String? = null,
        debugScreenName: String? = null,
        subject: Subject? = null
    ): Result<List<Suggestion>> {
        val token = accessTokenProvider()
        val trimmedSubjectCode = subjectCode.trim()
        val normalizedPaperType = PaperTypeLookups.normalizePaperType(paperType)

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        if (trimmedSubjectCode.isBlank()) {
            return Result.failure(Exception("Subject code is required"))
        }

        return try {
            val scope = resolveSubjectScope()
            logSubjectRequest(
                screenName = debugScreenName ?: "SubjectPredictions",
                endpoint = "/subjects/$trimmedSubjectCode/predictions",
                subject = subject,
                subjectCode = trimmedSubjectCode,
                paperType = normalizedPaperType,
                academicLevel = scope.academicLevel,
                extraParams = mapOf(
                    "university_id" to scope.universityId,
                    "department_id" to scope.departmentId,
                    "curriculum" to scope.curriculum,
                    "stream_group" to scope.streamGroup
                )
            )
            val response = subjectApi.getPredictions(
                authorization = "Bearer $token",
                subjectCode = trimmedSubjectCode,
                academicLevel = scope.academicLevel,
                paperType = normalizedPaperType,
                universityId = scope.universityId,
                departmentId = scope.departmentId,
                curriculum = scope.curriculum,
                streamGroup = scope.streamGroup
            )
            if (!response.isSuccessful) {
                val errorBody = runCatching { response.errorBody()?.string() }.getOrNull()
                logSubjectResponse(debugScreenName ?: "SubjectPredictions", response.code(), errorBody)
                return Result.failure(Exception(parseSubjectApiError(response.code(), errorBody, "Failed to load subject predictions")))
            }

            val normalized = SuggestionLookups.normalizePredictions(response.body())
            logSubjectResponse(debugScreenName ?: "SubjectPredictions", response.code(), null)
            Result.success(normalized)
        } catch (exception: Exception) {
            logSubjectError(debugScreenName ?: "SubjectPredictions", exception)
            Result.failure(Exception(mapThrowable(exception, "Failed to load subject predictions")))
        }
    }

    suspend fun getSubjectSuggestions(
        subjectCode: String,
        query: String,
        topK: Int = 100,
        paperType: String? = null,
        debugScreenName: String? = null,
        subject: Subject? = null
    ): Result<List<Suggestion>> {
        val token = accessTokenProvider()
        val trimmedSubjectCode = subjectCode.trim()
        val normalizedQuery = SuggestionLookups.normalizeQuery(query)
        val clampedTopK = SuggestionLookups.clampTopK(topK)
        val normalizedPaperType = PaperTypeLookups.normalizePaperType(paperType)

        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not logged in"))
        }

        if (trimmedSubjectCode.isBlank()) {
            return Result.failure(Exception("Subject code is required"))
        }

        if (normalizedQuery.isBlank()) {
            return Result.failure(Exception("Search query is required"))
        }

        if (paperType != null && normalizedPaperType == null) {
            return Result.failure(Exception("Invalid paper type. Please choose CQ, MCQ, or WRITTEN."))
        }

        return try {
            val scope = resolveSubjectScope()
            logSubjectRequest(
                screenName = debugScreenName ?: "SubjectSuggestions",
                endpoint = buildSuggestionsRequestUrl(
                    subjectCode = trimmedSubjectCode,
                    query = normalizedQuery,
                    topK = clampedTopK,
                    paperType = normalizedPaperType,
                    scope = scope
                ),
                subject = subject,
                subjectCode = trimmedSubjectCode,
                paperType = normalizedPaperType,
                academicLevel = scope.academicLevel,
                extraParams = suggestionsExtraParams(
                    query = normalizedQuery,
                    topK = clampedTopK,
                    scope = scope
                )
            )
            val response = subjectApi.getSuggestions(
                authorization = "Bearer $token",
                subjectCode = trimmedSubjectCode,
                query = normalizedQuery,
                topK = clampedTopK,
                paperType = normalizedPaperType,
                academicLevel = scope.academicLevel,
                universityId = scope.universityId,
                departmentId = scope.departmentId,
                curriculum = scope.curriculum,
                streamGroup = scope.streamGroup
            )
            if (!response.isSuccessful) {
                val errorBody = runCatching { response.errorBody()?.string() }.getOrNull()
                logSubjectResponse(debugScreenName ?: "SubjectSuggestions", response.code(), errorBody)
                return Result.failure(Exception(parseSubjectApiError(response.code(), errorBody, "Failed to load subject suggestions")))
            }

            val normalized = SuggestionLookups.normalizeSuggestions(response.body())
            logSuggestionSuccess(
                screenName = debugScreenName ?: "SubjectSuggestions",
                response = response.body(),
                suggestions = normalized
            )
            logSubjectResponseCount(debugScreenName ?: "SubjectSuggestions", normalized.size)
            logSubjectResponse(debugScreenName ?: "SubjectSuggestions", response.code(), null)
            Result.success(normalized)
        } catch (exception: Exception) {
            logSubjectError(debugScreenName ?: "SubjectSuggestions", exception)
            Result.failure(Exception(mapThrowable(exception, "Failed to load subject suggestions")))
        }
    }

    private fun logSubjectRequest(
        screenName: String,
        endpoint: String,
        subject: Subject?,
        subjectCode: String,
        paperType: String?,
        academicLevel: String?,
        extraParams: Map<String, Any?>
    ) {
        Log.d(
            "SubjectRequest",
            buildString {
                append(screenName)
                append(" request_url=")
                append(endpoint)
                append(" subject.id=")
                append(subject?.id ?: "null")
                append(" subject.subject_code=")
                append(subject?.subjectCode ?: subjectCode)
                append(" paper_type=")
                append(paperType ?: "null")
                append(" academic_level=")
                append(academicLevel ?: "null")
                if (extraParams.isNotEmpty()) {
                    append(" params=")
                    append(extraParams.entries.joinToString(", ") { "${it.key}=${it.value}" })
                }
            }
        )
    }

    private fun logScopeState(screenName: String, scope: SubjectScope) {
        Log.d(
            "SubjectRequest",
            buildString {
                append(screenName)
                append(" current_user academic_level=")
                append(scope.academicLevel ?: "null")
                append(" university_id=")
                append(scope.universityId ?: "null")
                append(" department_id=")
                append(scope.departmentId ?: "null")
                append(" curriculum=")
                append(scope.curriculum ?: "null")
                append(" stream_group=")
                append(scope.streamGroup ?: "null")
            }
        )
    }

    private fun logSubjectResponseCount(screenName: String, count: Int) {
        Log.d("SubjectRequest", "$screenName response_count=$count")
    }

    private fun logSubjectResponse(screenName: String, code: Int, errorBody: String?) {
        Log.d(
            "SubjectRequest",
            "$screenName response code=$code${errorBody?.let { " error_body=$it" } ?: ""}"
        )
    }

    private fun logSuggestionSuccess(
        screenName: String,
        response: com.qarena.android.data.remote.dto.SuggestionsResponse?,
        suggestions: List<Suggestion>
    ) {
        val first = suggestions.firstOrNull()
        Log.d(
            "SubjectRequest",
            buildString {
                append(screenName)
                append(" raw.success=")
                append(response?.success)
                append(" raw.message=")
                append(response?.message ?: "null")
                append(" success count=")
                append(suggestions.size)
                append(" response.warning=")
                append(response?.warning ?: "null")
                append(" response.fallback_warning=")
                append(response?.fallbackWarning ?: "null")
                append(" response.retrieval_source=")
                append(response?.retrievalSource ?: "null")
                append(" response.fallback_used=")
                append(response?.fallbackUsed)
                append(" first.question_text=")
                append(first?.questionText ?: "null")
                append(" first.stem=")
                append(first?.stem ?: "null")
                append(" first.paper_type=")
                append(first?.paperType ?: "null")
                append(" first.diagram_svg_present=")
                append(first?.diagramSvg?.isNotBlank() == true)
                append(" first.sub_questions=")
                append(first?.subQuestions?.size ?: 0)
            }
        )
    }

    private fun logSubjectError(screenName: String, exception: Exception) {
        val httpException = exception as? HttpException
        val errorBody = runCatching { httpException?.response()?.errorBody()?.string() }.getOrNull()
        Log.d(
            "SubjectRequest",
            buildString {
                append(screenName)
                append(" error code=")
                append(httpException?.code() ?: "null")
                append(" message=")
                append(exception.message ?: "null")
                if (!errorBody.isNullOrBlank()) {
                    append(" error_body=")
                    append(errorBody)
                }
            }
        )
    }

    private fun parseSubjectApiError(response: Response<*>?, fallback: String): String {
        val code = response?.code() ?: 0
        val errorBody = runCatching { response?.errorBody()?.string() }.getOrNull()
        return parseSubjectApiError(code, errorBody, fallback)
    }

    private fun parseSubjectApiError(code: Int, errorBody: String?, fallback: String): String {
        val parsed = ApiErrorParser.parseErrorBody(errorBody)

        val messageFromCode = ApiErrorParser.messageForSubjectLookupCode(
            code = ApiErrorParser.resolvedCode(parsed),
            fallback = parsed.message
        )

        if (!messageFromCode.isNullOrBlank()) {
            return messageFromCode
        }

        return when (code) {
            400 -> "Invalid paper type. Please choose CQ, MCQ, or WRITTEN."
            422 -> "The selected subject scope could not be analyzed. Please try a different paper type."
            401 -> "Session expired. Please log in again."
            403 -> "You do not have access to this subject scope."
            404 -> "Subject not found or inactive."
            429 -> "Too many requests. Please try again later."
            in 500..599 -> "Server error. Please try again."
            else -> ApiErrorParser.messageForHttpStatus(code, fallback)
        }
    }

    private fun backendAcademicLevel(value: String?): String? {
        return when (value?.trim()?.lowercase()) {
            "ssc" -> "SSC"
            "hsc" -> "HSC"
            "university" -> "UNIVERSITY"
            else -> value?.trim()?.takeIf { it.isNotBlank() }
        }
    }

    private fun resolveSubjectScope(): SubjectScope {
        val academicLevel = backendAcademicLevel(SessionManager.userAcademicLevel)
        val universityId = if (academicLevel == "UNIVERSITY") SessionManager.userUniversityId else null
        val departmentId = if (academicLevel == "UNIVERSITY") SessionManager.userDepartmentId else null
        val curriculum = if (academicLevel != "UNIVERSITY") SessionManager.userCurriculum else null
        val streamGroup = if (academicLevel != "UNIVERSITY") SessionManager.userStreamGroup else null

        return SubjectScope(
            academicLevel = academicLevel,
            universityId = universityId,
            departmentId = departmentId,
            curriculum = curriculum,
            streamGroup = streamGroup
        )
    }

    private fun buildSuggestionsRequestUrl(
        subjectCode: String,
        query: String,
        topK: Int,
        paperType: String?,
        scope: SubjectScope
    ): String {
        return buildString {
            append("/subjects/")
            append(subjectCode)
            append("/suggestions?query=")
            append(query)
            append("&top_k=")
            append(topK)
            paperType?.let {
                append("&paper_type=")
                append(it)
            }
            scope.academicLevel?.let {
                append("&academic_level=")
                append(it)
            }
            scope.universityId?.let {
                append("&university_id=")
                append(it)
            }
            scope.departmentId?.let {
                append("&department_id=")
                append(it)
            }
            scope.curriculum?.let {
                append("&curriculum=")
                append(it)
            }
            scope.streamGroup?.let {
                append("&stream_group=")
                append(it)
            }
        }
    }

    private fun suggestionsExtraParams(
        query: String,
        topK: Int,
        scope: SubjectScope
    ): Map<String, Any?> {
        return buildMap {
            put("query", query)
            put("top_k", topK)
            scope.universityId?.let { put("university_id", it) }
            scope.departmentId?.let { put("department_id", it) }
            scope.curriculum?.let { put("curriculum", it) }
            scope.streamGroup?.let { put("stream_group", it) }
        }
    }

    private data class SubjectScope(
        val academicLevel: String?,
        val universityId: Int?,
        val departmentId: Int?,
        val curriculum: String?,
        val streamGroup: String?
    )

    private fun mapThrowable(exception: Exception, fallback: String): String {
        return when (exception) {
            is ApiException -> exception.message ?: fallback
            is HttpException -> parseSubjectApiError(exception.response(), fallback)
            is IOException -> ApiErrorParser.messageForThrowable(exception)
            else -> exception.message ?: fallback
        }
    }
}
