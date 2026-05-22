package com.qarena.android.util

data class RegisterFormInput(
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String,
    val confirmPassword: String,
    val academicLevel: String,
    val universityId: Int? = null,
    val departmentId: Int? = null,
    val curriculum: String? = null,
    val streamGroup: String? = null,
    val classLevel: String? = null,
    val termsAccepted: Boolean,
    val program: String? = null,
    val batchSession: String? = null
)
