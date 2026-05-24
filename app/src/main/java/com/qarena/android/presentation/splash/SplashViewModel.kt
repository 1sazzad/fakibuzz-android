package com.qarena.android.presentation.splash

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qarena.android.core.session.SessionManager
import com.qarena.android.core.session.TokenStorage
import com.qarena.android.core.session.isProfileComplete
import com.qarena.android.data.repository.AuthRepository
import com.qarena.android.util.AcademicProfile
import kotlinx.coroutines.launch

sealed interface StartupRouteState {
    data object Checking : StartupRouteState
    data object Login : StartupRouteState
    data object ProfileSetup : StartupRouteState
    data object Home : StartupRouteState
}

class SplashViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val tokenStorage = TokenStorage(application)

    var startupRouteState by mutableStateOf<StartupRouteState>(StartupRouteState.Checking)
        private set

    fun checkStartupRoute() {
        viewModelScope.launch {
            startupRouteState = StartupRouteState.Checking

            val token = SessionManager.loadFromStorage(tokenStorage)

            if (token.isNullOrBlank()) {
                startupRouteState = StartupRouteState.Login
                return@launch
            }

            val result = authRepository.getMe(token)

            result
                .onSuccess { user ->
                    SessionManager.saveSession(
                        tokenStorage = tokenStorage,
                        token = token,
                        email = user.email,
                        role = user.role,
                        userId = user.id,
                        academicLevel = AcademicProfile.resolveAcademicLevel(user),
                        universityId = user.universityId,
                        departmentId = user.departmentId,
                        curriculum = user.curriculum,
                        streamGroup = user.streamGroup
                    )

                    startupRouteState = if (user.isProfileComplete) {
                        StartupRouteState.Home
                    } else {
                        StartupRouteState.ProfileSetup
                    }
                }
                .onFailure {
                    SessionManager.clearSession(tokenStorage)
                    startupRouteState = StartupRouteState.Login
                }
        }
    }
}
