package com.example.sheerhealthinterview.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.http.GET

object SheerAPI {
    private const val BASE_URL = "https://android-project-465819884967.us-central1.run.app"

    private fun okhttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .build()
    }

    private val retrofit = Retrofit.Builder()
        .client(okhttpClient())
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(BASE_URL)
        .build()

    val retrofitService: SheerApiService by lazy {
        retrofit.create(SheerApiService::class.java)
    }
}

private class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        requestBuilder.addHeader("Authorization", "1")

        return chain.proceed(requestBuilder.build())
    }
}

interface SheerApiService {
    @GET("case")
    suspend fun getCases(): retrofit2.Response<List<Case>>
}

@Serializable
data class Case (
    val title: String,
    val caseId: String,
    val timestamp: String,
    val status: String
)