package com.qarena.android.data.remote.dto

import com.google.gson.Gson
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginRequestSerializationTest {

    @Test
    fun loginRequestSerializesOnlyEmailAndPassword() {
        val json = Gson().toJson(
            LoginRequest(
                email = "student@example.com",
                password = "Password1"
            )
        )

        assertTrue(json.contains("\"email\""))
        assertTrue(json.contains("\"password\""))
        assertFalse(json.contains("role"))
        assertFalse(json.contains("remember"))
        assertFalse(json.contains("device"))
        assertFalse(json.contains("metadata"))
        assertFalse(json.contains("user"))
    }
}
