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

    fun saveSession(token: String) {
        accessToken = token
    }

    fun saveSession(token: String, email: String?, role: String?, userId: Int?) {
        accessToken = token
        userEmail = email
        userRole = role
        this.userId = userId
    }

    suspend fun saveSession(
        tokenStorage: TokenStorage,
        token: String,
        email: String? = userEmail,
        role: String? = userRole,
        userId: Int? = this.userId
    ) {
        saveSession(
            token = token,
            email = email,
            role = role,
            userId = userId
        )
        tokenStorage.saveAccessToken(token)
    }

    suspend fun loadFromStorage(tokenStorage: TokenStorage): String? {
        val token = tokenStorage.getAccessToken()

        if (token.isNullOrBlank()) {
            clearSession()
            return null
        }

        accessToken = token
        return token
    }

    fun clearSession() {
        accessToken = null
        userEmail = null
        userRole = null
        userId = null
    }

    suspend fun clearSession(tokenStorage: TokenStorage) {
        clearSession()
        tokenStorage.clearAccessToken()
    }

    fun isLoggedIn(): Boolean {
        return !accessToken.isNullOrBlank()
    }
}
