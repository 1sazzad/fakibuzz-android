package com.qarena.android.core.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionManagerTest {

    @Test
    fun saveSessionStoresTokenAndRole() {
        SessionManager.clearSession()

        SessionManager.saveSession(
            token = "access-token",
            email = null,
            role = "student",
            userId = null
        )

        assertEquals("access-token", SessionManager.accessToken)
        assertEquals("student", SessionManager.userRole)
    }

    @Test
    fun clearSessionClearsTokenRoleAndUser() {
        SessionManager.saveSession(
            token = "access-token",
            email = "student@example.com",
            role = "student",
            userId = 7
        )

        SessionManager.clearSession()

        assertNull(SessionManager.accessToken)
        assertNull(SessionManager.userEmail)
        assertNull(SessionManager.userRole)
        assertNull(SessionManager.userId)
    }
}
