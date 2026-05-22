package com.qarena.android.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiErrorParserTest {

    @Test
    fun parseErrorBodyReadsStandardBackendError() {
        val error = ApiErrorParser.parseErrorBody(
            """
            {
              "success": false,
              "message": "Email already exists.",
              "field": "email",
              "code": "EMAIL_ALREADY_EXISTS"
            }
            """.trimIndent()
        )

        assertEquals(false, error.success)
        assertEquals("Email already exists.", error.message)
        assertEquals("email", error.field)
        assertEquals("EMAIL_ALREADY_EXISTS", error.code)
    }

    @Test
    fun messageForCodeUsesFallbackMessageFirst() {
        val message = ApiErrorParser.messageForCode(
            code = "REGISTRATION_RATE_LIMITED",
            fallback = "Try again later."
        )

        assertEquals("Try again later.", message)
    }

    @Test
    fun messageForCodeHandlesKnownBackendCode() {
        val message = ApiErrorParser.messageForCode(
            code = "INVALID_DEPARTMENT",
            fallback = null
        )

        assertEquals("Selected department is not valid.", message)
    }

    @Test
    fun invalidStreamGroupCodeUsesClearStudentMessage() {
        val message = ApiErrorParser.messageForCode(
            code = "INVALID_STREAM_GROUP",
            fallback = null
        )

        assertEquals(
            "Common is not a student group. Choose Science, Business Studies, or Humanities.",
            message
        )
    }

    @Test
    fun messageForLoginCodeHandlesSuspiciousRequest() {
        val message = ApiErrorParser.messageForLoginCode(
            code = "SUSPICIOUS_REQUEST",
            fallback = "Backend message should not replace this."
        )

        assertEquals("Request was rejected for security reasons.", message)
    }

    @Test
    fun messageForLoginCodeUsesBackendValidationMessage() {
        val message = ApiErrorParser.messageForLoginCode(
            code = "VALIDATION_ERROR",
            fallback = "Email is invalid."
        )

        assertEquals("Email is invalid.", message)
    }

    @Test
    fun messageForSubjectLookupCodeHandlesInactiveSubject() {
        val message = ApiErrorParser.messageForSubjectLookupCode(
            code = "SUBJECT_INACTIVE",
            fallback = "Backend message should not replace this."
        )

        assertEquals("Subject not found or inactive.", message)
    }

    @Test
    fun messageForSubjectLookupCodeUsesValidationFallback() {
        val message = ApiErrorParser.messageForSubjectLookupCode(
            code = "VALIDATION_ERROR",
            fallback = "Query cannot be blank."
        )

        assertEquals("Query cannot be blank.", message)
    }
}
