package com.qarena.android.core.network

import com.qarena.android.BuildConfig
import com.google.gson.GsonBuilder
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        redactHeader("Authorization")
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val authDebugInterceptor = Interceptor { chain ->
        val request = chain.request()
        val hasAuthorizationHeader = !request.header("Authorization").isNullOrBlank()

        if (BuildConfig.DEBUG) {
            Log.d(
                "AuthDebug",
                "${request.method} ${request.url} Auth header present: $hasAuthorizationHeader"
            )
        }

        val response = chain.proceed(request)

        if (BuildConfig.DEBUG) {
            Log.d(
                "AuthDebug",
                "Response code: ${response.code} ${request.method} ${request.url}"
            )
        }

        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authDebugInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .build()
}
