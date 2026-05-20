package com.qarena.android.core.session

object SessionManager {

    var accessToken: String? = null
        private set

    var userEmail: String? = null
        private set

    var userRole: String? = null
        private set

    var userId: Int? = null
        private set

    fun saveSession(token: String, email: String?, role: String?, userId: Int?) {
        accessToken = token
        userEmail = email
        userRole = role
        this.userId = userId
    }

    fun clearSession() {
        accessToken = null
        userEmail = null
        userRole = null
        userId = null
    }

    fun isLoggedIn(): Boolean {
        return !accessToken.isNullOrBlank()
    }
}
