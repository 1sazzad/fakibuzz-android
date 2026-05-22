package com.qarena.android.core.session

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.legacyAuthDataStore by preferencesDataStore(name = "auth_session")

class TokenStorage(context: Context) {

    private val appContext = context.applicationContext

    private val encryptedPreferences: SharedPreferences by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            appContext,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    suspend fun saveAccessToken(token: String) {
        withContext(Dispatchers.IO) {
            encryptedPreferences.edit()
                .putString(ACCESS_TOKEN_KEY, token)
                .commit()
            clearLegacyAccessToken()
        }
    }

    suspend fun getAccessToken(): String? {
        return withContext(Dispatchers.IO) {
            val encryptedToken = encryptedPreferences.getString(ACCESS_TOKEN_KEY, null)
                ?.takeIf { it.isNotBlank() }

            if (encryptedToken != null) {
                return@withContext encryptedToken
            }

            val legacyToken = appContext.legacyAuthDataStore.data
                .map { preferences ->
                    preferences[LEGACY_ACCESS_TOKEN_KEY]?.takeIf { it.isNotBlank() }
                }
                .first()

            if (legacyToken != null) {
                encryptedPreferences.edit()
                    .putString(ACCESS_TOKEN_KEY, legacyToken)
                    .commit()
                clearLegacyAccessToken()
            }

            legacyToken
        }
    }

    suspend fun clearAccessToken() {
        withContext(Dispatchers.IO) {
            encryptedPreferences.edit()
                .remove(ACCESS_TOKEN_KEY)
                .commit()
            clearLegacyAccessToken()
        }
    }

    private suspend fun clearLegacyAccessToken() {
        appContext.legacyAuthDataStore.edit { preferences ->
            preferences.remove(LEGACY_ACCESS_TOKEN_KEY)
        }
    }

    private companion object {
        const val ENCRYPTED_PREFS_NAME = "auth_session_encrypted"
        const val ACCESS_TOKEN_KEY = "access_token"
        val LEGACY_ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    }
}
