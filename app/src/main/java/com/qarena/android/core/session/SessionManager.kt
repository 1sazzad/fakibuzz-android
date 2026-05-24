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

    var userAcademicLevel: String? = null
        private set

    var userUniversityId: Int? = null
        private set

    var userDepartmentId: Int? = null
        private set

    var userCurriculum: String? = null
        private set

    var userStreamGroup: String? = null
        private set

    fun saveSession(token: String) {
        accessToken = token
    }

    fun saveSession(
        token: String,
        email: String?,
        role: String?,
        userId: Int?,
        academicLevel: String? = userAcademicLevel,
        universityId: Int? = userUniversityId,
        departmentId: Int? = userDepartmentId,
        curriculum: String? = userCurriculum,
        streamGroup: String? = userStreamGroup
    ) {
        accessToken = token
        userEmail = email
        userRole = role
        this.userId = userId
        userAcademicLevel = academicLevel?.trim()?.takeIf { it.isNotBlank() }
        userUniversityId = universityId
        userDepartmentId = departmentId
        userCurriculum = curriculum
        userStreamGroup = streamGroup
    }

    suspend fun saveSession(
        tokenStorage: TokenStorage,
        token: String,
        email: String? = userEmail,
        role: String? = userRole,
        userId: Int? = this.userId,
        academicLevel: String? = userAcademicLevel,
        universityId: Int? = userUniversityId,
        departmentId: Int? = userDepartmentId,
        curriculum: String? = userCurriculum,
        streamGroup: String? = userStreamGroup
    ) {
        saveSession(
            token = token,
            email = email,
            role = role,
            userId = userId,
            academicLevel = academicLevel,
            universityId = universityId,
            departmentId = departmentId,
            curriculum = curriculum,
            streamGroup = streamGroup
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
        userAcademicLevel = null
        userUniversityId = null
        userDepartmentId = null
        userCurriculum = null
        userStreamGroup = null
    }

    suspend fun clearSession(tokenStorage: TokenStorage) {
        clearSession()
        tokenStorage.clearAccessToken()
    }

    fun isLoggedIn(): Boolean {
        return !accessToken.isNullOrBlank()
    }
}
