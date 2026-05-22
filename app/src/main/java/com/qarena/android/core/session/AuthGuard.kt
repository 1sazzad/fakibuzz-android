package com.qarena.android.core.session

fun hasAccessToken(): Boolean {
    return SessionManager.accessToken.isNullOrBlank().not()
}
