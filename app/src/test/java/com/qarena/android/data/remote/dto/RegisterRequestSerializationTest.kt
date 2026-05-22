package com.qarena.android.data.remote.dto

import com.google.gson.Gson
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterRequestSerializationTest {

    @Test
    fun registerRequestSerializesOnlyBackendContractFields() {
        val json = Gson().toJson(
            RegisterRequest(
                full_name = "Student User",
                email = "student@example.com",
                phone_number = "01712345678",
                password = "Password1",
                university_id = 1,
                department_id = 2,
                program = "BSc in CSE",
                batch_session = "2021-2022",
                terms_accepted = true
            )
        )

        assertTrue(json.contains("\"full_name\""))
        assertTrue(json.contains("\"phone_number\""))
        assertTrue(json.contains("\"university_id\""))
        assertTrue(json.contains("\"department_id\""))
        assertTrue(json.contains("\"batch_session\""))
        assertTrue(json.contains("\"terms_accepted\""))
        assertFalse(json.contains("confirm"))
        assertFalse(json.contains("university_name"))
        assertFalse(json.contains("department_name"))
        assertFalse(json.contains("institution"))
        assertFalse(json.contains("college"))
        assertFalse(json.contains("board"))
        assertFalse(json.contains("\"year\""))
        assertFalse(json.contains("semester"))
        assertFalse(json.contains("role"))
    }
}
