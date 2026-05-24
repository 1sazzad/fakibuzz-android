package com.qarena.android.data.remote

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.qarena.android.data.remote.dto.ApiErrorResponse
import java.io.IOException
import java.net.SocketTimeoutException

class ApiException(
    message: String,
    val code: String? = null,
    val field: String? = null
) : Exception(message)

object ApiErrorParser {
    private val gson = Gson()

    fun parseErrorBody(errorBody: String?): ApiErrorResponse {
        if (errorBody.isNullOrBlank()) {
            return ApiErrorResponse(message = null)
        }

        return try {
            gson.fromJson(errorBody, ApiErrorResponse::class.java)
                ?: ApiErrorResponse(message = null)
        } catch (_: JsonSyntaxException) {
            ApiErrorResponse(message = null)
        }
    }

    fun messageForThrowable(throwable: Throwable): String {
        return when (throwable) {
            is ApiException -> throwable.message ?: "Request failed"
            is SocketTimeoutException -> "Request timed out. Please try again."
            is IOException -> "Network error. Check your connection and try again."
            else -> throwable.message ?: "Unknown error. Please try again."
        }
    }

    fun messageForCode(code: String?, fallback: String?): String {
        if (!fallback.isNullOrBlank()) {
            return fallback
        }

        return when (code) {
            "EMAIL_ALREADY_EXISTS" -> "An account with this email already exists."
            "PHONE_ALREADY_EXISTS" -> "An account with this phone number already exists."
            "TERMS_NOT_ACCEPTED" -> "You must accept the terms to register."
            "VALIDATION_ERROR" -> "Please check your registration details and try again."
            "REGISTRATION_RATE_LIMITED" -> "Too many registration attempts. Please try again later."
            "INVALID_UNIVERSITY" -> "Selected university is not valid."
            "INVALID_DEPARTMENT" -> "Selected department is not valid."
            "INVALID_ACADEMIC_LEVEL" -> "Selected academic level is not valid."
            "INVALID_CURRICULUM" -> "Selected curriculum is not valid."
            "INVALID_STREAM_GROUP" -> "Common is not a student group. Choose Science, Business Studies, or Humanities."
            "INVALID_PAPER_TYPE" -> "Invalid paper type. Please choose CQ, MCQ, or WRITTEN."
            "INVALID_CLASS_LEVEL" -> "Selected class level is not valid."
            "MISSING_ACADEMIC_PROFILE", "ACADEMIC_PROFILE_REQUIRED" -> {
                "Please complete your academic profile."
            }
            "UNIVERSITY_PROFILE_REQUIRED", "UNIVERSITY_AND_DEPARTMENT_REQUIRED" -> {
                "University and department are required."
            }
            "INVALID_SUBJECT_SCOPE" -> "Your academic profile does not match the requested subjects."
            else -> "Registration failed. Please try again."
        }
    }

    fun messageForHttpStatus(statusCode: Int, fallback: String): String {
        return when (statusCode) {
            400 -> "Invalid request. Please review your input and try again."
            401 -> "Session expired. Please log in again."
            403 -> "You do not have permission to access this resource."
            429 -> "Too many requests. Please try again later."
            in 500..599 -> "Server error. Please try again later."
            else -> fallback
        }
    }

    fun resolvedCode(error: ApiErrorResponse): String? {
        return error.code ?: error.errorCode
    }

    fun messageForLoginCode(code: String?, fallback: String?): String {
        return when (code) {
            "INVALID_CREDENTIALS" -> "Invalid email or password."
            "ACCOUNT_INACTIVE" -> "Your account is inactive. Please contact support."
            "EMAIL_NOT_VERIFIED" -> "Please verify your email before logging in."
            "LOGIN_RATE_LIMITED" -> "Too many login attempts. Please try again later."
            "VALIDATION_ERROR" -> fallback ?: "Please check your login details and try again."
            "SUSPICIOUS_REQUEST" -> "Request was rejected for security reasons."
            else -> fallback ?: "Login failed. Please try again."
        }
    }

    fun messageForSubjectLookupCode(code: String?, fallback: String?): String {
        return when (code) {
            "VALIDATION_ERROR", "BLANK_QUERY", "INVALID_QUERY" -> {
                fallback ?: "Please provide a valid query and try again."
            }

            "INVALID_SUBJECT_SCOPE", "MISSING_ACADEMIC_PROFILE" -> {
                fallback ?: "Your academic profile does not match the requested subjects."
            }

            "INVALID_PAPER_TYPE" -> "Invalid paper type. Please choose CQ, MCQ, or WRITTEN."

            "UNAUTHORIZED", "TOKEN_EXPIRED" -> "Session expired. Please log in again."
            "SUBJECT_INACTIVE", "SUBJECT_NOT_FOUND", "NOT_FOUND" -> "Subject not found or inactive."
            "RATE_LIMITED", "TOO_MANY_REQUESTS" -> "Too many requests. Please try again later."
            else -> fallback ?: "Failed to load subject data. Please try again."
        }
    }
}
