package com.senac.restapi.api

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object GeminiClient {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    private const val TAG = "GeminiHTTP"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request()
            Log.d(TAG, "→ ${request.method} ${request.url.encodedPath}")
            val response = chain.proceed(request)
            val bodyBytes = response.peekBody(Long.MAX_VALUE)
            val bodySnippet = bodyBytes.string().take(800)
            Log.d(TAG, "← HTTP ${response.code} | body: $bodySnippet")
            response
        }
        .build()

    val geminiApi: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }
}
