package com.qarena.android.presentation.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoginInputValidatorTest {

    @Test
    fun validateAcceptsTrimmedValidEmailAndNonEmptyPassword() {
        val error = LoginInputValidator.validate(
            email = " student@example.com ",
            password = " x "
        )

        assertNull(error)
    }

    @Test
    fun validateRejectsMissingEmail() {
        val error = LoginInputValidator.validate(
            email = " ",
            password = "Password1"
        )

        assertEquals("Email is required", error)
    }

    @Test
    fun validateRejectsInvalidEmail() {
        val error = LoginInputValidator.validate(
            email = "not-an-email",
            password = "Password1"
        )

        assertEquals("Enter a valid email address", error)
    }

    @Test
    fun validateRejectsBlankPasswordOnly() {
        val error = LoginInputValidator.validate(
            email = "student@example.com",
            password = " "
        )

        assertEquals("Password is required", error)
    }
}
