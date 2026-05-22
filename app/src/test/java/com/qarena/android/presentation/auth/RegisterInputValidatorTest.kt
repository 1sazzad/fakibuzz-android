package com.qarena.android.presentation.auth

import com.qarena.android.util.AcademicProfile
import com.qarena.android.util.RegisterFormInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegisterInputValidatorTest {

    @Test
    fun validateAcceptsCompleteStudentRegistrationInput() {
        val error = RegisterInputValidator.validate(validInput())

        assertNull(error)
    }

    @Test
    fun validateRejectsInvalidBangladeshPhone() {
        val error = RegisterInputValidator.validate(
            validInput().copy(phone = "12345")
        )

        assertEquals("Enter a valid Bangladesh phone number", error)
    }

    @Test
    fun validateRejectsPasswordWithoutNumber() {
        val error = RegisterInputValidator.validate(
            validInput().copy(password = "Password", confirmPassword = "Password")
        )

        assertEquals("Password must include at least one number", error)
    }

    @Test
    fun validateRejectsMissingTerms() {
        val error = RegisterInputValidator.validate(
            validInput().copy(termsAccepted = false)
        )

        assertEquals("You must accept the terms to register", error)
    }

    private fun validInput(): RegisterFormInput {
        return RegisterFormInput(
            fullName = "Student User",
            email = "student@example.com",
            phone = "+8801712345678",
            password = "Password1",
            confirmPassword = "Password1",
            academicLevel = AcademicProfile.ACADEMIC_LEVEL_UNIVERSITY,
            universityId = 1,
            departmentId = 2,
            termsAccepted = true
        )
    }
}
